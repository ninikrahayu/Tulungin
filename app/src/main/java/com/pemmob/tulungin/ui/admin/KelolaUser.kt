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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.TulunginTheme

data class UserItem(
    val id: Int,
    val name: String,
    val phone: String,
    val isVerified: Boolean
)

@Composable
fun KelolaUser(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onUserClick: (UserItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Mock data pengguna (LazyColumn akan memuat hanya item yang berada di viewport layar saat scroll)
    val userList = remember {
        listOf(
            UserItem(id = 1, name = "Andi Pratama", phone = "081234567890", isVerified = true),
            UserItem(id = 2, name = "Siti Rahma", phone = "08987654321", isVerified = false),
            UserItem(id = 3, name = "Budi Santoso", phone = "08102938475", isVerified = true),
            UserItem(id = 4, name = "Rina Putri", phone = "082345678901", isVerified = false),
            UserItem(id = 5, name = "Dimas Setiawan", phone = "082468013579", isVerified = true),
            UserItem(id = 6, name = "Ahmad Fauzi", phone = "081398765432", isVerified = true),
            UserItem(id = 7, name = "Dewi Lestari", phone = "085612345678", isVerified = false),
            UserItem(id = 8, name = "Eko Prasetyo", phone = "087788990011", isVerified = true),
            UserItem(id = 9, name = "Fitri Handayani", phone = "081911223344", isVerified = false),
            UserItem(id = 10, name = "Gilang Ramadhan", phone = "082155667788", isVerified = true),
            UserItem(id = 11, name = "Hendra Wijaya", phone = "083899001122", isVerified = true),
            UserItem(id = 12, name = "Indah Permata", phone = "085233445566", isVerified = false)
        )
    }

    val filteredUsers = remember(searchQuery) {
        if (searchQuery.isBlank()) userList
        else userList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery)
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
                    tint = Color(0xFF0F1E24),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Kelola User/Akun",
                color = Color(0xFF0F1E24),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Cari pengguna...",
                        color = Color(0xFF8A969B),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color(0xFF7A898E),
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD6DFDF),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedTextColor = Color(0xFF0F1E24),
                    unfocusedTextColor = Color(0xFF0F1E24),
                    cursorColor = Color(0xFF0F282F)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // User List (LazyColumn hanya memuat item yang tampil di viewport layar)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = filteredUsers,
                key = { user -> user.id }
            ) { user ->
                UserCardItem(
                    user = user,
                    onClick = { onUserClick(user) }
                )
            }
        }
    }
}

@Composable
private fun UserCardItem(
    user: UserItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.2.dp, Color(0xFFDCF4F4), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Box
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF0F282F), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Middle Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.name,
                color = Color(0xFF0F1E24),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user.phone,
                color = Color(0xFF7A898E),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Status Badge
            StatusBadge(isVerified = user.isVerified)
        }

        // Right Chevron
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Color(0xFF7A898E),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun StatusBadge(isVerified: Boolean) {
    val bgColor = if (isVerified) Color(0xFFD5F5E3) else Color(0xFFFDE8CD)
    val contentColor = if (isVerified) Color(0xFF1E7E34) else Color(0xFFB45309)
    val dotColor = if (isVerified) Color(0xFF28A745) else Color(0xFFEA580C)
    val text = if (isVerified) "Terverifikasi" else "Belum Diverifikasi"

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KelolaUserPreview() {
    TulunginTheme {
        KelolaUser()
    }
}
