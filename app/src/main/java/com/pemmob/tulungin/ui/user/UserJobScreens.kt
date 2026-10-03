package com.pemmob.tulungin.ui.user

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.location.Location
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import com.pemmob.tulungin.data.user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@Composable
internal fun JobDetailScreen(
    job: UserJob,
    profile: UserProfile,
    applications: List<UserApplication>,
    busy: Boolean,
    onApply: () -> Unit,
    onSelectApp: (String) -> Unit,
    onCompleteFlow: () -> Unit,
    onChat: () -> Unit,
    onMap: () -> Unit
) {
    var confirm by rememberSaveable(job.id) { mutableStateOf(false) }
    val isRequester = job.requesterId == profile.id
    val myApp = applications.firstOrNull { it.jobId == job.id && it.applicantId == profile.id }
    val isAcceptedHelper = myApp?.status == "accepted" || job.helperId == profile.id
    val jobApplications = applications.filter { it.jobId == job.id }
    val isOngoing = job.status in listOf(JobStatus.ACCEPTED, JobStatus.IN_PROGRESS, JobStatus.AWAITING_CONFIRMATION)

    val context = LocalContext.current
    var helperLat by remember { mutableStateOf<Double?>(null) }
    var helperLng by remember { mutableStateOf<Double?>(null) }
    var distanceText by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            runCatching {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val hLat = location.latitude
                        val hLng = location.longitude
                        helperLat = hLat
                        helperLng = hLng
                        if (job.locationLat != null && job.locationLng != null) {
                            val results = FloatArray(1)
                            Location.distanceBetween(hLat, hLng, job.locationLat, job.locationLng, results)
                            val distKm = results[0] / 1000.0
                            distanceText = String.format(Locale.forLanguageTag("id-ID"), "%.1f km", distKm)
                        }
                    } else {
                        locationError = "Gagal mendapatkan lokasi saat ini."
                    }
                }.addOnFailureListener {
                    locationError = "Gagal mendapatkan lokasi: ${it.message}"
                }
            }
        } else {
            locationError = "Izin lokasi diperlukan untuk menghitung jarak."
        }
    }

    UserContent {
        UText(job.title, size = 16, weight = FontWeight.Bold, lineHeight = 22)
        StatusStrip(job.category)
        Notice("Detail Kebutuhan", job.description)

        if (job.locationLat != null && job.locationLng != null) {
            MapLibreViewerView(job.locationLat, job.locationLng, helperLat, helperLng)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    DetailCard("Lokasi", job.location)
                    if (distanceText != null) {
                        UText("Jarak dari lokasi kamu: $distanceText", size = 14, weight = FontWeight.Bold, color = UserPrimary)
                    } else if (locationError != null) {
                        UText(locationError!!, size = 12, color = Color.Red)
                    }
                }
                if (!isRequester) {
                    Button(
                        onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        colors = ButtonDefaults.buttonColors(containerColor = UserPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        UText("Lokasi Saya", color = Color.White)
                    }
                }
            }
        } else {
            DetailCard("Lokasi", job.location, onMap)
            Notice("Peta", "Lokasi peta belum tersedia untuk job ini.")
        }

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
            if (job.helperId != null) {
                Spacer(Modifier.height(8.dp))
                UserButton("Buka Chat", secondary = true, onClick = onChat)
                if (isOngoing) {
                    Spacer(Modifier.height(8.dp))
                    UserButton("Selesai", onClick = onCompleteFlow)
                }
            }
        } else if (isAcceptedHelper) {
            Notice("Status Lamaran", "Lamaranmu DITERIMA! Job sedang berlangsung.")
            Spacer(Modifier.height(8.dp))
            UserButton("Buka Chat", secondary = true, onClick = onChat)
            if (isOngoing) {
                Spacer(Modifier.height(8.dp))
                UserButton("Selesai", onClick = onCompleteFlow)
            }
        } else {
            if (myApp == null) {
                UserButton("Lamar Job", enabled = !busy && job.status == JobStatus.AVAILABLE && job.helperId == null) { confirm = true }
            } else {
                when (myApp.status) {
                    "pending" -> Notice("Status Lamaran", "Lamaranmu sudah dikirim (Pending). Menunggu pilihan dari peminta job.")
                    "rejected" -> Notice("Status Lamaran", "Lamaranmu ditolak oleh peminta.")
                    else -> {}
                }
            }
        }
    }
    if (confirm) ConfirmDialog("Lamar Job?", "Kamu akan melamar job ${job.title}. Lanjutkan?", "Lamar", { confirm = false }) { confirm = false; onApply() }
}

@Composable
internal fun MapLibreViewerView(
    jobLat: Double?,
    jobLng: Double?,
    helperLat: Double?,
    helperLng: Double?
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, UserOutline, RoundedCornerShape(16.dp)),
        factory = { mapView }
    ) { view ->
        view.getMapAsync { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/liberty") { _ ->
                val targetLat = jobLat ?: -7.4214
                val targetLng = jobLng ?: 109.2312
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(targetLat, targetLng))
                    .zoom(14.0)
                    .build()

                map.clear()
                if (jobLat != null && jobLng != null) {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(jobLat, jobLng))
                            .title("Lokasi Job")
                    )
                }
                if (helperLat != null && helperLng != null) {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(helperLat, helperLng))
                            .title("Lokasi Kamu")
                    )
                }
            }
        }
    }
}

@Composable
internal fun ActiveJobScreen(
    job: UserJob,
    profile: UserProfile,
    busy: Boolean,
    onStart: () -> Unit,
    onChat: () -> Unit,
    onCompleteFlow: () -> Unit,
    onMap: () -> Unit,
    onDemo: () -> Unit
) {
    val requester = job.requesterId == profile.id
    val helper = job.helperId == profile.id
    val isOngoing = job.status in listOf(JobStatus.ACCEPTED, JobStatus.IN_PROGRESS, JobStatus.AWAITING_CONFIRMATION)

    UserContent {
        UText(job.title, size = 16, weight = FontWeight.Bold, lineHeight = 22)
        StatusStrip(job.status.label, job.status == JobStatus.COMPLETED)
        DetailCard("Kategori", job.category)
        DetailCard("Lokasi", job.location, onMap)
        DetailCard("Waktu", job.scheduledAt)
        DetailCard("Upah Jasa", rupiah(job.fee))

        if (helper) {
            UserButton("Perbarui Status", enabled = !busy && job.status in listOf(JobStatus.ACCEPTED, JobStatus.IN_PROGRESS), onClick = onStart)
            UserButton("Buka Chat", secondary = true, onClick = onChat)
            if (isOngoing) {
                Spacer(Modifier.height(8.dp))
                UserButton("Selesai", onClick = onCompleteFlow)
            }
        } else if (requester) {
            if (job.helperId != null) UserButton("Buka Chat", secondary = true, onClick = onChat)
            if (isOngoing) {
                Spacer(Modifier.height(8.dp))
                UserButton("Selesai", onClick = onCompleteFlow)
            }
        }
        if (job.status == JobStatus.AVAILABLE && requester) Notice("Menunggu Penulung", "Permintaanmu sudah diterbitkan. Penulung dapat melamar dan kamu dapat memilihnya.")
        if (job.status == JobStatus.AVAILABLE && requester || job.status == JobStatus.IN_PROGRESS && requester) {
            UserButton("Simulasikan lawan transaksi", secondary = true, onClick = onDemo)
        }
    }
}

@Composable
internal fun JobCompletionScreen(
    job: UserJob,
    profile: UserProfile,
    busy: Boolean,
    onUploadWorkProof: (String, String) -> Unit,
    onUploadPaymentProof: (String, String) -> Unit,
    onConfirmComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val isHelper = job.helperId == profile.id || (job.status in listOf(JobStatus.ACCEPTED, JobStatus.IN_PROGRESS) && job.requesterId != profile.id)
    val isRequester = job.requesterId == profile.id
    val hasWorkProof = !job.proofUri.isNullOrBlank()
    val hasPaymentProof = !job.paymentProofUri.isNullOrBlank()

    var workUri by rememberSaveable(job.id, job.proofUri) { mutableStateOf(job.proofUri ?: "") }
    var workName by rememberSaveable(job.id, job.proofName) { mutableStateOf(job.proofName ?: "Bukti Pekerjaan") }
    var payUri by rememberSaveable(job.id, job.paymentProofUri) { mutableStateOf(job.paymentProofUri ?: "") }
    var payName by rememberSaveable(job.id, job.paymentProofName) { mutableStateOf(job.paymentProofName ?: "Bukti Pembayaran") }

    val context = LocalContext.current
    val workPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selected ->
        if (selected != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                var filename = "Bukti pekerjaan"
                context.contentResolver.query(selected, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) filename = cursor.getString(nameIndex)
                    }
                }
                workUri = selected.toString(); workName = filename
            }.onFailure { onError(it.message ?: "File tidak dapat dibuka.") }
        }
    }

    val payPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selected ->
        if (selected != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                var filename = "Bukti pembayaran"
                context.contentResolver.query(selected, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) filename = cursor.getString(nameIndex)
                    }
                }
                payUri = selected.toString(); payName = filename
            }.onFailure { onError(it.message ?: "File tidak dapat dibuka.") }
        }
    }

    UserContent {
        UText("Konfirmasi Penyelesaian", size = 18, weight = FontWeight.Bold, lineHeight = 24)
        StatusStrip(job.status.label)

        // Helper Section: Work Proof
        UText("Bukti Pekerjaan (dari Penulung)", size = 16, weight = FontWeight.Bold)
        if (isHelper) {
            if (workUri.isBlank()) {
                Box(Modifier.fillMaxWidth().height(140.dp).background(UserSurface, RoundedCornerShape(16.dp)).border(1.dp, UserOutline, RoundedCornerShape(16.dp)).clickable { workPicker.launch(arrayOf("image/*")) }, contentAlignment = Alignment.Center) {
                    UText("＋ Pilih Foto Bukti Pekerjaan", color = UserPrimary, weight = FontWeight.Medium)
                }
            } else {
                ProofPreview(workUri, workName, onError)
                UserButton("Ganti Foto Pekerjaan", secondary = true) { workPicker.launch(arrayOf("image/*")) }
                UserButton("Kirim Foto Pekerjaan", enabled = !busy) { onUploadWorkProof(workUri, workName) }
            }
        } else {
            if (hasWorkProof) {
                ProofPreview(job.proofUri, job.proofName, onError)
            } else {
                Notice("Belum ada bukti", "Penulung belum mengirim bukti pekerjaan.")
            }
        }

        Spacer(Modifier.height(16.dp))
        // Requester Section: Payment Proof
        UText("Bukti Pembayaran (dari Peminta)", size = 16, weight = FontWeight.Bold)
        if (isRequester) {
            if (payUri.isBlank()) {
                Box(Modifier.fillMaxWidth().height(140.dp).background(UserSurface, RoundedCornerShape(16.dp)).border(1.dp, UserOutline, RoundedCornerShape(16.dp)).clickable { payPicker.launch(arrayOf("image/*")) }, contentAlignment = Alignment.Center) {
                    UText("＋ Pilih Foto Bukti Pembayaran", color = UserPrimary, weight = FontWeight.Medium)
                }
            } else {
                ProofPreview(payUri, payName, onError)
                UserButton("Ganti Foto Pembayaran", secondary = true) { payPicker.launch(arrayOf("image/*")) }
                UserButton("Kirim Foto Pembayaran", enabled = !busy) { onUploadPaymentProof(payUri, payName) }
            }
        } else {
            if (hasPaymentProof) {
                ProofPreview(job.paymentProofUri, job.paymentProofName, onError)
            } else {
                Notice("Belum ada bukti", "Peminta belum mengirim bukti pembayaran.")
            }
        }

        Spacer(Modifier.height(16.dp))
        UText("Status Bukti", size = 16, weight = FontWeight.Bold)
        Notice("Bukti Pekerjaan", if (hasWorkProof) "✓ Bukti pekerjaan sudah dikirim" else "✗ Bukti pekerjaan belum dikirim")
        Notice("Bukti Pembayaran", if (hasPaymentProof) "✓ Bukti pembayaran sudah dikirim" else "✗ Bukti pembayaran belum dikirim")

        Spacer(Modifier.height(16.dp))
        val canConfirm = hasWorkProof && hasPaymentProof
        UserButton("Konfirmasi Selesai", enabled = !busy && canConfirm, onClick = onConfirmComplete)
        if (!canConfirm) {
            UText("Tombol akan aktif setelah kedua belah pihak (Helper & Peminta) mengirimkan bukti masing-masing.", size = 12, color = UserSecondary)
        }
    }
}

@Composable
internal fun ProofPreview(uri: String?, name: String?, onError: (String) -> Unit) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            if (uri.isNullOrBlank() || uri.startsWith("demo://")) {
                null
            } else if (uri.startsWith("http://") || uri.startsWith("https://")) {
                runCatching {
                    val url = URL(uri)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val inputStream = connection.inputStream
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }.getOrNull()
            } else {
                runCatching {
                    val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri))
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        val sample = (maxOf(info.size.width, info.size.height) / 1200).coerceAtLeast(1)
                        decoder.setTargetSampleSize(sample)
                    }.asImageBitmap()
                }.getOrNull()
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(UserSurface, RoundedCornerShape(16.dp))
            .border(1.dp, UserOutline, RoundedCornerShape(16.dp))
            .clickable {
                if (!uri.isNullOrBlank() && !uri.startsWith("demo://")) {
                    runCatching {
                        val parsed = Uri.parse(uri)
                        val intent = if (uri.startsWith("http://") || uri.startsWith("https://")) {
                            Intent(Intent.ACTION_VIEW, parsed)
                        } else {
                            Intent(Intent.ACTION_VIEW).setDataAndType(
                                parsed,
                                context.contentResolver.getType(parsed) ?: "application/octet-stream"
                            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        onError("File tidak tersedia atau belum ada aplikasi pembukanya.")
                    }
                }
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap!!, "Bukti", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        } else {
            UText(name ?: "Bukti foto", color = UserPrimary, weight = FontWeight.Medium)
        }
    }
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
            Notice("Penyelesaian", "Kedua bukti telah dikirim dan dikonfirmasi selesai.")
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
