package com.pemmob.tulungin.ui.user

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pemmob.tulungin.data.user.*
import com.pemmob.tulungin.ui.theme.TulunginTheme

@Composable
fun UserApp(viewModel: UserViewModel, onLogout: () -> Unit) {
    TulunginTheme(darkTheme = false) {
        val state by viewModel.snapshot.collectAsState()
        val busy by viewModel.busy.collectAsState()
        val message by viewModel.message.collectAsState()
        var stack by rememberSaveable { mutableStateOf(listOf("home")) }
        var logout by rememberSaveable { mutableStateOf(false) }
        var statusDialog by rememberSaveable { mutableStateOf(false) }
        val current = stack.last()
        val screen = current.substringBefore('/')
        val id = current.substringAfter('/', "")
        val job = state.jobs.firstOrNull { it.id == id }
        val chat = state.conversations.firstOrNull { it.jobId == id }
        val root = screen in listOf("home", "jobs", "history", "profile")
        val snackbar = remember { SnackbarHostState() }
        val savedScreens = rememberSaveableStateHolder()

        fun navigate(route: String) { if (stack.last() != route) stack = stack + route }
        fun tab(route: String) { stack = listOf(route) }
        fun back() { if (stack.size > 1) stack = stack.dropLast(1) else tab("home") }
        fun openJob(value: UserJob) {
            val route = when {
                value.status in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED) -> "history_detail"
                value.requesterId == state.profile.id && value.status == JobStatus.AWAITING_CONFIRMATION -> "confirm"
                value.status == JobStatus.AVAILABLE && value.requesterId != state.profile.id -> "detail"
                else -> "active"
            }
            navigate("$route/${value.id}")
        }

        BackHandler(enabled = current != "home") { back() }
        LaunchedEffect(message) {
            message?.let {
                snackbar.showSnackbar(it)
                viewModel.clearMessage()
            }
        }
        val title = when (screen) {
            "jobs" -> "Job Available"; "history" -> "Histori"; "profile" -> "Profil"
            "create" -> "Buat Permintaan"; "detail" -> "Detail Job"; "active" -> if (job?.requesterId == state.profile.id) "Detail Permintaan" else "Job Aktif"
            "proof" -> "Bukti Penyelesaian"; "confirm" -> "Detail Permintaan"; "review" -> "Beri Ulasan"
            "history_detail" -> "Detail Histori"; "chats" -> "Chat"; "chat" -> chat?.name ?: if (job?.requesterId == state.profile.id) job?.helperName ?: "Penulung" else job?.requesterName ?: "Chat"
            "map" -> "Lokasi"; "edit_profile" -> "Edit Profil"; "payment" -> "Pembayaran"
            "support" -> "Bantuan"; "demo" -> "Mode Simulasi"; else -> "Tulungin"
        }
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(), containerColor = Color.White, contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = { if (screen != "home") UserHeader(title, if (root) null else ::back) },
            bottomBar = { if (root) UserBottomBar(screen, ::tab, { navigate("create") }) },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).then(if (!root) Modifier.navigationBarsPadding() else Modifier)) {
                savedScreens.SaveableStateProvider(current) {
                    when (screen) {
                        "home" -> HomeScreen(state, { navigate("create") }, { tab("jobs") }, ::openJob)
                        "jobs" -> BrowseJobsScreen(state, ::openJob)
                        "history" -> HistoryScreen(state, ::openJob)
                        "profile" -> ProfileScreen(state.profile, ::navigate) { logout = true }
                        "create" -> CreateRequestScreen(busy, { draft -> viewModel.perform("Permintaan simulasi berhasil diterbitkan.") { val createdId = createJob(draft); stack = listOf("home", "active/$createdId") } }, viewModel::notify)
                        "edit_profile" -> EditProfileScreen(state.profile, busy) { profile -> viewModel.perform("Profil berhasil disimpan.") { updateProfile(profile); back() } }
                        "chats" -> ChatListScreen(state) { navigate("chat/$it") }
                        "support" -> SupportScreen(busy, state.tickets) { text, clear -> viewModel.perform { val ticket = sendSupport(text); clear(); viewModel.notify("Tiket $ticket tersimpan lokal. Belum dikirim ke CS sungguhan.") } }
                        "demo" -> DemoScreen(state, busy, ::openJob, { selected -> viewModel.perform("Aksi lawan transaksi disimulasikan.") { (this as? DemoControls)?.simulateCounterparty(selected.id) ?: error("Mode simulasi tidak tersedia.") } }, { viewModel.perform("Data simulasi direset.") { (this as? DemoControls)?.resetDemo() ?: error("Mode simulasi tidak tersedia."); tab("home") } })
                        else -> {
                            if (job == null) UserContent { Notice("Job tidak ditemukan", "Job mungkin sudah direset. Kembali ke Beranda untuk melanjutkan."); UserButton("Ke Beranda") { tab("home") } }
                            else when (screen) {
                                "detail" -> JobDetailScreen(job, busy, { viewModel.perform("Job berhasil diambil.") { acceptJob(job.id); stack = stack.dropLast(1) + "active/${job.id}" } }, { navigate("map/${job.id}") })
                                "active" -> ActiveJobScreen(job, state.profile, busy, { statusDialog = true }, { navigate("chat/${job.id}") }, { navigate("proof/${job.id}") }, { navigate("confirm/${job.id}") }, { navigate("map/${job.id}") }, { viewModel.perform("Aksi lawan transaksi disimulasikan.") {
                                    (this as? DemoControls)?.simulateCounterparty(job.id) ?: error("Mode simulasi tidak tersedia.")
                                    val updated = this.snapshot.value.jobs.first { it.id == job.id }
                                    if (updated.status == JobStatus.COMPLETED) stack = stack.dropLast(1) + "history_detail/${job.id}"
                                } })
                                "proof" -> ProofScreen(job, busy, { uri, name -> viewModel.perform("Bukti tersimpan lokal. Menunggu konfirmasi peminta.") { submitProof(job.id, uri, name); back() } }, viewModel::notify)
                                "confirm" -> ConfirmCompletionScreen(job, busy, { viewModel.perform("Pekerjaan dikonfirmasi selesai.") { confirmCompletion(job.id); stack = stack.dropLast(1) + "history_detail/${job.id}" } }, viewModel::notify)
                                "history_detail" -> HistoryDetailScreen(job, state.profile, { navigate("review/${job.id}") }, { navigate("payment/${job.id}") }, { navigate("map/${job.id}") })
                                "review" -> ReviewScreen(job, busy) { rating, text -> viewModel.perform("Ulasan berhasil disimpan.") { submitReview(job.id, rating, text); back() } }
                                "payment" -> PaymentScreen(job, busy) { method -> viewModel.perform("Pembayaran simulasi berhasil; tidak ada uang dipindahkan.") { pay(job.id, method) } }
                                "chat" -> ChatRoomScreen(job, chat, busy) { text, clear -> viewModel.perform { sendMessage(job.id, text); clear() } }
                                "map" -> MapScreen(job)
                            }
                        }
                    }
                }
            }
        }
        if (logout) ConfirmDialog("Keluar dari demo?", "Data simulasi tetap tersimpan di perangkat ini.", "Keluar", { logout = false }, onLogout)
        if (statusDialog && job != null) ChoiceDialog("Perbarui Status", if (job.status == JobStatus.ACCEPTED) listOf("Mulai pekerjaan") else listOf("Kirim bukti penyelesaian"), { statusDialog = false }) {
            if (job.status == JobStatus.ACCEPTED) viewModel.perform("Status diperbarui: sedang dikerjakan.") { startJob(job.id) } else navigate("proof/${job.id}")
        }
    }
}

@Composable
private fun DemoScreen(state: UserSnapshot, busy: Boolean, onJob: (UserJob) -> Unit, onSimulate: (UserJob) -> Unit, onReset: () -> Unit) {
    var reset by rememberSaveable { mutableStateOf(false) }
    UserContent {
        Notice("Demo frontend lokal", "Chat, pembayaran, bantuan, dan aksi lawan transaksi memakai data simulasi. Satu akun dapat meminta bantuan dan menjadi Penulung.")
        UText("Uji sebagai peminta", size = 16, weight = FontWeight.Bold)
        UText("Buka contoh permintaan untuk memeriksa bukti, konfirmasi, memberi ulasan, lalu mencoba pembayaran.", color = UserSecondary)
        state.jobs.filter { it.requesterId == state.profile.id && it.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED) }.forEach { job ->
            UserCard(onClick = { onJob(job) }) {
                UText(job.title, weight = FontWeight.SemiBold)
                UText(job.status.label, color = UserSecondary)
                if (job.status in listOf(JobStatus.AVAILABLE, JobStatus.IN_PROGRESS)) UserButton(if (job.status == JobStatus.AVAILABLE) "Simulasikan Penulung mengambil" else "Simulasikan Penulung mengirim bukti", secondary = true, enabled = !busy) { onSimulate(job) }
            }
        }
        UText("Uji sebagai Penulung", size = 16, weight = FontWeight.Bold)
        UText("Ambil job melalui Job Available, mulai pekerjaan, lalu pilih foto atau PDF sebagai bukti. Setelah itu simulasikan peminta mengonfirmasi.", color = UserSecondary)
        state.jobs.filter { it.helperId == state.profile.id && it.status == JobStatus.AWAITING_CONFIRMATION }.forEach { job ->
            UserCard(onClick = { onJob(job) }) {
                UText(job.title, weight = FontWeight.SemiBold)
                UserButton("Simulasikan peminta mengonfirmasi", secondary = true, enabled = !busy) { onSimulate(job) }
            }
        }
        Spacer(Modifier.height(8.dp))
        UserButton("Reset Data Simulasi", secondary = true, enabled = !busy) { reset = true }
    }
    if (reset) ConfirmDialog("Reset data simulasi?", "Job, profil, chat, dan tiket demo akan kembali ke contoh awal. File foto asli di perangkat tidak dihapus.", "Reset", { reset = false }) { reset = false; onReset() }
}
