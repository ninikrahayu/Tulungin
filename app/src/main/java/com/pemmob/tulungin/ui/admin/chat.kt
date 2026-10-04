package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class ConversationItem(
    val id: String,
    val userId: String,
    val name: String,
    val topic: String,
    val lastMessage: String,
    val time: String,
    val initial: String = name.firstOrNull()?.uppercase() ?: "U"
)

data class ChatMessage(
    val id: String,
    val message: String,
    val timestamp: String,
    val isFromAdmin: Boolean,
    val senderName: String
)

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedConversation by remember { mutableStateOf<ConversationItem?>(null) }
    var conversationList by remember { mutableStateOf<List<ConversationItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("conversations")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                loading = false
                if (error != null) {
                    errorMessage = error.message
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val timeFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
                    conversationList = snapshot.documents.map { doc ->
                        val updatedAt = doc.getTimestamp("updatedAt")?.toDate() ?: Date()
                        ConversationItem(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            name = doc.getString("userName") ?: "Pengguna",
                            topic = doc.getString("userEmail") ?: "Bantuan User",
                            lastMessage = doc.getString("lastMessage") ?: "Belum ada pesan",
                            time = timeFormat.format(updatedAt),
                            initial = (doc.getString("userName") ?: "U").firstOrNull()?.uppercase() ?: "U"
                        )
                    }
                }
            }
    }

    val filteredConversations = remember(searchQuery, conversationList) {
        if (searchQuery.isBlank()) {
            conversationList
        } else {
            conversationList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.topic.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (selectedConversation != null) {
        val currentConv = selectedConversation!!
        ChatDetailScreen(
            conversationId = currentConv.id,
            userName = currentConv.name,
            topic = currentConv.topic,
            onBackClick = { selectedConversation = null }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Kembali",
                        tint = TulunginTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Chat",
                    color = TulunginTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = TulunginDivider
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(text = "Cari percakapan...", color = TulunginPlaceholder, fontSize = 14.sp)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TulunginInputBorderFocused,
                        unfocusedBorderColor = TulunginInputBorder,
                        focusedContainerColor = TulunginInputBg,
                        unfocusedContainerColor = TulunginInputBg,
                        focusedTextColor = TulunginTextPrimary,
                        unfocusedTextColor = TulunginTextPrimary,
                        cursorColor = TulunginPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Daftar Percakapan",
                color = TulunginTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)
            )

            when {
                loading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Memuat percakapan...", color = TulunginTextSecondary, textAlign = TextAlign.Center)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Gagal memuat chat: $errorMessage", color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
                filteredConversations.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada percakapan.", color = TulunginTextSecondary, textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = filteredConversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationCardItem(
                                conversation = conversation,
                                onClick = { selectedConversation = conversation }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationCardItem(
    conversation: ConversationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.2.dp, TulunginMintBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(TulunginPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.initial,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.name,
                    color = TulunginTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = conversation.time,
                    color = TulunginTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = conversation.topic,
                color = TulunginPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = conversation.lastMessage,
                color = TulunginTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ChatDetailScreen(
    conversationId: String,
    userName: String = "Pengguna",
    topic: String = "Bantuan",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val auth = FirebaseAuth.getInstance()
    val adminId = auth.currentUser?.uid ?: "admin"

    LaunchedEffect(conversationId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                loading = false
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val timeFormat = SimpleDateFormat("HH.mm", Locale.getDefault())
                    messages = snapshot.documents.map { doc ->
                        val date = doc.getTimestamp("createdAt")?.toDate() ?: Date()
                        val role = doc.getString("senderRole") ?: "user"
                        ChatMessage(
                            id = doc.id,
                            message = doc.getString("message") ?: "",
                            timestamp = timeFormat.format(date),
                            isFromAdmin = role == "admin",
                            senderName = doc.getString("senderName") ?: ""
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
        if (inputText.isBlank()) return
        val text = inputText.trim()
        inputText = ""

        val db = FirebaseFirestore.getInstance()
        val msgRef = db.collection("conversations").document(conversationId).collection("messages").document()
        val msgData = mapOf(
            "id" to msgRef.id,
            "conversationId" to conversationId,
            "senderId" to adminId,
            "senderRole" to "admin",
            "senderName" to "Admin Tulungin",
            "message" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "read" to false
        )

        msgRef.set(msgData)
        db.collection("conversations").document(conversationId).update(
            mapOf(
                "lastMessage" to text,
                "lastMessageAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Kembali",
                    tint = TulunginTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = userName,
                color = TulunginTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = TulunginDivider
        )

        Text(
            text = "$userName · $topic",
            color = TulunginTextSecondary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 8.dp)
        )

        if (loading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Memuat pesan...", color = TulunginTextSecondary, textAlign = TextAlign.Center)
            }
        } else if (messages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada pesan.", color = TulunginTextSecondary, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    ChatBubbleItem(message = message)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                maxLines = 4,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = TulunginTextPrimary,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(TulunginPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 110.dp)
                    .background(TulunginInputBg, RoundedCornerShape(12.dp))
                    .border(1.dp, TulunginInputBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Tulis pesan...",
                                color = TulunginPlaceholder,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = { sendMessage() },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .widthIn(min = 72.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TulunginPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Kirim",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isFromAdmin = message.isFromAdmin
    val bubbleBg = if (isFromAdmin) TulunginMintBackground else TulunginMintSoft

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromAdmin) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .wrapContentWidth(if (isFromAdmin) Alignment.End else Alignment.Start)
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleBg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.message,
                color = TulunginTextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.timestamp,
                color = TulunginTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun Chat(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    ChatScreen(
        modifier = modifier,
        onBackClick = onBackClick
    )
}
