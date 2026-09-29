package com.pemmob.tulungin.ui.user

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pemmob.tulungin.data.user.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CreateRequestScreen(busy: Boolean, onPublish: (JobDraft) -> Unit, onError: (String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var detail by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var fee by rememberSaveable { mutableStateOf("") }
    var chooseCategory by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy · HH.mm", Locale.forLanguageTag("id-ID")) }
    UserContent {
        UserField("Judul Permintaan", title, { title = it.take(100) }, "Contoh: Bantu pindahan kos")
        UserSelector("Kategori", category, "Pilih kategori bantuan") { chooseCategory = true }
        UserField("Detail Kebutuhan", detail, { detail = it.take(2000) }, "Jelaskan bantuan yang dibutuhkan", multiline = true)
        UserField("Lokasi", location, { location = it.take(250) }, "Pilih lokasi kebutuhan")
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
            if (scheduled == null || !scheduled.isAfter(LocalDateTime.now())) onError("Pilih tanggal dan waktu yang akan datang.")
            else onPublish(JobDraft(title, category, detail, location, date, fee.toLongOrNull() ?: 0))
        }
    }
    if (chooseCategory) ChoiceDialog("Kategori Bantuan", JobRules.categories, { chooseCategory = false }) { category = it }
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
                    androidx.compose.material3.TextButton(onClick = { rating = number }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(0.dp)) {
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
