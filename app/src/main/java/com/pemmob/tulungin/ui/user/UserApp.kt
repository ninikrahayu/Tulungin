package com.pemmob.tulungin.ui.user

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pemmob.tulungin.data.user.*
import com.pemmob.tulungin.ui.theme.TulunginTheme
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.launch

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
        val root = screen in listOf("home", "jobs", "history", "profile")
        val snackbar = remember { SnackbarHostState() }
        val savedScreens = rememberSaveableStateHolder()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        fun navigate(route: String) { if (stack.last() != route) stack = stack + route }
        fun tab(route: String) { stack = listOf(route) }
        fun back() { if (stack.size > 1) stack = stack.dropLast(1) else tab("home") }
        fun openJob(value: UserJob) {
            val isAcceptedHelperForMe = state.applications.any { it.jobId == value.id && it.applicantId == state.profile.id && it.status == "accepted" }
            val route = when {
                value.status in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED) -> "history_detail"
                value.requesterId == state.profile.id && value.status == JobStatus.AWAITING_CONFIRMATION -> "confirm"
                value.status == JobStatus.AVAILABLE && !isAcceptedHelperForMe && value.helperId != state.profile.id -> "detail"
                else -> "active"
            }
            navigate("$route/${value.id}")
        }

        fun uploadToCloudinaryAndSubmit(jobId: String, uriString: String, name: String, isWorkProof: Boolean, onSuccessMessage: String) {
            val uri = Uri.parse(uriString)
            if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                viewModel.perform("Mengunggah foto ke Cloudinary...") {
                    runCatching {
                        MediaManager.get().upload(uri)
                            .unsigned("tulungin_preset")
                            .callback(object : UploadCallback {
                                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                                    val secureUrl = resultData?.get("secure_url") as? String ?: uriString
                                    coroutineScope.launch {
                                        if (isWorkProof) viewModel.repository.submitProof(jobId, secureUrl, name)
                                        else viewModel.repository.submitPaymentProof(jobId, secureUrl, name)
                                        viewModel.notify(onSuccessMessage)
                                    }
                                }
                                override fun onStart(requestId: String?) {}
                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                    viewModel.notify("Gagal upload ke Cloudinary: ${error?.description ?: "Coba lagi"}")
                                }
                            })
                            .dispatch()
                    }.onFailure {
                        viewModel.notify("Cloudinary error: ${it.message}")
                    }
                }
            } else {
                viewModel.perform(onSuccessMessage) {
                    if (isWorkProof) submitProof(jobId, uriString, name)
                    else submitPaymentProof(jobId, uriString, name)
                }
            }
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
            "completion" -> "Konfirmasi Penyelesaian"; "review" -> "Beri Ulasan"
            "history_detail" -> "Detail Histori"; "chats" -> "Chat"; "admin_chat" -> "Chat dengan Admin"
            "chat" -> job?.title ?: "Chat Job"
            "map" -> "Lokasi"; "edit_profile" -> "Edit Profil"; "payment" -> "Pembayaran"
            "support" -> "Bantuan"; else -> "Tulungin"
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
                        "create" -> CreateRequestScreen(busy, { draft -> viewModel.perform("Permintaan berhasil diterbitkan.") { val createdId = createJob(draft); stack = listOf("home", "active/$createdId") } }, viewModel::notify)
                        "edit_profile" -> EditProfileScreen(state.profile, busy) { profile -> viewModel.perform("Profil berhasil disimpan.") { updateProfile(profile); back() } }
                        "chats" -> ChatListScreen(state, { navigate("chat/$it") }) { navigate("admin_chat") }
                        "admin_chat" -> UserAdminChatScreen(state.profile, busy, viewModel::notify)
                        "support" -> SupportScreen(busy, state.tickets) { text, clear -> viewModel.perform { val ticket = sendSupport(text); clear(); viewModel.notify("Tiket $ticket tersimpan. Tim CS akan segera menghubungi.") } }
                        else -> {
                            if (job == null) UserContent { Notice("Job tidak ditemukan", "Job mungkin sudah dihapus. Kembali ke Beranda untuk melanjutkan."); UserButton("Ke Beranda") { tab("home") } }
                            else when (screen) {
                                "detail" -> JobDetailScreen(job, state.profile, state.applications, busy, { viewModel.perform("Lamaran berhasil dikirim.") { applyJob(job.id); back() } }, { appId -> viewModel.perform("Penulung berhasil dipilih.") { selectApplication(job.id, appId); stack = stack.dropLast(1) + "active/${job.id}" } }, { navigate("completion/${job.id}") }, { navigate("chat/${job.id}") }, { navigate("map/${job.id}") })
                                "active" -> ActiveJobScreen(job, state.profile, busy, { statusDialog = true }, { navigate("chat/${job.id}") }, { navigate("completion/${job.id}") }, { navigate("map/${job.id}") })
                                "completion" -> JobCompletionScreen(job, state.profile, busy, { uri, name -> uploadToCloudinaryAndSubmit(job.id, uri, name, true, "Bukti pekerjaan berhasil diunggah ke Cloudinary dan disimpan.") }, { uri, name -> uploadToCloudinaryAndSubmit(job.id, uri, name, false, "Bukti pembayaran berhasil diunggah ke Cloudinary dan disimpan.") }, { viewModel.perform("Konfirmasi selesai berhasil disimpan.") { confirmCompletion(job.id); val updated = snapshot.value.jobs.firstOrNull { it.id == job.id }; if (updated?.status == JobStatus.COMPLETED) stack = stack.dropLast(1) + "history_detail/${job.id}" else back() } }, viewModel::notify)
                                "history_detail" -> HistoryDetailScreen(job, state.profile, { navigate("review/${job.id}") }, { navigate("payment/${job.id}") }, { navigate("map/${job.id}") })
                                "review" -> ReviewScreen(job, busy) { rating, text -> viewModel.perform("Ulasan berhasil disimpan.") { submitReview(job.id, rating, text); back() } }
                                "payment" -> PaymentScreen(job, busy) { method -> viewModel.perform("Pembayaran berhasil.") { pay(job.id, method) } }
                                "chat" -> ChatRoomScreen(job, state.profile, busy, viewModel::notify)
                                "map" -> MapScreen(job)
                            }
                        }
                    }
                }
            }
        }
        if (logout) ConfirmDialog("Keluar dari akun?", "Sesi Anda akan diakhiri.", "Keluar", { logout = false }, onLogout)
        if (statusDialog && job != null) ChoiceDialog("Perbarui Status", if (job.status == JobStatus.ACCEPTED) listOf("Mulai pekerjaan") else listOf("Kirim bukti pekerjaan"), { statusDialog = false }) {
            if (job.status == JobStatus.ACCEPTED) viewModel.perform("Status diperbarui: sedang dikerjakan.") { startJob(job.id) } else navigate("completion/${job.id}")
        }
    }
}
