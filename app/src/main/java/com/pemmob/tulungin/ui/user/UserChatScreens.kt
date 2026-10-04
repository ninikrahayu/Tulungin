package com.pemmob.tulungin.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pemmob.tulungin.data.user.*

@Composable
internal fun ChatListScreen(state: UserSnapshot, onChat: (String) -> Unit, onAdminChat: () -> Unit) {
    UserContent {
        UText("Percakapan", size = 16, weight = FontWeight.Bold, lineHeight = 22)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(UserMint)
                .border(1.dp, UserPrimary, RoundedCornerShape(16.dp))
                .clickable(onClick = onAdminChat)
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    UText("Pusat Bantuan / Admin", weight = FontWeight.Bold, color = UserPrimary)
                    UText("Hubungi admin jika mengalami kendala", size = 13, color = UserSecondary)
                }
                UText("›", size = 22, color = UserPrimary, weight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))
        UText("Chat Pekerjaan", size = 15, weight = FontWeight.SemiBold)

        if (state.conversations.isEmpty()) Notice("Belum ada percakapan", "Buka chat dari pekerjaan yang sedang berjalan.")
        state.conversations.forEach { chat ->
            val job = state.jobs.firstOrNull { it.id == chat.jobId }
            UserCard(Modifier.heightIn(min = 82.dp), onClick = { onChat(chat.jobId) }) {
                UText("${chat.name} · ${job?.title.orEmpty()}", size = 15, weight = FontWeight.SemiBold, lineHeight = 22)
                Row(Modifier.fillMaxWidth()) {
                    UText(chat.messages.lastOrNull()?.text.orEmpty(), Modifier.weight(1f), color = UserSecondary, weight = FontWeight.Medium)
                    UText("›", color = UserPrimary)
                }
            }
        }
    }
}

data class FirestoreChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderRole: String = "user",
    val senderName: String = "",
    val message: String = "",
    val createdAt: Timestamp? = null
)

@Composable
internal fun UserAdminChatScreen(profile: UserProfile, busy: Boolean, onError: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: profile.id
    val conversationId = "chat_$userId"
    val firestore = FirebaseFirestore.getInstance()

    var messages by remember { mutableStateOf<List<FirestoreChatMessage>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        val convRef = firestore.collection("conversations").document(conversationId)
        convRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val convData = mapOf(
                    "id" to conversationId,
                    "userId" to userId,
                    "userName" to profile.name,
                    "userEmail" to profile.email,
                    "lastMessage" to "Halo, saya butuh bantuan.",
                    "lastMessageAt" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                convRef.set(convData)
            }
        }

        convRef.collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                loading = false
                if (error != null) {
                    onError(error.message ?: "Gagal memuat pesan.")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    messages = snapshot.documents.map { doc ->
                        FirestoreChatMessage(
                            id = doc.id,
                            conversationId = doc.getString("conversationId") ?: conversationId,
                            senderId = doc.getString("senderId") ?: "",
                            senderRole = doc.getString("senderRole") ?: "user",
                            senderName = doc.getString("senderName") ?: "",
                            message = doc.getString("message") ?: "",
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    }
                }
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        if (draft.isBlank()) return
        val text = draft.trim()
        draft = ""

        val msgRef = firestore.collection("conversations").document(conversationId).collection("messages").document()
        val msgData = mapOf(
            "id" to msgRef.id,
            "conversationId" to conversationId,
            "senderId" to userId,
            "senderRole" to "user",
            "senderName" to profile.name,
            "message" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "read" to false
        )

        msgRef.set(msgData).addOnFailureListener {
            onError("Gagal mengirim pesan: ${it.message}")
        }

        firestore.collection("conversations").document(conversationId).update(
            mapOf(
                "lastMessage" to text,
                "lastMessageAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 18.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        UText("Chat dengan Admin Tulungin", size = 16, weight = FontWeight.Bold)
        if (loading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                UText("Memuat chat...", color = UserSecondary)
            }
        } else if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Notice("Belum ada pesan", "Kirim pesan untuk memulai percakapan dengan admin.")
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(messages, key = { it.id }) { message ->
                    val isUser = message.senderRole == "user"
                    Box(Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                        Box(Modifier.fillMaxWidth(0.855f).background(if (isUser) UserMint else UserSoft, RoundedCornerShape(16.dp)).padding(12.dp)) {
                            Column {
                                UText(if (isUser) "Anda" else message.senderName, size = 11, color = UserPrimary, weight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                UText(message.message, lineHeight = 20)
                            }
                        }
                    }
                }
            }
        }
        UserField("Pesan", draft, { draft = it.take(2000) }, "Tulis pesan untuk admin...")
        UserButton("Kirim", enabled = !busy && draft.isNotBlank()) { sendMessage() }
    }
}

@Composable
internal fun ChatRoomScreen(job: UserJob, profile: UserProfile, busy: Boolean, onError: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: profile.id
    val senderName = profile.name.ifBlank { "Pengguna" }
    val firestore = FirebaseFirestore.getInstance()

    var messages by remember { mutableStateOf<List<FirestoreChatMessage>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(job.id) {
        firestore.collection("jobs").document(job.id).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                loading = false
                if (error != null) {
                    onError(error.message ?: "Gagal memuat chat.")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    messages = snapshot.documents.map { doc ->
                        FirestoreChatMessage(
                            id = doc.id,
                            conversationId = job.id,
                            senderId = doc.getString("senderId") ?: "",
                            senderRole = if (doc.getString("senderId") == job.requesterId) "requester" else "helper",
                            senderName = doc.getString("senderName") ?: "",
                            message = doc.getString("message") ?: "",
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    }
                }
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        if (draft.isBlank()) return
        val text = draft.trim()
        draft = ""

        val msgRef = firestore.collection("jobs").document(job.id).collection("messages").document()
        val msgData = mapOf(
            "id" to msgRef.id,
            "jobId" to job.id,
            "senderId" to currentUserId,
            "senderName" to senderName,
            "message" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )

        msgRef.set(msgData).addOnFailureListener {
            onError("Gagal mengirim pesan: ${it.message}")
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 18.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        UText(job.title, size = 15, weight = FontWeight.Bold, lineHeight = 22)
        UText("Chat antara Peminta & Penulung", size = 12, color = UserSecondary)

        if (loading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                UText("Memuat chat...", color = UserSecondary)
            }
        } else if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Notice("Belum ada pesan", "Kirim pesan untuk memulai diskusi mengenai job ini.")
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(messages, key = { it.id }) { message ->
                    val isOutgoing = message.senderId == currentUserId
                    Box(Modifier.fillMaxWidth(), contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart) {
                        Box(Modifier.fillMaxWidth(0.855f).background(if (isOutgoing) UserMint else UserSoft, RoundedCornerShape(16.dp)).padding(12.dp)) {
                            Column {
                                UText(if (isOutgoing) "Anda" else message.senderName, size = 11, color = UserPrimary, weight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                UText(message.message, lineHeight = 20)
                            }
                        }
                    }
                }
            }
        }
        UserField("Pesan", draft, { draft = it.take(2000) }, "Tulis pesan...")
        UserButton("Kirim", enabled = !busy && draft.isNotBlank()) { sendMessage() }
    }
}
