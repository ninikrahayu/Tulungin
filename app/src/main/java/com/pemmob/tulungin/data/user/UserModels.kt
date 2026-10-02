package com.pemmob.tulungin.data.user

enum class JobStatus(val label: String) {
    AVAILABLE("Tersedia"),
    ACCEPTED("Job diambil"),
    IN_PROGRESS("Sedang dikerjakan"),
    AWAITING_CONFIRMATION("Menunggu konfirmasi"),
    COMPLETED("Selesai"),
    CANCELLED("Dibatalkan")
}

data class UserProfile(
    val id: String = "demo-user",
    val name: String = "Andi Pratama",
    val email: String = "andi@email.com",
    val phone: String = "081234567890",
    val address: String = "Jl. Kampus No. 12, Purwokerto",
    val verified: Boolean = true
)

data class UserJob(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val scheduledAt: String,
    val fee: Long,
    val distanceKm: Double,
    val requesterId: String,
    val requesterName: String,
    val helperId: String? = null,
    val helperName: String? = null,
    val status: JobStatus = JobStatus.AVAILABLE,
    val proofUri: String? = null,
    val proofName: String? = null,
    val paymentProofUri: String? = null,
    val paymentProofName: String? = null,
    val helperConfirmed: Boolean = false,
    val requesterConfirmed: Boolean = false,
    val rating: Int = 0,
    val review: String = "",
    val paymentMethod: String = "",
    val paid: Boolean = false
)

data class UserApplication(
    val id: String,
    val jobId: String,
    val applicantId: String,
    val applicantName: String,
    val status: String = "pending", // "pending", "accepted", "rejected"
    val createdAt: Long = System.currentTimeMillis()
)

data class JobDraft(
    val title: String,
    val category: String,
    val description: String,
    val location: String,
    val scheduledAt: String,
    val fee: Long
)

data class ChatMessage(val id: String, val text: String, val outgoing: Boolean)
data class Conversation(val jobId: String, val name: String, val messages: List<ChatMessage>)
data class SupportTicket(val id: String, val message: String)
data class UserSnapshot(
    val profile: UserProfile = UserProfile(),
    val jobs: List<UserJob> = emptyList(),
    val applications: List<UserApplication> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val tickets: List<SupportTicket> = emptyList()
)

object JobRules {
    fun accept(job: UserJob, user: UserProfile): UserJob {
        require(job.status == JobStatus.AVAILABLE) { "Job sudah tidak tersedia." }
        require(job.requesterId != user.id) { "Permintaan sendiri tidak dapat diambil." }
        require(user.verified) { "Akun harus terverifikasi untuk mengambil job." }
        return job.copy(helperId = user.id, helperName = user.name, status = JobStatus.ACCEPTED)
    }

    fun start(job: UserJob, userId: String): UserJob {
        require(job.helperId == userId && (job.status == JobStatus.ACCEPTED || job.status == JobStatus.IN_PROGRESS)) { "Job belum dapat dimulai." }
        return job.copy(status = JobStatus.IN_PROGRESS)
    }

    fun submitProof(job: UserJob, userId: String, uri: String, name: String): UserJob {
        require(job.helperId == userId && job.status == JobStatus.IN_PROGRESS) { "Mulai pekerjaan sebelum mengirim bukti." }
        require(uri.isNotBlank() && name.isNotBlank()) { "Pilih foto atau file bukti terlebih dahulu." }
        return job.copy(proofUri = uri, proofName = name)
    }

    fun submitPaymentProof(job: UserJob, userId: String, uri: String, name: String): UserJob {
        require(job.requesterId == userId) { "Hanya peminta yang dapat mengirim bukti pembayaran." }
        require(!job.proofUri.isNullOrBlank()) { "Helper harus mengirim bukti pekerjaan terlebih dahulu." }
        require(uri.isNotBlank() && name.isNotBlank()) { "Pilih foto atau file bukti pembayaran terlebih dahulu." }
        return job.copy(paymentProofUri = uri, paymentProofName = name, status = JobStatus.AWAITING_CONFIRMATION)
    }

    fun confirmJob(job: UserJob, userId: String): UserJob {
        require(!job.proofUri.isNullOrBlank() && !job.paymentProofUri.isNullOrBlank()) { "Kedua bukti (pekerjaan dan pembayaran) harus dikirim terlebih dahulu." }
        val isHelper = job.helperId == userId
        val isRequester = job.requesterId == userId
        require(isHelper || isRequester) { "Hanya pihak yang terlibat yang dapat mengonfirmasi." }

        val newHelperConfirmed = if (isHelper) true else job.helperConfirmed
        val newRequesterConfirmed = if (isRequester) true else job.requesterConfirmed

        val newStatus = if (newHelperConfirmed && newRequesterConfirmed) JobStatus.COMPLETED else job.status
        return job.copy(
            helperConfirmed = newHelperConfirmed,
            requesterConfirmed = newRequesterConfirmed,
            status = newStatus
        )
    }

    fun complete(job: UserJob, userId: String): UserJob {
        return confirmJob(job, userId)
    }

    fun review(job: UserJob, userId: String, rating: Int, text: String): UserJob {
        require(job.requesterId == userId && job.status == JobStatus.COMPLETED) { "Ulasan diberikan peminta setelah pekerjaan selesai." }
        require(job.rating == 0) { "Ulasan untuk job ini sudah dikirim." }
        require(rating in 1..5 && text.isNotBlank()) { "Pilih bintang dan tulis ulasanmu." }
        return job.copy(rating = rating, review = text.trim())
    }

    fun pay(job: UserJob, userId: String, method: String): UserJob {
        require(job.requesterId == userId && job.status == JobStatus.COMPLETED) { "Pembayaran tersedia bagi peminta setelah pekerjaan selesai." }
        require(!job.paid) { "Job sudah dibayar." }
        require(method in paymentMethods) { "Pilih metode pembayaran." }
        return job.copy(paid = true, paymentMethod = method)
    }

    val categories = listOf("Kebersihan", "Pengantaran", "Perbaikan", "Jasa Rumah", "Lainnya")
    val paymentMethods = listOf("QRIS (simulasi)", "Transfer Bank (simulasi)", "Tunai (simulasi)")
}
