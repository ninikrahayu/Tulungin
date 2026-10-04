package com.pemmob.tulungin.data.user

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for Tulungin user and job data operations backed by Cloud Firestore and Cloudinary.
 */
interface UserRepository {
    val snapshot: StateFlow<UserSnapshot>
    suspend fun createJob(draft: JobDraft): String
    suspend fun applyJob(jobId: String)
    suspend fun selectApplication(jobId: String, applicationId: String)
    suspend fun acceptJob(id: String)
    suspend fun startJob(id: String)
    suspend fun submitProof(id: String, uri: String, name: String)
    suspend fun submitPaymentProof(id: String, uri: String, name: String)
    suspend fun confirmCompletion(id: String)
    suspend fun submitReview(id: String, rating: Int, text: String)
    suspend fun pay(id: String, method: String)
    suspend fun updateProfile(profile: UserProfile)
    suspend fun sendMessage(jobId: String, text: String)
    suspend fun sendSupport(message: String): String
}
