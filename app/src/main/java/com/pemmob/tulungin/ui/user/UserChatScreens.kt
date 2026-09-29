package com.pemmob.tulungin.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pemmob.tulungin.data.user.*

@Composable
internal fun ChatListScreen(state: UserSnapshot, onChat: (String) -> Unit) {
    UserContent {
        UText("Percakapan", size = 16, weight = FontWeight.Bold, lineHeight = 22)
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

@Composable
internal fun ChatRoomScreen(job: UserJob, conversation: Conversation?, busy: Boolean, onSend: (String, () -> Unit) -> Unit) {
    var draft by rememberSaveable(job.id) { mutableStateOf("") }
    val messages = conversation?.messages.orEmpty()
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 18.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        UText(job.title, size = 13, color = UserSecondary, lineHeight = 21)
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(messages, key = { it.id }) { message ->
                Box(Modifier.fillMaxWidth(0.855f).heightIn(min = 66.dp).background(if (message.outgoing) UserMint else UserSoft, RoundedCornerShape(16.dp)).padding(12.dp)) {
                    UText(message.text, lineHeight = 20)
                }
            }
        }
        UserField("Pesan", draft, { draft = it.take(2000) }, "Tulis pesan...")
        UserButton("Kirim", enabled = !busy && draft.isNotBlank()) { onSend(draft) { draft = "" } }
    }
}
