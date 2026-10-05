package com.pemmob.tulungin.data.user

import android.content.Context
import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Production implementation of UserRepository backed strictly by Cloud Firestore.
 * Listens to FirebaseAuth state changes to prevent cross-account data leakage.
 */
class FirestoreUserRepository(context: Context) : UserRepository {
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
    private val mutex = Mutex()
    private val mutableSnapshot = MutableStateFlow(UserSnapshot())
    override val snapshot: StateFlow<UserSnapshot> = mutableSnapshot.asStateFlow()

    private var jobsListener: ListenerRegistration? = null
    private var applicationsListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            listenToData(user.uid)
        } else {
            userListener?.remove()
            userListener = null
            jobsListener?.remove()
            jobsListener = null
            applicationsListener?.remove()
            applicationsListener = null
            mutableSnapshot.value = UserSnapshot()
        }
    }

    private fun parseJobStatus(raw: String?): JobStatus {
        if (raw == null) return JobStatus.AVAILABLE
        return when (raw.lowercase()) {
            "open", "available" -> JobStatus.AVAILABLE
            "accepted" -> JobStatus.ACCEPTED
            "in_progress", "inprogress", "progress" -> JobStatus.IN_PROGRESS
            "awaiting_confirmation", "awaiting" -> JobStatus.AWAITING_CONFIRMATION
            "completed" -> JobStatus.COMPLETED
            "cancelled" -> JobStatus.CANCELLED
            else -> runCatching { JobStatus.valueOf(raw.uppercase()) }.getOrDefault(JobStatus.AVAILABLE)
        }
    }

    init {
        runCatching {
            auth.addAuthStateListener(authStateListener)
            val currentUser = auth.currentUser
            if (currentUser != null) {
                listenToData(currentUser.uid)
            }
        }
    }

    private fun listenToData(userId: String) {
        userListener?.remove()
        userListener = firestore.collection("users").document(userId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null && firebaseUser.uid == userId) {
                        mutableSnapshot.value = snapshot.value.copy(
                            profile = UserProfile(
                                id = userId,
                                name = firebaseUser.displayName ?: "Pengguna",
                                email = firebaseUser.email ?: "",
                                verified = false,
                                verificationRequested = false
                            )
                        )
                    }
                    return@addSnapshotListener
                }
                val profile = UserProfile(
                    id = userId,
                    name = doc.getString("name") ?: auth.currentUser?.displayName ?: "",
                    email = doc.getString("email") ?: auth.currentUser?.email ?: "",
                    phone = doc.getString("phone") ?: "",
                    address = doc.getString("address") ?: "",
                    verified = doc.getBoolean("verified") ?: false,
                    photoUrl = doc.getString("photoUrl") ?: auth.currentUser?.photoUrl?.toString() ?: "",
                    verificationRequested = doc.getBoolean("verificationRequested") ?: false
                )
                mutableSnapshot.value = snapshot.value.copy(profile = profile)
            }

        jobsListener?.remove()
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
                            locationLat = document.get("locationLat") as? Double ?: document.get("locationLat")?.toString()?.toDoubleOrNull(),
                            locationLng = document.get("locationLng") as? Double ?: document.get("locationLng")?.toString()?.toDoubleOrNull(),
                            scheduledAt = document.getString("scheduledAt") ?: "",
                            fee = when (val f = document.get("fee")) { is Number -> f.toLong(); is String -> f.toLongOrNull() ?: 0L; else -> 0L },
                            distanceKm = when (val d = document.get("distanceKm")) { is Number -> d.toDouble(); is String -> d.toDoubleOrNull() ?: 0.0; else -> 0.0 },
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
                            rating = when (val r = document.get("rating")) { is Number -> r.toInt(); is String -> r.toIntOrNull() ?: 0; else -> 0 },
                            review = document.getString("review") ?: "",
                            paymentMethod = document.getString("paymentMethod") ?: "",
                            paid = document.getBoolean("paid") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                val current = snapshot.value
                mutableSnapshot.value = current.copy(jobs = firestoreJobs)
            }

        applicationsListener?.remove()
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
                            createdAt = when (val c = document.get("createdAt")) {
                                is Number -> c.toLong()
                                is Timestamp -> c.toDate().time
                                is String -> c.toLongOrNull() ?: System.currentTimeMillis()
                                else -> System.currentTimeMillis()
                            }
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                val current = snapshot.value
                mutableSnapshot.value = current.copy(applications = firestoreApps)
            }
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

        mutableSnapshot.value = current.copy(jobs = updatedJobs)
    }

    /**
     * Creates a new job request and persists it to Cloud Firestore.
     */
    override suspend fun createJob(draft: JobDraft): String = mutex.withLock {
        require(draft.title.trim().length >= 4) { "Judul minimal 4 karakter." }
        require(draft.category in JobRules.categories) { "Pilih kategori bantuan." }
        require(draft.description.trim().length >= 10) { "Detail kebutuhan minimal 10 karakter." }
        require(draft.location.isNotBlank() && draft.scheduledAt.isNotBlank()) { "Lengkapi lokasi serta waktu." }
        require(draft.fee in 1000..100000000) { "Upah harus antara Rp1.000 dan Rp100.000.000." }

        val firebaseUser = auth.currentUser ?: error("User belum login.")
        val userId = firebaseUser.uid
        listenToData(userId)
        val id = runCatching { firestore.collection("jobs").document().id }.getOrNull() ?: UUID.randomUUID().toString()

        val requesterName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: snapshot.value.profile.name.ifBlank { "Pengguna" }

        val job = UserJob(
            id = id,
            title = draft.title.trim(),
            category = draft.category,
            description = draft.description.trim(),
            location = draft.location.trim(),
            locationLat = draft.locationLat,
            locationLng = draft.locationLng,
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
            "locationLat" to job.locationLat,
            "locationLng" to job.locationLng,
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

        firestore.collection("jobs").document(id).set(jobData).await()
        id
    }

    override suspend fun applyJob(jobId: String): Unit = mutex.withLock {
        val firebaseUser = auth.currentUser ?: error("User belum login.")
        val userId = firebaseUser.uid
        listenToData(userId)
        val current = snapshot.value
        val userName = current.profile.name.ifBlank { firebaseUser.displayName ?: "Pengguna" }

        val job = current.jobs.firstOrNull { it.id == jobId } ?: error("Job tidak ditemukan.")
        require(job.requesterId != userId) { "Peminta tidak dapat melamar job sendiri." }
        require(job.status == JobStatus.AVAILABLE && job.helperId == null) { "Job sudah tidak tersedia atau sudah memiliki helper." }

        val existingInFirestore = firestore.collection("applications")
            .whereEqualTo("jobId", jobId)
            .whereEqualTo("applicantId", userId)
            .get().await()

        require(existingInFirestore.isEmpty && current.applications.none { it.jobId == jobId && it.applicantId == userId }) {
            "Kamu sudah mengirim lamaran untuk job ini."
        }

        val appId = runCatching { firestore.collection("applications").document().id }.getOrNull() ?: UUID.randomUUID().toString()
        val appData = hashMapOf(
            "id" to appId,
            "jobId" to jobId,
            "applicantId" to userId,
            "applicantName" to userName,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("applications").document(appId).set(appData).await()
    }

    /**
     * Selects a helper application, updating job status to IN_PROGRESS in Firestore.
     */
    override suspend fun selectApplication(jobId: String, applicationId: String): Unit = mutex.withLock {
        val firebaseUser = auth.currentUser ?: error("User belum login.")
        val userId = firebaseUser.uid
        listenToData(userId)
        val current = snapshot.value

        val job = current.jobs.firstOrNull { it.id == jobId } ?: error("Job tidak ditemukan.")
        require(job.requesterId == userId) { "Hanya pemilik job yang dapat memilih helper." }

        val targetApp = current.applications.firstOrNull { it.id == applicationId && it.jobId == jobId } ?: error("Lamaran tidak ditemukan.")
        require(targetApp.status == "pending") { "Lamaran ini sudah diproses." }

        firestore.collection("jobs").document(jobId).update(
            mapOf(
                "helperId" to targetApp.applicantId,
                "helperName" to targetApp.applicantName,
                "status" to JobStatus.IN_PROGRESS.name
            )
        ).await()

        val batch = firestore.batch()
        batch.update(firestore.collection("applications").document(targetApp.id), "status", "accepted")
        current.applications.filter { it.jobId == jobId && it.id != applicationId && it.status == "pending" }.forEach { app ->
            batch.update(firestore.collection("applications").document(app.id), "status", "rejected")
        }
        batch.commit().await()
    }

    override suspend fun acceptJob(id: String) = changeJob(id) { j, p -> JobRules.accept(j, p) }
    override suspend fun startJob(id: String) = changeJob(id) { j, p -> JobRules.start(j, p.id) }
    override suspend fun submitProof(id: String, uri: String, name: String) = changeJob(id) { j, p -> JobRules.submitProof(j, p.id, uri, name) }
    override suspend fun submitPaymentProof(id: String, uri: String, name: String) = changeJob(id) { j, p -> JobRules.submitPaymentProof(j, p.id, uri, name) }
    override suspend fun confirmCompletion(id: String) = changeJob(id) { j, p -> JobRules.confirmJob(j, p.id) }
    override suspend fun submitReview(id: String, rating: Int, text: String) = changeJob(id) { j, p -> JobRules.review(j, p.id, rating, text) }
    override suspend fun pay(id: String, method: String) = changeJob(id) { j, p -> JobRules.pay(j, p.id, method) }

    override suspend fun updateProfile(profile: UserProfile): Unit = mutex.withLock {
        require(profile.name.trim().length >= 2) { "Nama minimal 2 karakter." }
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(profile.email.trim())) { "Format email belum benar." }
        require(Regex("^\\+?[0-9]{10,15}$").matches(profile.phone.trim())) { "Nomor HP harus 10–15 digit." }
        require(profile.address.trim().length >= 5) { "Lengkapi alamatmu." }
        
        val firebaseUser = auth.currentUser ?: error("User belum login.")
        val currentId = firebaseUser.uid
        listenToData(currentId)
        val updated = profile.copy(id = currentId, name = profile.name.trim(), email = profile.email.trim(), phone = profile.phone.trim(), address = profile.address.trim())

        firestore.collection("users").document(currentId).set(
            mapOf(
                "name" to updated.name,
                "email" to updated.email,
                "phone" to updated.phone,
                "address" to updated.address,
                "photoUrl" to updated.photoUrl,
                "verified" to updated.verified,
                "verificationRequested" to updated.verificationRequested,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()

        val current = snapshot.value
        mutableSnapshot.value = current.copy(profile = updated)
    }

    override suspend fun sendMessage(jobId: String, text: String): Unit = mutex.withLock {
        require(text.isNotBlank() && text.length <= 2000) { "Pesan harus berisi 1–2.000 karakter." }
        val firebaseUser = auth.currentUser ?: error("User belum login.")
        val userId = firebaseUser.uid
        val msgRef = firestore.collection("jobs").document(jobId).collection("messages").document()
        val msgData = mapOf(
            "id" to msgRef.id,
            "jobId" to jobId,
            "senderId" to userId,
            "senderName" to (snapshot.value.profile.name.ifBlank { firebaseUser.displayName ?: "Pengguna" }),
            "message" to text.trim(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        msgRef.set(msgData).await()
    }

    override suspend fun sendSupport(message: String): String = mutex.withLock {
        require(message.isNotBlank()) { "Pesan bantuan tidak boleh kosong." }
        val firebaseUser = auth.currentUser ?: error("User belum login.")
        val ticketRef = firestore.collection("support_tickets").document()
        val ticketData = mapOf(
            "id" to ticketRef.id,
            "userId" to firebaseUser.uid,
            "message" to message.trim(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        ticketRef.set(ticketData).await()
        ticketRef.id
    }

    override fun updateHelperLocation(lat: Double, lng: Double) {
        val current = snapshot.value
        val updatedJobs = current.jobs.map { job ->
            if (job.locationLat != null && job.locationLng != null) {
                val results = FloatArray(1)
                Location.distanceBetween(lat, lng, job.locationLat, job.locationLng, results)
                job.copy(distanceKm = results[0] / 1000.0)
            } else {
                job
            }
        }
        mutableSnapshot.value = current.copy(jobs = updatedJobs)
    }
}
