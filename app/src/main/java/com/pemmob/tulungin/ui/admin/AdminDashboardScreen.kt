package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.*
import com.pemmob.tulungin.ui.user.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier,
    onManageUsersClick: () -> Unit = {},
    onManageCategoriesClick: () -> Unit = {},
    onManageJobsClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var totalUsers by remember { mutableStateOf("...") }
    var totalJobs by remember { mutableStateOf("...") }
    var totalCategories by remember { mutableStateOf("...") }
    var activeJobs by remember { mutableStateOf("...") }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").addSnapshotListener { snap, _ ->
            if (snap != null) totalUsers = snap.size().toString()
        }
        db.collection("jobs").addSnapshotListener { snap, _ ->
            if (snap != null) {
                totalJobs = snap.size().toString()
                val active = snap.documents.count {
                    val status = it.getString("status")
                    status == "IN_PROGRESS" || status == "ACCEPTED" || status == "AVAILABLE" || status == "AWAITING_CONFIRMATION"
                }
                activeJobs = active.toString()
            }
        }
        db.collection("categories").addSnapshotListener { snap, _ ->
            if (snap != null) totalCategories = snap.size().toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tulungin_text_logo),
                contentDescription = "tulungin",
                modifier = Modifier.height(28.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(1f))

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = { PlainTooltip { Text("Logout") } },
                state = rememberTooltipState()
            ) {
                IconButton(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout),
                        contentDescription = "Logout",
                        tint = TulunginTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total User",
                value = totalUsers,
                iconRes = R.drawable.ic_users
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Job",
                value = totalJobs,
                iconRes = R.drawable.ic_briefcase
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Kategori",
                value = totalCategories,
                iconRes = R.drawable.ic_category
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Job Aktif",
                value = activeJobs,
                iconRes = R.drawable.ic_clock
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Menu Utama",
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MenuCard(
                modifier = Modifier.weight(1f),
                title = "Kelola User/Akun",
                iconRes = R.drawable.ic_person,
                onClick = onManageUsersClick
            )
            MenuCard(
                modifier = Modifier.weight(1f),
                title = "Kelola Kategori",
                iconRes = R.drawable.ic_tag,
                onClick = onManageCategoriesClick
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MenuCard(
                modifier = Modifier.weight(1f),
                title = "Kelola Job",
                iconRes = R.drawable.ic_briefcase,
                onClick = onManageJobsClick
            )
            MenuCard(
                modifier = Modifier.weight(1f),
                title = "Chat",
                iconRes = R.drawable.ic_chat,
                onClick = onChatClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            "Keluar dari akun?",
            "Sesi Anda akan diakhiri.",
            "Keluar",
            { showLogoutConfirm = false },
            { showLogoutConfirm = false; onLogoutClick() }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    iconRes: Int
) {
    Column(
        modifier = modifier
            .background(TulunginMintSoft, RoundedCornerShape(16.dp))
            .border(1.dp, TulunginInputBorderFocused, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TulunginTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(TulunginMintLight, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = TulunginPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            color = TulunginPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MenuCard(
    modifier: Modifier = Modifier,
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 120.dp)
            .heightIn(min = 120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.5.dp, TulunginMintBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(TulunginPrimary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            color = TulunginTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
