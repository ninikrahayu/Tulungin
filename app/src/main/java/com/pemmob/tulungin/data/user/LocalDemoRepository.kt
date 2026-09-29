package com.pemmob.tulungin.data.user

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration

class LocalDemoRepository(context: Context) : UserRepository, DemoControls {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val preferences = context.applicationContext.getSharedPreferences("tulungin_user_demo_v1", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val mutableSnapshot = MutableStateFlow(
        preferences.getString("snapshot", null)?.let { runCatching { SnapshotCodec.decode(it) }.getOrNull() } ?: DemoFixtures.initial()
    )
    override val snapshot: StateFlow<UserSnapshot> = mutableSnapshot.asStateFlow()
    private var jobsListener: ListenerRegistration? = null

    init {
        jobsListener = firestore.collection("jobs")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val documents = querySnapshot?.documents ?: return@addSnapshotListener

                val firestoreJobs = documents.mapNotNull { document ->
                    try {
                        UserJob(
                            id = document.getString("id") ?: document.id,
                            title = document.getString("title") ?: "",
                            category = document.getString("category") ?: "",
                            description = document.getString("description") ?: "",
                            location = document.getString("location") ?: "",
                            scheduledAt = document.getString("scheduledAt") ?: "",
                            fee = document.getLong("fee") ?: 0L,
                            distanceKm = document.getDouble("distanceKm") ?: 0.0,
                            requesterId = document.getString("requesterId") ?: "",
                            requesterName = document.getString("requesterName") ?: "",
                            helperId = document.getString("helperId"),
                            helperName = document.getString("helperName"),
                            status = JobStatus.valueOf(
                                document.getString("status") ?: "AVAILABLE"
                            ),
                            proofUri = document.getString("proofUri"),
                            proofName = document.getString("proofName"),
                            rating = (document.getLong("rating") ?: 0L).toInt(),
                            review = document.getString("review") ?: "",
                            paymentMethod = document.getString("paymentMethod") ?: "",
                            paid = document.getBoolean("paid") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                val current = snapshot.value
                save(current.copy(jobs = firestoreJobs))
            }
    }

    private fun save(state: UserSnapshot) {
        preferences.edit().putString("snapshot", SnapshotCodec.encode(state)).apply()
        mutableSnapshot.value = state
    }

    private suspend fun changeJob(id: String, transform: (UserJob, UserProfile) -> UserJob) = mutex.withLock {
        val current = snapshot.value
        require(current.jobs.any { it.id == id }) { "Job tidak ditemukan." }
        save(current.copy(jobs = current.jobs.map { if (it.id == id) transform(it, current.profile) else it }))
    }

    override suspend fun createJob(draft: JobDraft): String = mutex.withLock {
        require(draft.title.trim().length >= 4) {
            "Judul minimal 4 karakter."
        }
        require(draft.category in JobRules.categories) {
            "Pilih kategori bantuan."
        }
        require(draft.description.trim().length >= 10) {
            "Detail kebutuhan minimal 10 karakter."
        }
        require(draft.location.isNotBlank() && draft.scheduledAt.isNotBlank()) {
            "Lengkapi lokasi serta waktu."
        }
        require(draft.fee in 1000..100000000) {
            "Upah harus antara Rp1.000 dan Rp100.000.000."
        }

        val firebaseUser = auth.currentUser
            ?: error("Kamu harus login terlebih dahulu.")

        val current = snapshot.value

        val id = firestore.collection("jobs").document().id

        val requesterName = if (current.profile.id == firebaseUser.uid) {
            current.profile.name
        } else {
            firebaseUser.displayName ?: "Pengguna"
        }

        val job = UserJob(
            id = id,
            title = draft.title.trim(),
            category = draft.category,
            description = draft.description.trim(),
            location = draft.location.trim(),
            scheduledAt = draft.scheduledAt,
            fee = draft.fee,
            distanceKm = 0.0,
            requesterId = firebaseUser.uid,
            requesterName = requesterName
        )

        val jobData = hashMapOf<String, Any?>(
            "id" to id,
            "title" to job.title,
            "category" to job.category,
            "description" to job.description,
            "location" to job.location,
            "scheduledAt" to job.scheduledAt,
            "fee" to job.fee,
            "distanceKm" to job.distanceKm,
            "requesterId" to job.requesterId,
            "requesterName" to job.requesterName,
            "helperId" to null,
            "helperName" to null,
            "status" to "AVAILABLE",
            "proofUri" to null,
            "proofName" to null,
            "rating" to 0,
            "review" to "",
            "paymentMethod" to "",
            "paid" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        // Simpan job ke Firestore
        firestore.collection("jobs")
            .document(id)
            .set(jobData)
            .await()

        // Sinkronkan ID profil lokal dengan UID Firebase
        val updatedProfile = current.profile.copy(
            id = firebaseUser.uid,
            name = requesterName,
            email = firebaseUser.email ?: current.profile.email
        )

        // Perbarui snapshot lokal
        save(
            current.copy(
                profile = updatedProfile,
                jobs = listOf(job) + current.jobs
            )
        )

        id
    }

    override suspend fun acceptJob(id: String) = changeJob(id) { j, p -> JobRules.accept(j, p) }
    override suspend fun startJob(id: String) = changeJob(id) { j, p -> JobRules.start(j, p.id) }
    override suspend fun submitProof(id: String, uri: String, name: String) = changeJob(id) { j, p -> JobRules.submitProof(j, p.id, uri, name) }
    override suspend fun confirmCompletion(id: String) = changeJob(id) { j, p -> JobRules.complete(j, p.id) }
    override suspend fun submitReview(id: String, rating: Int, text: String) = changeJob(id) { j, p -> JobRules.review(j, p.id, rating, text) }
    override suspend fun pay(id: String, method: String) = changeJob(id) { j, p -> JobRules.pay(j, p.id, method) }

    override suspend fun updateProfile(profile: UserProfile) = mutex.withLock {
        require(profile.name.trim().length >= 2) { "Nama minimal 2 karakter." }
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(profile.email.trim())) { "Format email belum benar." }
        require(Regex("^\\+?[0-9]{10,15}$").matches(profile.phone.trim())) { "Nomor HP harus 10–15 digit." }
        require(profile.address.trim().length >= 5) { "Lengkapi alamatmu." }
        val current = snapshot.value
        val updated = profile.copy(id = current.profile.id, verified = current.profile.verified, name = profile.name.trim(), email = profile.email.trim(), phone = profile.phone.trim(), address = profile.address.trim())
        save(current.copy(profile = updated, jobs = current.jobs.map { j -> j.copy(
            requesterName = if (j.requesterId == updated.id) updated.name else j.requesterName,
            helperName = if (j.helperId == updated.id) updated.name else j.helperName
        ) }))
    }

    override suspend fun sendMessage(jobId: String, text: String) = mutex.withLock {
        require(text.isNotBlank() && text.length <= 2000) { "Pesan harus berisi 1–2.000 karakter." }
        val current = snapshot.value
        val job = current.jobs.first { it.id == jobId }
        require(job.requesterId == current.profile.id || job.helperId == current.profile.id || current.conversations.any { it.jobId == jobId }) { "Percakapan belum tersedia untuk job ini." }
        val existing = current.conversations.firstOrNull { it.jobId == jobId } ?: Conversation(jobId, if (job.requesterId == current.profile.id) job.helperName ?: "Penulung" else job.requesterName, emptyList())
        val updated = existing.copy(messages = existing.messages + listOf(
            ChatMessage(UUID.randomUUID().toString(), text.trim(), true),
            ChatMessage(UUID.randomUUID().toString(), "Baik, pesanmu sudah saya terima. (Balasan simulasi)", false)
        ))
        save(current.copy(conversations = current.conversations.filterNot { it.jobId == jobId } + updated))
    }

    override suspend fun sendSupport(message: String): String = mutex.withLock {
        require(message.trim().length >= 10 && message.length <= 2000) { "Pesan bantuan harus 10–2.000 karakter." }
        val current = snapshot.value
        val id = "DEMO-${current.tickets.size + 1}"
        save(current.copy(tickets = current.tickets + SupportTicket(id, message.trim())))
        id
    }

    override suspend fun simulateCounterparty(jobId: String) = changeJob(jobId) { job, profile ->
        when {
            job.requesterId == profile.id && job.status == JobStatus.AVAILABLE -> job.copy(helperId = "demo-helper", helperName = "Rina", status = JobStatus.IN_PROGRESS)
            job.requesterId == profile.id && job.status == JobStatus.IN_PROGRESS -> job.copy(status = JobStatus.AWAITING_CONFIRMATION, proofUri = "demo://bukti", proofName = "Bukti foto pekerjaan")
            job.helperId == profile.id && job.status == JobStatus.AWAITING_CONFIRMATION -> JobRules.complete(job, job.requesterId)
            else -> error("Tidak ada langkah lawan transaksi yang perlu disimulasikan.")
        }
    }

    override suspend fun resetDemo() = mutex.withLock { save(DemoFixtures.initial()) }
}
