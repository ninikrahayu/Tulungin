package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.*

data class JobItem(
    val id: String,
    val title: String,
    val category: String,
    val requester: String,
    val status: String,
    val description: String,
    val location: String,
    val scheduledAt: String,
    val fee: Long,
    val helperName: String?
)

@Composable
fun KelolaJob(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onJobClick: (JobItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var jobList by remember { mutableStateOf<List<JobItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("jobs").addSnapshotListener { snapshot, error ->
            loading = false
            if (error != null) {
                errorMessage = error.message
                return@addSnapshotListener
            }
            if (snapshot != null) {
                jobList = snapshot.documents.map { doc ->
                    JobItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "Tanpa Judul",
                        category = doc.getString("category") ?: "Lainnya",
                        requester = doc.getString("requesterName") ?: "Peminta",
                        status = doc.getString("status") ?: "AVAILABLE",
                        description = doc.getString("description") ?: "",
                        location = doc.getString("location") ?: "",
                        scheduledAt = doc.getString("scheduledAt") ?: "",
                        fee = doc.getLong("fee") ?: 0L,
                        helperName = doc.getString("helperName")
                    )
                }
            }
        }
    }

    val filteredJobs = remember(searchQuery, jobList) {
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
                text = "Kelola Job",
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
                    Text(text = "Cari job...", color = TulunginPlaceholder, fontSize = 14.sp)
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
            text = "Daftar Job",
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
                    Text("Memuat data...", color = TulunginTextSecondary, textAlign = TextAlign.Center)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak dapat memuat data: $errorMessage", color = Color.Red, textAlign = TextAlign.Center)
                }
            }
            filteredJobs.isEmpty() -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada data.", color = TulunginTextSecondary, textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
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
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = job.title,
                color = TulunginTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = job.category,
                color = TulunginTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Peminta: ${job.requester}",
                color = TulunginTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Status: ${job.status}",
                color = TulunginPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TulunginPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}
