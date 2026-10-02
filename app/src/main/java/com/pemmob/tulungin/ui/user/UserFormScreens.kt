package com.pemmob.tulungin.ui.user

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pemmob.tulungin.data.user.*
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CreateRequestScreen(busy: Boolean, onPublish: (JobDraft) -> Unit, onError: (String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var detail by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var latLng by remember { mutableStateOf<LatLng?>(null) }
    var date by rememberSaveable { mutableStateOf("") }
    var fee by rememberSaveable { mutableStateOf("") }
    var chooseCategory by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy · HH.mm", Locale.forLanguageTag("id-ID")) }

    UserContent {
        UserField("Judul Permintaan", title, { title = it.take(100) }, "Contoh: Bantu pindahan kos")
        UserSelector("Kategori", category, "Pilih kategori bantuan") { chooseCategory = true }
        UserField("Detail Kebutuhan", detail, { detail = it.take(2000) }, "Jelaskan bantuan yang dibutuhkan", multiline = true)
        UserField("Lokasi (Alamat)", location, { location = it.take(250) }, "Contoh: Jl. Soedirman No. 12, Cilacap")

        UText("Pilih Titik Lokasi pada Peta", size = 14, weight = FontWeight.SemiBold, color = UserPrimary)
        MapLibrePickerView(latLng) { lat, lng ->
            latLng = LatLng(lat, lng)
        }

        val currentLatLng = latLng
        if (currentLatLng != null) {
            UText("Koordinat dipilih: %.4f, %.4f".format(currentLatLng.latitude, currentLatLng.longitude), size = 12, color = UserSecondary)
        } else {
            UText("Tap pada peta untuk menentukan pin lokasi job.", size = 12, color = UserSecondary)
        }

        UserSelector("Waktu", date, "Pilih tanggal dan waktu") {
            val now = LocalDateTime.now()
            DatePickerDialog(context, { _, year, month, day ->
                TimePickerDialog(context, { _, hour, minute ->
                    val chosen = LocalDateTime.of(year, month + 1, day, hour, minute)
                    if (chosen.isAfter(LocalDateTime.now())) date = chosen.format(formatter) else onError("Pilih waktu yang akan datang.")
                }, now.hour, now.minute, true).show()
            }, now.year, now.monthValue - 1, now.dayOfMonth).apply { datePicker.minDate = System.currentTimeMillis() - 1000 }.show()
        }
        UserField("Upah Jasa", fee, { fee = it.filter(Char::isDigit).take(9) }, "Rp0", keyboard = KeyboardType.Number)
        UserButton("Terbitkan Permintaan", enabled = !busy) {
            val scheduled = runCatching { LocalDateTime.parse(date, formatter) }.getOrNull()
            if (scheduled == null || !scheduled.isAfter(LocalDateTime.now())) {
                onError("Pilih tanggal dan waktu yang akan datang.")
            } else if (location.isBlank()) {
                onError("Lengkapi alamat atau pilih lokasi pada peta.")
            } else {
                onPublish(JobDraft(title, category, detail, location, date, fee.toLongOrNull() ?: 0, latLng?.latitude, latLng?.longitude))
            }
        }
    }
    if (chooseCategory) ChoiceDialog("Kategori Bantuan", JobRules.categories, { chooseCategory = false }) { category = it }
}

@Composable
internal fun MapLibrePickerView(
    selectedLatLng: LatLng?,
    onLocationSelected: (Double, Double) -> Unit
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
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, UserOutline, RoundedCornerShape(16.dp)),
        factory = { mapView }
    ) { view ->
        view.getMapAsync { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                val center = selectedLatLng ?: LatLng(-7.4214, 109.2312)
                map.cameraPosition = CameraPosition.Builder()
                    .target(center)
                    .zoom(14.0)
                    .build()

                if (selectedLatLng != null) {
                    map.clear()
                    map.addMarker(
                        MarkerOptions()
                            .position(selectedLatLng)
                            .title("Lokasi Job")
                    )
                }

                map.addOnMapClickListener { point ->
                    map.clear()
                    map.addMarker(
                        MarkerOptions()
                            .position(point)
                            .title("Lokasi Job")
                    )
                    onLocationSelected(point.latitude, point.longitude)
                    true
                }
            }
        }
    }
}

@Composable
internal fun EditProfileScreen(profile: UserProfile, busy: Boolean, onSave: (UserProfile) -> Unit) {
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var email by rememberSaveable { mutableStateOf(profile.email) }
    var phone by rememberSaveable { mutableStateOf(profile.phone) }
    var address by rememberSaveable { mutableStateOf(profile.address) }
    UserContent {
        UserField("Nama Lengkap", name, { name = it.take(100) })
        UserField("Email", email, { email = it.take(150) }, keyboard = KeyboardType.Email)
        UserField("Nomor HP", phone, { phone = it.take(16) }, keyboard = KeyboardType.Phone)
        UserField("Alamat Lengkap", address, { address = it.take(250) }, multiline = true)
        UserButton("Simpan Perubahan", enabled = !busy) { onSave(profile.copy(name = name, email = email, phone = phone, address = address)) }
    }
}

@Composable
internal fun ReviewScreen(job: UserJob, busy: Boolean, onSubmit: (Int, String) -> Unit) {
    var rating by rememberSaveable(job.id) { mutableIntStateOf(0) }
    var review by rememberSaveable(job.id) { mutableStateOf("") }
    UserContent {
        Notice(job.title, "Bantuan untuk ${job.title.lowercase()} telah selesai.")
        UserCard(Modifier.heightIn(min = 112.dp)) {
            UText("Bagaimana pengalamanmu?", size = 15, weight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                (1..5).forEach { number ->
                    TextButton(onClick = { rating = number }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
                        UText(if (number <= rating) "★" else "☆", size = 32, lineHeight = 40, color = UserPrimary)
                    }
                }
            }
        }
        UserField("Ulasan", review, { review = it.take(1000) }, "Tulis pengalamanmu...", multiline = true)
        UserButton("Kirim Ulasan", enabled = !busy) { onSubmit(rating, review) }
    }
}

@Composable
internal fun PaymentScreen(job: UserJob, busy: Boolean, onPay: (String) -> Unit) {
    var method by rememberSaveable(job.id) { mutableStateOf(job.paymentMethod) }
    var choosing by rememberSaveable { mutableStateOf(false) }
    var confirming by rememberSaveable { mutableStateOf(false) }
    UserContent {
        UText("Ringkasan Pembayaran", size = 16, weight = FontWeight.Bold, lineHeight = 22)
        DetailCard("Job", job.title)
        DetailCard("Upah Jasa", rupiah(job.fee))
        UserSelector("Metode Pembayaran", method, "Pilih metode pembayaran") { if (!job.paid) choosing = true }
        UserButton(if (job.paid) "Pembayaran Selesai" else "Lanjut Bayar", enabled = !busy && !job.paid) { if (method.isBlank()) choosing = true else confirming = true }
        StatusStrip(if (job.paid) "Pembayaran simulasi berhasil" else "Menunggu pembayaran", completed = job.paid)
    }
    if (choosing) ChoiceDialog("Metode Pembayaran", JobRules.paymentMethods, { choosing = false }) { method = it }
    if (confirming) ConfirmDialog("Simulasi Pembayaran", "Selesaikan simulasi ${rupiah(job.fee)} melalui $method? Tidak ada uang yang ditagih atau dipindahkan.", "Simulasikan", { confirming = false }) { confirming = false; onPay(method) }
}

@Composable
internal fun SupportScreen(busy: Boolean, tickets: List<SupportTicket>, onSend: (String, () -> Unit) -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }
    UserContent {
        UText("Customer Service", size = 16, weight = FontWeight.Bold, lineHeight = 22)
        UText("Ada kendala saat menggunakan Tulungin? Hubungi Customer Service.", color = UserSecondary, lineHeight = 21)
        UserField("Pesan Bantuan", message, { message = it.take(2000) }, "Ceritakan kendalamu...", multiline = true)
        UserButton("Kirim Pesan", enabled = !busy) { onSend(message) { message = "" } }
        if (tickets.isNotEmpty()) {
            UText("Riwayat bantuan simulasi", size = 16, weight = FontWeight.Bold)
            tickets.reversed().forEach { Notice(it.id, it.message) }
        }
    }
}
