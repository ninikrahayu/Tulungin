package com.pemmob.tulungin.data.user

import org.json.JSONArray
import org.json.JSONObject

internal object SnapshotCodec {
    private fun JSONObject.stringOrNull(key: String) = if (isNull(key)) null else getString(key)
    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> = (0 until length()).map { transform(getJSONObject(it)) }

    fun encode(state: UserSnapshot): String = JSONObject().apply {
        put("schema", 1)
        put("profile", JSONObject().apply {
            put("id", state.profile.id); put("name", state.profile.name); put("email", state.profile.email)
            put("phone", state.profile.phone); put("address", state.profile.address); put("verified", state.profile.verified)
        })
        put("jobs", JSONArray(state.jobs.map { job -> JSONObject().apply {
            put("id", job.id); put("title", job.title); put("category", job.category); put("description", job.description)
            put("location", job.location); put("scheduledAt", job.scheduledAt); put("fee", job.fee); put("distanceKm", job.distanceKm)
            put("requesterId", job.requesterId); put("requesterName", job.requesterName)
            put("helperId", job.helperId ?: JSONObject.NULL); put("helperName", job.helperName ?: JSONObject.NULL)
            put("status", job.status.name); put("proofUri", job.proofUri ?: JSONObject.NULL); put("proofName", job.proofName ?: JSONObject.NULL)
            put("rating", job.rating); put("review", job.review); put("paymentMethod", job.paymentMethod); put("paid", job.paid)
        } }))
        put("applications", JSONArray(state.applications.map { app -> JSONObject().apply {
            put("id", app.id); put("jobId", app.jobId); put("applicantId", app.applicantId)
            put("applicantName", app.applicantName); put("status", app.status); put("createdAt", app.createdAt)
        } }))
        put("conversations", JSONArray(state.conversations.map { chat -> JSONObject().apply {
            put("jobId", chat.jobId); put("name", chat.name)
            put("messages", JSONArray(chat.messages.map { message -> JSONObject().apply {
                put("id", message.id); put("text", message.text); put("outgoing", message.outgoing)
            } }))
        } }))
        put("tickets", JSONArray(state.tickets.map { ticket -> JSONObject().apply { put("id", ticket.id); put("message", ticket.message) } }))
    }.toString()

    fun decode(raw: String): UserSnapshot {
        val root = JSONObject(raw)
        require(root.getInt("schema") == 1)
        val p = root.getJSONObject("profile")
        return UserSnapshot(
            profile = UserProfile(p.getString("id"), p.getString("name"), p.getString("email"), p.getString("phone"), p.getString("address"), p.getBoolean("verified")),
            jobs = root.getJSONArray("jobs").mapObjects { j -> UserJob(
                id = j.getString("id"), title = j.getString("title"), category = j.getString("category"), description = j.getString("description"),
                location = j.getString("location"), scheduledAt = j.getString("scheduledAt"), fee = j.getLong("fee"), distanceKm = j.getDouble("distanceKm"),
                requesterId = j.getString("requesterId"), requesterName = j.getString("requesterName"), helperId = j.stringOrNull("helperId"), helperName = j.stringOrNull("helperName"),
                status = JobStatus.valueOf(j.getString("status")), proofUri = j.stringOrNull("proofUri"), proofName = j.stringOrNull("proofName"),
                rating = j.getInt("rating"), review = j.getString("review"), paymentMethod = j.getString("paymentMethod"), paid = j.getBoolean("paid")
            ) },
            applications = root.optJSONArray("applications")?.mapObjects { a -> UserApplication(
                id = a.getString("id"), jobId = a.getString("jobId"), applicantId = a.getString("applicantId"),
                applicantName = a.getString("applicantName"), status = a.getString("status"), createdAt = a.optLong("createdAt", System.currentTimeMillis())
            ) } ?: emptyList(),
            conversations = root.getJSONArray("conversations").mapObjects { chat -> Conversation(
                chat.getString("jobId"), chat.getString("name"), chat.getJSONArray("messages").mapObjects { ChatMessage(it.getString("id"), it.getString("text"), it.getBoolean("outgoing")) }
            ) },
            tickets = root.getJSONArray("tickets").mapObjects { SupportTicket(it.getString("id"), it.getString("message")) }
        )
    }
}
