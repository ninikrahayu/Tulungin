package com.pemmob.tulungin.ui.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pemmob.tulungin.data.user.*
import java.util.Locale

@Composable
internal fun HomeScreen(state: UserSnapshot, onCreate: () -> Unit, onBrowse: () -> Unit, onJob: (UserJob) -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 24.dp)) {
        FigmaAsset("logo", Modifier.size(113.dp, 42.dp), "Tulungin")
        Spacer(Modifier.height(4.dp))
        UText("Halo, ${state.profile.name.substringBefore(' ')}!", size = 20, weight = FontWeight.Bold, lineHeight = 28)
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().heightIn(min = 184.dp).background(UserMint, RoundedCornerShape(16.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            UText("Butuh bantuan?", size = 20, weight = FontWeight.Bold, color = UserPrimary, lineHeight = 28)
            UText("Buat permintaan dan temukan Penulung untuk membantumu.", color = UserPrimary, lineHeight = 20)
            Button(onClick = onCreate, modifier = Modifier.width(204.dp).height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UserPrimary), contentPadding = PaddingValues(12.dp)) { UText("Buat Permintaan", color = Color.White, weight = FontWeight.SemiBold, lineHeight = 20) }
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth().heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            UText("Job Tersedia", size = 18, weight = FontWeight.Bold, lineHeight = 26)
            Box(Modifier.width(90.dp).height(32.dp).clickable(onClick = onBrowse), contentAlignment = Alignment.Center) { UText("Lihat Semua", weight = FontWeight.SemiBold, color = UserPrimary) }
        }
        Spacer(Modifier.height(12.dp))
        val available = state.jobs.filter {
            it.status == JobStatus.AVAILABLE && state.applications.none { app -> app.jobId == it.id && app.applicantId == state.profile.id && app.status == "accepted" }
        }.take(2)
        if (available.isEmpty()) Notice("Belum ada job", "Coba lagi nanti atau buat permintaan bantuanmu.")
        available.forEachIndexed { index, job ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            JobCard(job, compact = true) { onJob(job) }
        }
        Spacer(Modifier.height(52.dp))
        UText("Aktivitas Saya", size = 18, weight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        val active = state.jobs.filter { job ->
            val isRequester = job.requesterId == state.profile.id
            val isAcceptedHelper = state.applications.any { it.jobId == job.id && it.applicantId == state.profile.id && it.status == "accepted" }
            val isHelper = job.helperId == state.profile.id || isAcceptedHelper
            val isOngoingStatus = job.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED)
            (isRequester && isOngoingStatus) || isAcceptedHelper || (isHelper && isOngoingStatus)
        }
        if (active.isEmpty()) Notice("Belum ada aktivitas", "Job yang kamu buat atau ambil akan tampil di sini.")
        active.forEach { job ->
            UserCard(onClick = { onJob(job) }) {
                UText(job.title, size = 15, weight = FontWeight.SemiBold)
                UText(if (job.requesterId == state.profile.id) "Permintaan saya" else "Saya sebagai Penulung", size = 12, color = UserSecondary)
                UText(job.status.label, size = 13, color = UserPrimary, weight = FontWeight.Medium)
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
internal fun JobCard(job: UserJob, compact: Boolean = false, onClick: () -> Unit) {
    UserCard(Modifier.heightIn(min = if (compact) 128.dp else 124.dp), border = if (compact) UserOutline else UserMint, spacing = if (compact) 8.dp else 6.dp, onClick = onClick) {
        UText(job.title, size = 16, weight = FontWeight.SemiBold, lineHeight = 24)
        UText(job.category, size = 13, color = UserSecondary, lineHeight = 18)
        Row(Modifier.fillMaxWidth().heightIn(min = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            if (compact) { FigmaAsset("location", Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)) }
            UText(String.format(Locale.forLanguageTag("id-ID"), "%.1f km", job.distanceKm), Modifier.weight(1f), color = UserSecondary, size = if (compact) 13 else 14)
            UText(rupiah(job.fee), modifier = if (compact) Modifier.fillMaxWidth(0.4f) else Modifier, color = UserPrimary, weight = if (compact) FontWeight.Bold else FontWeight.SemiBold, size = if (compact) 15 else 14)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BrowseJobsScreen(state: UserSnapshot, onJob: (UserJob) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Semua kategori") }
    var distance by rememberSaveable { mutableStateOf("Semua jarak") }
    var filter by rememberSaveable { mutableStateOf(false) }
    val maximumDistance = when (distance) { "Maksimal 2 km" -> 2.0; "Maksimal 5 km" -> 5.0; "Maksimal 10 km" -> 10.0; else -> Double.MAX_VALUE }
    val jobs = state.jobs.filter {
        it.status == JobStatus.AVAILABLE &&
                state.applications.none { app -> app.jobId == it.id && app.applicantId == state.profile.id && app.status == "accepted" } &&
                (query.isBlank() || "${it.title} ${it.category} ${it.location}".contains(query.trim(), true)) &&
                (category == "Semua kategori" || it.category == category) &&
                it.distanceKm <= maximumDistance
    }
    UserContent {
        UserField("Cari Job", query, { query = it }, "Cari pekerjaan...")
        UserButton("Filter Job", onClick = { filter = true })
        UText("Job Tersedia", size = 16, weight = FontWeight.Bold, lineHeight = 22)
        if (jobs.isEmpty()) Notice("Job tidak ditemukan", "Coba kata kunci lain atau reset filter.")
        jobs.forEach { job -> JobCard(job) { onJob(job) } }
    }
    if (filter) {
        var draftCategory by rememberSaveable { mutableStateOf(category) }
        var draftDistance by rememberSaveable { mutableStateOf(distance) }
        var choosing by rememberSaveable { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { filter = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color.White, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), dragHandle = {
            Box(Modifier.fillMaxWidth().padding(start = 24.dp, top = 24.dp, bottom = 14.dp)) { Box(Modifier.size(40.dp, 4.dp).background(UserOutline, RoundedCornerShape(2.dp))) }
        }) {
            Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                UText("Filter Job", size = 16, weight = FontWeight.Bold)
                UserSelector("Kategori", draftCategory.takeUnless { it == "Semua kategori" } ?: "", "Pilih kategori bantuan") { choosing = "category" }
                UserSelector("Jarak", draftDistance.takeUnless { it == "Semua jarak" } ?: "", "Pilih jarak dari lokasimu") { choosing = "distance" }
                UserButton("Terapkan Filter", onClick = { category = draftCategory; distance = draftDistance; filter = false })
                UserButton("Reset Filter", secondary = true, onClick = { category = "Semua kategori"; distance = "Semua jarak"; filter = false })
            }
        }
        if (choosing.isNotBlank()) ChoiceDialog(if (choosing == "category") "Kategori" else "Jarak", if (choosing == "category") listOf("Semua kategori") + JobRules.categories else listOf("Semua jarak", "Maksimal 2 km", "Maksimal 5 km", "Maksimal 10 km"), { choosing = "" }) { if (choosing == "category") draftCategory = it else draftDistance = it }
    }
}

@Composable
internal fun HistoryScreen(state: UserSnapshot, onJob: (UserJob) -> Unit) {
    UserContent {
        UText("Riwayat & Job Berlangsung", size = 16, weight = FontWeight.Bold, lineHeight = 22)
        val jobs = state.jobs.filter { job ->
            val isRequester = job.requesterId == state.profile.id
            val isAcceptedHelper = state.applications.any { it.jobId == job.id && it.applicantId == state.profile.id && it.status == "accepted" }
            val isHelper = job.helperId == state.profile.id || isAcceptedHelper
            val isHistoryStatus = job.status in listOf(JobStatus.IN_PROGRESS, JobStatus.ACCEPTED, JobStatus.AWAITING_CONFIRMATION, JobStatus.COMPLETED, JobStatus.CANCELLED)
            (isRequester && isHistoryStatus) || isAcceptedHelper || (isHelper && isHistoryStatus)
        }
        if (jobs.isEmpty()) Notice("Belum ada riwayat", "Pekerjaan yang sedang dikerjakan, selesai, atau dibatalkan akan tampil di sini.")
        jobs.forEach { job ->
            UserCard(Modifier.heightIn(min = 82.dp), onClick = { onJob(job) }) {
                UText(job.title, size = 15, weight = FontWeight.SemiBold, lineHeight = 22)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    UText(job.scheduledAt.substringBefore(" ·").replace("September", "Sep"), Modifier.weight(1f), color = UserSecondary, weight = FontWeight.Medium)
                    UText(job.status.label, color = UserPrimary, weight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun ProfileScreen(profile: UserProfile, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    UserContent {
        Row(Modifier.fillMaxWidth().heightIn(min = 85.dp).background(UserMint, RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UText(profile.name, weight = FontWeight.SemiBold, color = UserPrimary)
                UText(if (profile.verified) "Akun terverifikasi" else "Menunggu verifikasi admin", size = 13, color = UserPrimary)
            }
            FigmaAsset("avatar", Modifier.size(52.dp))
        }
        DetailCard("Email", profile.email)
        DetailCard("Nomor HP", profile.phone)
        DetailCard("Alamat", profile.address)
        UserButton("Edit Profil", onClick = { onNavigate("edit_profile") })
        Spacer(Modifier.height(12.dp))
        UText("Layanan lainnya", size = 16, weight = FontWeight.Bold)
        listOf("Chat" to "chats", "Bantuan" to "support").forEach { (label, route) ->
            UserCard(onClick = { onNavigate(route) }) { UText(label, color = UserPrimary, weight = FontWeight.SemiBold) }
        }
        UserButton("Keluar", secondary = true, onClick = onLogout)
    }
}
