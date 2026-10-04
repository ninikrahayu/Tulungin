package com.pemmob.tulungin.user

import com.pemmob.tulungin.data.user.*
import org.junit.Assert.*
import org.junit.Test

class JobRulesTest {
    private val user = UserProfile(id = "user-1", name = "Test User", verified = true)
    private val job = UserJob(
        id = "job-1",
        title = "Bantu Pindahan Kos",
        category = "Jasa Rumah",
        description = "Membantu memindahkan barang",
        location = "Jl. Kampus",
        scheduledAt = "28 September 2026",
        fee = 50000,
        distanceKm = 1.0,
        requesterId = "requester-1",
        requesterName = "Andi"
    )

    @Test
    fun helperSubmitsWorkProofRequesterSubmitsPaymentProofAndBothConfirm() {
        val accepted = JobRules.accept(job, user)
        val started = JobRules.start(accepted, user.id)
        val workSubmitted = JobRules.submitProof(started, user.id, "content://proof/1", "photo.jpg")
        val paymentSubmitted = JobRules.submitPaymentProof(workSubmitted, job.requesterId, "content://payment/1", "payment.jpg")
        val helperConfirmed = JobRules.confirmJob(paymentSubmitted, user.id)
        val fullyCompleted = JobRules.confirmJob(helperConfirmed, job.requesterId)
        assertEquals(JobStatus.COMPLETED, fullyCompleted.status)
        assertTrue(fullyCompleted.helperConfirmed)
        assertTrue(fullyCompleted.requesterConfirmed)
        assertEquals("content://proof/1", fullyCompleted.proofUri)
        assertEquals("content://payment/1", fullyCompleted.paymentProofUri)
    }

    @Test(expected = IllegalArgumentException::class)
    fun requesterCannotAcceptOwnJob() { JobRules.accept(job.copy(requesterId = user.id), user) }

    @Test(expected = IllegalArgumentException::class)
    fun jobCannotBeAcceptedTwice() { JobRules.accept(JobRules.accept(job, user), user.copy(id = "other")) }

    @Test(expected = IllegalArgumentException::class)
    fun helperCannotConfirmOwnProofWithoutRequesterPaymentProof() {
        val accepted = JobRules.accept(job, user)
        val started = JobRules.start(accepted, user.id)
        val submitted = JobRules.submitProof(started, user.id, "content://proof/1", "photo.jpg")
        JobRules.confirmJob(submitted, user.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun proofRequiresStartedJob() { JobRules.submitProof(JobRules.accept(job, user), user.id, "content://proof/1", "photo.jpg") }

    @Test(expected = IllegalArgumentException::class)
    fun noReviewBeforeCompletion() { JobRules.review(job, job.requesterId, 5, "Baik") }

    @Test(expected = IllegalArgumentException::class)
    fun noDuplicateReview() { JobRules.review(job.copy(status = JobStatus.COMPLETED, rating = 4), job.requesterId, 5, "Baik") }

    @Test(expected = IllegalArgumentException::class)
    fun noPaymentBeforeCompletion() { JobRules.pay(job, job.requesterId, JobRules.paymentMethods.first()) }

    @Test
    fun requesterCanReviewAndPayCompletedJob() {
        val reviewed = JobRules.review(job.copy(status = JobStatus.COMPLETED), job.requesterId, 5, "Sangat membantu")
        val paid = JobRules.pay(reviewed, job.requesterId, JobRules.paymentMethods.first())
        assertEquals(5, paid.rating)
        assertTrue(paid.paid)
    }

    @Test(expected = IllegalArgumentException::class)
    fun noDoublePayment() { JobRules.pay(job.copy(status = JobStatus.COMPLETED, paid = true), job.requesterId, JobRules.paymentMethods.first()) }
}
