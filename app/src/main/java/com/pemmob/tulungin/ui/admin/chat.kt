package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.TulunginDivider
import com.pemmob.tulungin.ui.theme.TulunginInputBg
import com.pemmob.tulungin.ui.theme.TulunginInputBorder
import com.pemmob.tulungin.ui.theme.TulunginInputBorderFocused
import com.pemmob.tulungin.ui.theme.TulunginMintBackground
import com.pemmob.tulungin.ui.theme.TulunginMintBorder
import com.pemmob.tulungin.ui.theme.TulunginMintSoft
import com.pemmob.tulungin.ui.theme.TulunginPlaceholder
import com.pemmob.tulungin.ui.theme.TulunginPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextSecondary
import com.pemmob.tulungin.ui.theme.TulunginTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConversationItem(
    val id: Int,
    val name: String,
    val topic: String,
    val lastMessage: String,
    val time: String,
    val hasUnread: Boolean,
    val initial: String = name.firstOrNull()?.uppercase() ?: ""
)

data class ChatMessage(
    val id: Int,
    val message: String,
    val timestamp: String,
    val isFromAdmin: Boolean
)

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedConversation by remember { mutableStateOf<ConversationItem?>(null) }

    val conversationList = remember {
        listOf(
            ConversationItem(
                id = 1,
                name = "Siti Rahma",
                topic = "Bantuan Akun",
                lastMessage = "Saya butuh bantuan terkait akun.",
                time = "10.42",
                hasUnread = true,
                initial = "S"
            ),
            ConversationItem(
                id = 2,
                name = "Andi Pratama",
                topic = "Bantu Pindahan Kos",
                lastMessage = "Terima kasih atas bantuannya.",
                time = "Kemarin",
                hasUnread = false,
                initial = "A"
            )
        )
    }

    val filteredConversations = remember(searchQuery) {
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
        ) {
            // Top Header
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
                    fontWeight = FontWeight.Bold
                )
            }

            // Faint horizontal divider under Top Header
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = TulunginDivider
            )

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Cari percakapan...",
                            color = TulunginPlaceholder,
                            fontSize = 14.sp
                        )
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

            // Section Title: "Daftar Percakapan"
            Text(
                text = "Daftar Percakapan",
                color = TulunginTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp)
            )

            // Conversation List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 24.dp),
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
        // Avatar Circle
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

        Spacer(modifier = Modifier.width(14.dp))

        // Content Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Row 1: Name & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.name,
                    color = TulunginTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = conversation.time,
                    color = TulunginTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Row 2: Topic & Unread Dot Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.topic,
                    color = TulunginPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (conversation.hasUnread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TulunginPrimary, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Row 3: Last Message Snippet
            Text(
                text = conversation.lastMessage,
                color = TulunginTextSecondary,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChatDetailScreen(
    userName: String = "Siti Rahma",
    topic: String = "Bantuan Akun",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = 1,
                    message = "Saya butuh bantuan terkait akun.",
                    timestamp = "10.41",
                    isFromAdmin = false
                ),
                ChatMessage(
                    id = 2,
                    message = "Baik, kami bantu periksa.",
                    timestamp = "10.42",
                    isFromAdmin = true
                )
            )
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        if (inputText.isNotBlank()) {
            val currentTime = SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date())
            messages = messages + ChatMessage(
                id = messages.size + 1,
                message = inputText.trim(),
                timestamp = currentTime,
                isFromAdmin = true
            )
            inputText = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Top Header
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
                fontWeight = FontWeight.Bold
            )
        }

        // Faint horizontal divider under Top Header
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = TulunginDivider
        )

        // Subtitle Context (e.g. "Siti Rahma · Bantuan Akun")
        Text(
            text = "$userName · $topic",
            color = TulunginTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 8.dp)
        )

        // Chat Messages List
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

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Input Box
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = TulunginTextPrimary
                ),
                cursorBrush = SolidColor(TulunginPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(TulunginInputBg, RoundedCornerShape(12.dp))
                    .border(1.dp, TulunginInputBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
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

            // "Kirim" Button
            Button(
                onClick = { sendMessage() },
                modifier = Modifier
                    .height(48.dp)
                    .width(74.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TulunginPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
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
                .widthIn(max = 280.dp)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatPreview() {
    TulunginTheme {
        ChatScreen()
    }
}
