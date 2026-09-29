package com.pemmob.tulungin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.saveable.rememberSaveable
import com.pemmob.tulungin.ui.user.UserApp
import com.pemmob.tulungin.ui.user.UserViewModel
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.pemmob.tulungin.ui.theme.TulunginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        setContent {
            TulunginTheme {
                var backStack by rememberSaveable { mutableStateOf(listOf("login")) }
                val currentScreen = backStack.lastOrNull() ?: "login"
                val authMessage by userViewModel.message.collectAsState()
                LaunchedEffect(authMessage, currentScreen) {
                    if (currentScreen != "user" && authMessage != null) {
                        Toast.makeText(this@MainActivity, authMessage, Toast.LENGTH_LONG).show()
                        userViewModel.clearMessage()
                    }
                }

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
                    backStack = backStack + screen
                }

                fun navigateBack() {
                    if (backStack.size > 1) {
                        backStack = backStack.dropLast(1)
                    }
                }

                BackHandler(enabled = currentScreen != "user" && (backStack.size > 1 || currentScreen == "admin")) {
                    if (currentScreen == "admin") backStack = listOf("login") else navigateBack()
                }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(this@MainActivity, "Isi email dan password untuk masuk demo.", Toast.LENGTH_SHORT).show()
                                } else {
                                    backStack = listOf(if (email.trim().equals("admin@tulungin.demo", true)) "admin" else "user")
                                    Toast.makeText(this@MainActivity, "Mode simulasi lokal, belum memakai autentikasi backend.", Toast.LENGTH_LONG).show()
                                }
                            },
                            onGoogleLoginClick = { Toast.makeText(this@MainActivity, "Google Sign-In belum terhubung. Gunakan email dan password demo.", Toast.LENGTH_LONG).show() },
                            onRegisterClick = { navigateTo("register") }
                        )
                    }
                    "user" -> {
                        UserApp(userViewModel) { backStack = listOf("login") }
                    }
                    "register" -> {
                        RegisterScreen(
                            onLoginClick = { navigateBack() },
                            onRegisterClick = { name, email, password, phone, address ->
                                if (password.length < 6) Toast.makeText(this@MainActivity, "Password demo minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                                else userViewModel.perform {
                                    updateProfile(snapshot.value.profile.copy(name = name, email = email, phone = phone, address = address))
                                    backStack = listOf("user")
                                }
                            },
                            onGoogleSignUpClick = { Toast.makeText(this@MainActivity, "Pendaftaran Google belum terhubung pada demo.", Toast.LENGTH_LONG).show() }
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
