package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.TulunginTheme

@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier,
    onManageUsersClick: () -> Unit = {},
    onManageCategoriesClick: () -> Unit = {},
    onManageJobsClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header Logo
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Metric Statistics Cards (2x2 Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total User",
                value = "1,250",
                iconRes = R.drawable.ic_users
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Job",
                value = "486",
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
                value = "12",
                iconRes = R.drawable.ic_category
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Job Aktif",
                value = "124",
                iconRes = R.drawable.ic_clock
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section 2: "Menu Utama" Title
        Text(
            text = "Menu Utama",
            color = Color(0xFF0F1E24),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Menu Utama Navigation Cards (2x2 Grid)
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
            .background(Color(0xFFEEF3F3), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFD6DFDF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color(0xFF7D8C90),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(0xFFD2F2F2), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color(0xFF0F282F),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            color = Color(0xFF0F282F),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
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
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFDCF4F4), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF0F282F), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = Color(0xFF0F1E24),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardScreenPreview() {
    TulunginTheme {
        AdminDashboardScreen()
    }
}
