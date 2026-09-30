package com.pemmob.tulungin.ui.user

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pemmob.tulungin.data.user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun JobDetailScreen(
    job: UserJob,
    profile: UserProfile,
    applications: List<UserApplication>,
    busy: Boolean,
    onApply: () -> Unit,
    onSelectApp: (String) -> Unit,
    onMap: () -> Unit
) {
    var confirm by rememberSaveable(job.id) { mutableStateOf(false) }
    val isRequester = job.requesterId == profile.id
    val myApp = applications.firstOrNull { it.jobId == job.id && it.applicantId == profile.id }
    val jobApplications = applications.filter { it.jobId == job.id }

    UserContent {
        UText(job.title, size = 16, weight = FontWeight.Bold, lineHeight = 22)
        StatusStrip(job.category)
        Notice("Detail Kebutuhan", job.description)
        DetailCard("Lokasi", job.location, onMap)
        DetailCard("Waktu", job.scheduledAt)
        DetailCard("Upah Jasa", rupiah(job.fee))

        if (isRequester) {
            Spacer(Modifier.height(8.dp))
            UText("Daftar Pelamar (${jobApplications.size})", size = 16, weight = FontWeight.Bold)
            if (jobApplications.isEmpty()) {
                Notice("Belum ada pelamar", "Belum ada Penulung yang melamar job ini.")
            } else {
                jobApplications.forEach { app ->
                    UserCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                UText(app.applicantName, weight = FontWeight.SemiBold)
                                UText("Status: ${app.status}", size = 13, color = if (app.status == "accepted") UserPrimary else UserSecondary)
                            }
                            if (job.status == JobStatus.AVAILABLE && job.helperId == null && app.status == "pending") {
                                Button(
                                    onClick = { onSelectApp(app.id) },
                                    enabled = !busy,
                                    colors = ButtonDefaults.buttonColors(containerColor = UserPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    UText("Terima", color = Color.White, weight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        } else {
            if (myApp == null) {
                UserButton("Lamar Job", enabled = !busy && job.status == JobStatus.AVAILABLE && job.helperId == null) { confirm = true }
            } else {
                when (myApp.status) {
                    "pending" -> Notice("Status Lamaran", "Lamaranmu sudah dikirim (Pending). Menunggu pilihan dari peminta job.")
                    "accepted" -> Notice("Status Lamaran", "Lamaranmu DITERIMA! Job sedang berlangsung.")
                    "rejected" -> Notice("Status Lamaran", "Lamaranmu ditolak oleh peminta.")
                }
            }
        }
    }
    if (confirm) ConfirmDialog("Lamar Job?", "Kamu akan melamar job ${job.title}. Lanjutkan?", "Lamar", { confirm = false }) { confirm = false; onApply() }
}

@Composable
internal fun ActiveJobScreen(job: UserJob, profile: UserProfile, busy: Boolean, onStart: () -> Unit, onChat: () -> Unit, onProof: () -> Unit, onConfirm: () -> Unit, onMap: () -> Unit, onDemo: () -> Unit) {
    val requester = job.requesterId == profile.id
    UserContent {
        UText(job.title, size = 16, weight = FontWeight.Bold, lineHeight = 22)
        StatusStrip(job.status.label, job.status == JobStatus.COMPLETED)
        DetailCard("Kategori", job.category)
        DetailCard("Lokasi", job.location, onMap)
        DetailCard("Waktu", job.scheduledAt)
        DetailCard("Upah Jasa", rupiah(job.fee))
        if (!requester) {
            UserButton("Perbarui Status", enabled = !busy && job.status in listOf(JobStatus.ACCEPTED, JobStatus.IN_PROGRESS), onClick = onStart)
            UserButton("Buka Chat", secondary = true, onClick = onChat)
            UserButton("Unggah Bukti Penyelesaian", enabled = !busy && job.status == JobStatus.IN_PROGRESS, onClick = onProof)
        } else {
            if (job.helperId != null) UserButton("Buka Chat", secondary = true, onClick = onChat)
            if (job.status == JobStatus.AWAITING_CONFIRMATION) UserButton("Periksa Bukti Penyelesaian", onClick = onConfirm)
        }
        if (job.status == JobStatus.AVAILABLE && requester) Notice("Menunggu Penulung", "Permintaanmu sudah diterbitkan. Penulung dapat melamar dan kamu dapat memilihnya.")
        if (!requester && job.status == JobStatus.AWAITING_CONFIRMATION) Notice("Bukti terkirim", "Menunggu peminta memeriksa dan mengonfirmasi pekerjaan.")
        if (job.status == JobStatus.AVAILABLE && requester || job.status == JobStatus.IN_PROGRESS && requester || job.status == JobStatus.AWAITING_CONFIRMATION && !requester) {
            UserButton("Simulasikan lawan transaksi", secondary = true, onClick = onDemo)
        }
    }
}

@Composable
internal fun ProofScreen(job: UserJob, busy: Boolean, onSubmit: (String, String) -> Unit, onError: (String) -> Unit) {
    var uri by rememberSaveable(job.id) { mutableStateOf("") }
    var name by rememberSaveable(job.id) { mutableStateOf("") }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selected ->
        if (selected != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                var filename = "Bukti penyelesaian"
                context.contentResolver.query(selected, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) filename = cursor.getString(nameIndex)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) require(cursor.getLong(sizeIndex) <= 20 * 1024 * 1024) { "Ukuran bukti maksimal 20 MB." }
                    }
                }
                uri = selected.toString(); name = filename
            }.onFailure { onError(it.message ?: "File tidak dapat dibuka.") }
        }
    }
    UserContent {
        Notice(job.title, "Unggah bukti setelah bantuan selesai dikerjakan.")
        if (uri.isBlank()) {
            Box(Modifier.fillMaxWidth().height(150.dp).background(UserSurface, RoundedCornerShape(16.dp)).border(1.dp, UserOutline, RoundedCornerShape(16.dp)).clickable { picker.launch(arrayOf("image/*", "application/pdf")) }, contentAlignment = Alignment.Center) {
                UText("＋  Pilih foto/file bukti", color = UserPrimary, weight = FontWeight.Medium)
            }
        } else {
            ProofPreview(uri, name, onError)
            UserButton("Ganti foto/file", secondary = true) { picker.launch(arrayOf("image/*", "application/pdf")) }
        }
        UText("Pastikan foto atau file menunjukkan pekerjaan yang telah selesai.", color = UserSecondary, lineHeight = 21)
        UserButton("Kirim Bukti", enabled = !busy) { onSubmit(uri, name) }
    }
}

@Composable
internal fun ProofPreview(uri: String?, name: String?, onError: (String) -> Unit) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            if (uri.isNullOrBlank() || uri.startsWith("demo://")) null else runCatching {
                val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri))
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val sample = (maxOf(info.size.width, info.size.height) / 1200).coerceAtLeast(1)
                    decoder.setTargetSampleSize(sample)
                }.asImageBitmap()
            }.getOrNull()
        }
    }
    Box(Modifier.fillMaxWidth().height(150.dp).background(UserSurface, RoundedCornerShape(16.dp)).border(1.dp, UserOutline, RoundedCornerShape(16.dp)).clickable {
        if (!uri.isNullOrBlank() && !uri.startsWith("demo://")) runCatching {
            val parsed = Uri.parse(uri)
            context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(parsed, context.contentResolver.getType(parsed) ?: "application/octet-stream").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        }.onFailure { onError("File tidak tersedia atau belum ada aplikasi pembukanya. Pilih kembali file bukti.") }
    }.padding(12.dp), contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap!!, "Bukti penyelesaian", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        else UText(name ?: "Bukti foto pekerjaan", color = UserPrimary, weight = FontWeight.Medium)
    }
}

@Composable
internal fun ConfirmCompletionScreen(job: UserJob, busy: Boolean, onConfirm: () -> Unit, onError: (String) -> Unit) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    UserContent {
        UText(job.title, size = 16, weight = FontWeight.Bold, lineHeight = 22)
        StatusStrip(job.status.label)
        Notice("Bukti dari Penulung", "Foto penyelesaian telah dikirim untuk diperiksa.")
        ProofPreview(job.proofUri, job.proofName, onError)
        UserButton("Konfirmasi Selesai", enabled = !busy && job.status == JobStatus.AWAITING_CONFIRMATION) { confirm = true }
    }
    if (confirm) ConfirmDialog("Konfirmasi selesai?", "Pastikan hasil pekerjaan sudah sesuai dengan permintaanmu.", "Selesai", { confirm = false }) { confirm = false; onConfirm() }
}

@Composable
internal fun HistoryDetailScreen(job: UserJob, profile: UserProfile, onReview: () -> Unit, onPay: () -> Unit, onMap: () -> Unit) {
    UserContent {
        UText(job.title, size = 16, weight = FontWeight.Bold, lineHeight = 22)
        StatusStrip(job.status.label, job.status == JobStatus.COMPLETED, job.status == JobStatus.CANCELLED)
        DetailCard("Kategori", job.category)
        DetailCard("Lokasi", job.location, onMap)
        DetailCard("Waktu", job.scheduledAt)
        DetailCard("Upah Jasa", rupiah(job.fee))
        if (job.status == JobStatus.COMPLETED) {
            Notice("Penyelesaian", "Bukti telah dikirim dan pekerjaan dikonfirmasi selesai.")
            if (job.rating > 0) Notice("Ulasan", "${"★".repeat(job.rating)}${"☆".repeat(5 - job.rating)}  ${job.review}")
            else if (job.requesterId == profile.id) UserButton("Beri Ulasan", onClick = onReview)
            if (job.requesterId == profile.id && !job.paid) UserButton("Pembayaran", onClick = onPay)
            if (job.paid) UText("Sudah dibayar · ${job.paymentMethod.ifBlank { "Simulasi" }}", size = 12, color = UserSecondary)
        } else Notice("Permintaan", "Status job: ${job.status.label}")
    }
}

@Composable
internal fun MapScreen(job: UserJob) {
    UserContent {
        BoxWithConstraints(Modifier.fillMaxWidth().height(485.dp).background(UserSoft, RoundedCornerShape(16.dp)).border(1.dp, UserOutline, RoundedCornerShape(16.dp))) {
            val ratio = maxWidth / 345.dp
            Canvas(Modifier.fillMaxSize()) {
                val sx = size.width / 345f
                val sy = size.height / 485f
                listOf(floatArrayOf(19f,59f,305f,12f), floatArrayOf(39f,149f,260f,10f), floatArrayOf(0f,249f,345f,12f), floatArrayOf(69f,354f,250f,12f), floatArrayOf(99f,0f,12f,485f), floatArrayOf(239f,0f,11f,485f)).forEach { r -> drawRect(Color.White, Offset(r[0] * sx, r[1] * sy), Size(r[2] * sx, r[3] * sy)) }
            }
            FigmaAsset("map_marker", Modifier.offset(x = 169.dp * ratio, y = 189.dp).size(36.dp, 44.dp), "Lokasi job pada peta simulasi")
        }
        Notice("Lokasi Peminta", job.location)
        UText("Peta ilustrasi simulasi; belum menggunakan GPS atau rute langsung.", size = 12, color = UserSecondary)
    }
}
