package com.pemmob.tulungin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pemmob.tulungin.ui.admin.AdminDashboardScreen
import com.pemmob.tulungin.ui.admin.ChatScreen
import com.pemmob.tulungin.ui.admin.DetailJob
import com.pemmob.tulungin.ui.admin.DetailUser
import com.pemmob.tulungin.ui.admin.JobItem
import com.pemmob.tulungin.ui.admin.KelolaJob
import com.pemmob.tulungin.ui.admin.KelolaKategori
import com.pemmob.tulungin.ui.admin.KelolaUser
import com.pemmob.tulungin.ui.admin.UserItem
import com.pemmob.tulungin.ui.auth.LoginScreen
import com.pemmob.tulungin.ui.auth.RegisterScreen
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableStateListOf
import com.pemmob.tulungin.ui.theme.TulunginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TulunginTheme {
                // Stack riwayat halaman untuk navigasi tombol kembali sistem HP
                val backStack = remember { mutableStateListOf("login") }
                val currentScreen = backStack.lastOrNull() ?: "login"

                var selectedUser by remember {
                    mutableStateOf(
                        UserItem(1, "Andi Pratama", "081234567890", true)
                    )
                }

                var selectedJob by remember {
                    mutableStateOf(
                        JobItem(1, "Bantu Pindahan Kos", "Jasa Rumah", "Andi Pratama", "Sedang dikerjakan")
                    )
                }

                fun navigateTo(screen: String) {
                    backStack.add(screen)
                }

                fun navigateBack() {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.size - 1)
                    }
                }

                // Menangani tombol kembali fisik / gesture sistem bawaan HP
                BackHandler(enabled = backStack.size > 1) {
                    navigateBack()
                }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginClick = { _, _ -> navigateTo("admin") },
                            onRegisterClick = { navigateTo("register") }
                        )
                    }
                    "register" -> {
                        RegisterScreen(
                            onLoginClick = { navigateBack() },
                            onRegisterClick = { _, _, _, _, _ -> navigateBack() }
                        )
                    }
                    "admin" -> {
                        AdminDashboardScreen(
                            onManageUsersClick = { navigateTo("kelola_user") },
                            onManageCategoriesClick = { navigateTo("kelola_kategori") },
                            onManageJobsClick = { navigateTo("kelola_job") },
                            onChatClick = { navigateTo("chat") }
                        )
                    }
                    "kelola_user" -> {
                        KelolaUser(
                            onBackClick = { navigateBack() },
                            onUserClick = { user ->
                                selectedUser = user
                                navigateTo("detail_user")
                            }
                        )
                    }
                    "detail_user" -> {
                        DetailUser(
                            name = selectedUser.name,
                            phone = selectedUser.phone,
                            isVerified = selectedUser.isVerified,
                            onBackClick = { navigateBack() }
                        )
                    }
                    "kelola_kategori" -> {
                        KelolaKategori(
                            onBackClick = { navigateBack() }
                        )
                    }
                    "kelola_job" -> {
                        KelolaJob(
                            onBackClick = { navigateBack() },
                            onJobClick = { job ->
                                selectedJob = job
                                navigateTo("detail_job")
                            }
                        )
                    }
                    "detail_job" -> {
                        DetailJob(
                            jobTitle = selectedJob.title,
                            statusText = selectedJob.status,
                            category = selectedJob.category,
                            requester = selectedJob.requester,
                            onBackClick = { navigateBack() }
                        )
                    }
                    "chat" -> {
                        ChatScreen(
                            onBackClick = { navigateBack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TulunginTheme {
        Greeting("Android")
    }
}