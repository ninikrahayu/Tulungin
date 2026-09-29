package com.pemmob.tulungin.user

import com.pemmob.tulungin.data.user.*
import org.junit.Assert.*
import org.junit.Test

class JobRulesTest {
    private val user = UserProfile()
    private val job = DemoFixtures.initial().jobs.first()

    @Test
    fun helperCompletesWorkAndRequesterConfirms() {
        val accepted = JobRules.accept(job, user)
        val started = JobRules.start(accepted, user.id)
        val submitted = JobRules.submitProof(started, user.id, "content://proof/1", "photo.jpg")
        val completed = JobRules.complete(submitted, job.requesterId)
        assertEquals(JobStatus.COMPLETED, completed.status)
        assertEquals("content://proof/1", completed.proofUri)
    }

    @Test(expected = IllegalArgumentException::class)
    fun requesterCannotAcceptOwnJob() { JobRules.accept(job.copy(requesterId = user.id), user) }

    @Test(expected = IllegalArgumentException::class)
    fun jobCannotBeAcceptedTwice() { JobRules.accept(JobRules.accept(job, user), user.copy(id = "other")) }

    @Test(expected = IllegalArgumentException::class)
    fun helperCannotConfirmOwnProof() {
        JobRules.complete(job.copy(helperId = user.id, status = JobStatus.AWAITING_CONFIRMATION, proofUri = "content://proof/1"), user.id)
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
