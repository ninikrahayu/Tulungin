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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.pemmob.tulungin.ui.theme.TulunginDivider
import com.pemmob.tulungin.ui.theme.TulunginInputBg
import com.pemmob.tulungin.ui.theme.TulunginInputBorder
import com.pemmob.tulungin.ui.theme.TulunginInputBorderFocused
import com.pemmob.tulungin.ui.theme.TulunginMintBorder
import com.pemmob.tulungin.ui.theme.TulunginPlaceholder
import com.pemmob.tulungin.ui.theme.TulunginPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextSecondary
import com.pemmob.tulungin.ui.theme.TulunginTheme

data class JobItem(
    val id: Int,
    val title: String,
    val category: String,
    val requester: String,
    val status: String
)

@Composable
fun KelolaJob(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onJobClick: (JobItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val jobList = remember {
        listOf(
            JobItem(
                id = 1,
                title = "Bantu Pindahan Kos",
                category = "Jasa Rumah",
                requester = "Andi Pratama",
                status = "Aktif"
            ),
            JobItem(
                id = 2,
                title = "Antar Dokumen",
                category = "Pengantaran",
                requester = "Budi Santoso",
                status = "Selesai"
            ),
            JobItem(
                id = 3,
                title = "Bersihkan Halaman",
                category = "Kebersihan",
                requester = "Siti Rahma",
                status = "Dalam Proses"
            )
        )
    }

    val filteredJobs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            jobList
        } else {
            jobList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.requester.contains(searchQuery, ignoreCase = true) ||
                it.status.contains(searchQuery, ignoreCase = true)
            }
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
                text = "Kelola Job",
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
                        text = "Cari job...",
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

        // Section Title: "Daftar Job"
        Text(
            text = "Daftar Job",
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp)
        )

        // Job List (LazyColumn)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = filteredJobs,
                key = { job -> job.id }
            ) { job ->
                JobCardItem(
                    job = job,
                    onClick = { onJobClick(job) }
                )
            }
        }
    }
}

@Composable
private fun JobCardItem(
    job: JobItem,
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Job Details Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = job.title,
                color = TulunginTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = job.category,
                color = TulunginTextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Peminta: ${job.requester}",
                color = TulunginTextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Status: ${job.status}",
                color = TulunginPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Chevron Right Icon
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TulunginPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KelolaJobPreview() {
    TulunginTheme {
        KelolaJob()
    }
}
