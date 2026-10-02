package com.pemmob.tulungin.data.user

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class LocalDemoRepository(context: Context) : UserRepository, DemoControls {
    private val appContext = context.applicationContext
    private val auth by lazy {
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext)
        }
        FirebaseAuth.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val preferences = appContext.getSharedPreferences("tulungin_user_demo_v1", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val mutableSnapshot = MutableStateFlow(
        run {
            val decoded = preferences.getString("snapshot", null)?.let { runCatching { SnapshotCodec.decode(it) }.getOrNull() } ?: DemoFixtures.initial()
            val firebaseUser = runCatching { auth.currentUser }.getOrNull()
            if (firebaseUser != null) {
                decoded.copy(profile = decoded.profile.copy(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: decoded.profile.email,
                    name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: decoded.profile.name
                ))
            } else {
                decoded
            }
        }
    )
    override val snapshot: StateFlow<UserSnapshot> = mutableSnapshot.asStateFlow()
    private var jobsListener: ListenerRegistration? = null
    private var applicationsListener: ListenerRegistration? = null

    private fun parseJobStatus(raw: String?): JobStatus {
        if (raw == null) return JobStatus.AVAILABLE
        return when (raw.lowercase()) {
            "open", "available" -> JobStatus.AVAILABLE
            "accepted" -> JobStatus.ACCEPTED
            "in_progress", "inprogress" -> JobStatus.IN_PROGRESS
            "awaiting_confirmation", "awaiting" -> JobStatus.AWAITING_CONFIRMATION
            "completed" -> JobStatus.COMPLETED
            "cancelled" -> JobStatus.CANCELLED
            else -> runCatching { JobStatus.valueOf(raw.uppercase()) }.getOrDefault(JobStatus.AVAILABLE)
        }
    }

    init {
        runCatching {
            jobsListener = firestore.collection("jobs")
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) return@addSnapshotListener
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
                                status = parseJobStatus(document.getString("status")),
                                proofUri = document.getString("proofUri"),
                                proofName = document.getString("proofName"),
                                paymentProofUri = document.getString("paymentProofUri"),
                                paymentProofName = document.getString("paymentProofName"),
                                helperConfirmed = document.getBoolean("helperConfirmed") ?: false,
                                requesterConfirmed = document.getBoolean("requesterConfirmed") ?: false,
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

            applicationsListener = firestore.collection("applications")
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val documents = querySnapshot?.documents ?: return@addSnapshotListener
                    val firestoreApps = documents.mapNotNull { document ->
                        try {
                            UserApplication(
                                id = document.getString("id") ?: document.id,
                                jobId = document.getString("jobId") ?: "",
                                applicantId = document.getString("applicantId") ?: "",
                                applicantName = document.getString("applicantName") ?: "",
                                status = document.getString("status") ?: "pending",
                                createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val current = snapshot.value
                    save(current.copy(applications = firestoreApps))
                }
        }
    }

    private fun save(state: UserSnapshot) {
        val firebaseUser = runCatching { auth.currentUser }.getOrNull()
        val syncedState = if (firebaseUser != null && state.profile.id != firebaseUser.uid) {
            state.copy(profile = state.profile.copy(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: state.profile.email,
                name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: state.profile.name
            ))
        } else {
            state
        }
        preferences.edit().putString("snapshot", SnapshotCodec.encode(syncedState)).apply()
        mutableSnapshot.value = syncedState
    }

    private suspend fun changeJob(id: String, transform: (UserJob, UserProfile) -> UserJob) = mutex.withLock {
        val current = snapshot.value
        require(current.jobs.any { it.id == id }) { "Job tidak ditemukan." }
        val updatedJobs = current.jobs.map { if (it.id == id) transform(it, current.profile) else it }
        val updatedJob = updatedJobs.first { it.id == id }

        runCatching {
            firestore.collection("jobs").document(id).update(
                mapOf(
                    "helperId" to updatedJob.helperId,
                    "helperName" to updatedJob.helperName,
                    "status" to updatedJob.status.name,
                    "proofUri" to updatedJob.proofUri,
                    "proofName" to updatedJob.proofName,
                    "paymentProofUri" to updatedJob.paymentProofUri,
                    "paymentProofName" to updatedJob.paymentProofName,
                    "helperConfirmed" to updatedJob.helperConfirmed,
                    "requesterConfirmed" to updatedJob.requesterConfirmed,
                    "rating" to updatedJob.rating,
                    "review" to updatedJob.review,
                    "paymentMethod" to updatedJob.paymentMethod,
                    "paid" to updatedJob.paid
                )
            ).await()
        }

        save(current.copy(jobs = updatedJobs))
    }

    override suspend fun createJob(draft: JobDraft): String = mutex.withLock {
        require(draft.title.trim().length >= 4) { "Judul minimal 4 karakter." }
        require(draft.category in JobRules.categories) { "Pilih kategori bantuan." }
        require(draft.description.trim().length >= 10) { "Detail kebutuhan minimal 10 karakter." }
        require(draft.location.isNotBlank() && draft.scheduledAt.isNotBlank()) { "Lengkapi lokasi serta waktu." }
        require(draft.fee in 1000..100000000) { "Upah harus antara Rp1.000 dan Rp100.000.000." }

        val firebaseUser = runCatching { auth.currentUser }.getOrNull()
        val current = snapshot.value
        val userId = firebaseUser?.uid ?: current.profile.id
        val id = runCatching { firestore.collection("jobs").document().id }.getOrNull() ?: UUID.randomUUID().toString()

        val requesterName = if (current.profile.id == userId && current.profile.name.isNotBlank()) {
            current.profile.name
        } else {
            firebaseUser?.displayName ?: current.profile.name.ifBlank { "Pengguna" }
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
            requesterId = userId,
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
            "paymentProofUri" to null,
            "paymentProofName" to null,
            "helperConfirmed" to false,
            "requesterConfirmed" to false,
            "rating" to 0,
            "review" to "",
            "paymentMethod" to "",
            "paid" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        runCatching {
            firestore.collection("jobs").document(id).set(jobData).await()
        }

        val updatedProfile = current.profile.copy(
            id = userId,
            name = requesterName,
            email = firebaseUser?.email ?: current.profile.email
        )

        save(current.copy(profile = updatedProfile, jobs = listOf(job) + current.jobs))
        id
    }

    override suspend fun applyJob(jobId: String) = mutex.withLock {
        val current = snapshot.value
        val firebaseUser = runCatching { auth.currentUser }.getOrNull()
        val userId = firebaseUser?.uid ?: current.profile.id
        val userName = current.profile.name.ifBlank { firebaseUser?.displayName ?: "Pengguna" }

        val job = current.jobs.firstOrNull { it.id == jobId } ?: error("Job tidak ditemukan.")
        require(job.requesterId != userId) { "Peminta tidak dapat melamar job sendiri." }
        require(job.status == JobStatus.AVAILABLE && job.helperId == null) { "Job sudah tidak tersedia atau sudah memiliki helper." }

        require(current.applications.none { it.jobId == jobId && it.applicantId == userId && it.status in listOf("pending", "accepted") }) {
            "Kamu sudah mengirim lamaran untuk job ini."
        }

        val appId = runCatching { firestore.collection("applications").document().id }.getOrNull() ?: UUID.randomUUID().toString()
        val app = UserApplication(
            id = appId,
            jobId = jobId,
            applicantId = userId,
            applicantName = userName,
            status = "pending"
        )

        val appData = hashMapOf(
            "id" to appId,
            "jobId" to jobId,
            "applicantId" to userId,
            "applicantName" to userName,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )

        runCatching {
            firestore.collection("applications").document(appId).set(appData).await()
        }

        save(current.copy(applications = current.applications + app))
    }

    override suspend fun selectApplication(jobId: String, applicationId: String) = mutex.withLock {
        val current = snapshot.value
        val firebaseUser = runCatching { auth.currentUser }.getOrNull()
        val userId = firebaseUser?.uid ?: current.profile.id

        val job = current.jobs.firstOrNull { it.id == jobId } ?: error("Job tidak ditemukan.")
        require(job.requesterId == userId) { "Hanya pemilik job yang dapat memilih helper." }

        val targetApp = current.applications.firstOrNull { it.id == applicationId && it.jobId == jobId } ?: error("Lamaran tidak ditemukan.")
        require(targetApp.status == "pending") { "Lamaran ini sudah diproses." }

        runCatching {
            firestore.collection("jobs").document(jobId).update(
                mapOf(
                    "helperId" to targetApp.applicantId,
                    "helperName" to targetApp.applicantName,
                    "status" to JobStatus.IN_PROGRESS.name
                )
            ).await()
        }

        val batch = runCatching { firestore.batch() }.getOrNull()
        if (batch != null) {
            batch.update(firestore.collection("applications").document(targetApp.id), "status", "accepted")
            current.applications.filter { it.jobId == jobId && it.id != applicationId && it.status == "pending" }.forEach { app ->
                batch.update(firestore.collection("applications").document(app.id), "status", "rejected")
            }
            runCatching { batch.commit().await() }
        }

        val updatedApps = current.applications.map { app ->
            if (app.jobId == jobId) {
                if (app.id == applicationId) {
                    app.copy(status = "accepted")
                } else if (app.status == "pending") {
                    app.copy(status = "rejected")
                } else {
                    app
                }
            } else {
                app
            }
        }

        val updatedJobs = current.jobs.map { j ->
            if (j.id == jobId) {
                j.copy(helperId = targetApp.applicantId, helperName = targetApp.applicantName, status = JobStatus.IN_PROGRESS)
            } else {
                j
            }
        }

        save(current.copy(jobs = updatedJobs, applications = updatedApps))
    }

    override suspend fun acceptJob(id: String) = changeJob(id) { j, p -> JobRules.accept(j, p) }
    override suspend fun startJob(id: String) = changeJob(id) { j, p -> JobRules.start(j, p.id) }
    override suspend fun submitProof(id: String, uri: String, name: String) = changeJob(id) { j, p -> JobRules.submitProof(j, p.id, uri, name) }
    override suspend fun submitPaymentProof(id: String, uri: String, name: String) = changeJob(id) { j, p -> JobRules.submitPaymentProof(j, p.id, uri, name) }
    override suspend fun confirmCompletion(id: String) = changeJob(id) { j, p -> JobRules.confirmJob(j, p.id) }
    override suspend fun submitReview(id: String, rating: Int, text: String) = changeJob(id) { j, p -> JobRules.review(j, p.id, rating, text) }
    override suspend fun pay(id: String, method: String) = changeJob(id) { j, p -> JobRules.pay(j, p.id, method) }

    override suspend fun updateProfile(profile: UserProfile) = mutex.withLock {
        require(profile.name.trim().length >= 2) { "Nama minimal 2 karakter." }
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(profile.email.trim())) { "Format email belum benar." }
        require(Regex("^\\+?[0-9]{10,15}$").matches(profile.phone.trim())) { "Nomor HP harus 10–15 digit." }
        require(profile.address.trim().length >= 5) { "Lengkapi alamatmu." }
        val current = snapshot.value
        val firebaseUser = runCatching { auth.currentUser }.getOrNull()
        val currentId = firebaseUser?.uid ?: current.profile.id
        val updated = profile.copy(id = currentId, verified = current.profile.verified, name = profile.name.trim(), email = profile.email.trim(), phone = profile.phone.trim(), address = profile.address.trim())
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
