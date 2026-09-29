package com.pemmob.tulungin

import android.os.Bundle
import android.util.Log
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
import com.pemmob.tulungin.ui.admin.DetailUser
import com.pemmob.tulungin.ui.admin.KelolaKategori
import com.pemmob.tulungin.ui.admin.KelolaUser
import com.pemmob.tulungin.ui.admin.UserItem
import com.pemmob.tulungin.ui.auth.LoginScreen
import com.pemmob.tulungin.ui.auth.RegisterScreen
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableStateListOf
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.pemmob.tulungin.ui.theme.TulunginTheme
import kotlinx.coroutines.launch

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
                            onManageCategoriesClick = { navigateTo("kelola_kategori") }
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
                }
            }
        }
    }
    private fun signInWithGoogle(onSuccess: () -> Unit) {
        lifecycleScope.launch {
            try {
                val signInWithGoogleOption =
                    GetSignInWithGoogleOption.Builder(
                        serverClientId = getString(R.string.default_web_client_id)
                    )
                        .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request
                )

                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    firebaseAuthWithGoogle(
                        idToken = googleIdTokenCredential.idToken,
                        onSuccess = onSuccess
                    )
                } else {
                    Log.e(
                        "TulunginAuth",
                        "Credential bukan Google ID Token: ${credential.type}"
                    )
                }

            } catch (e: Exception) {
                Log.e("TulunginAuth", "Google Sign-In gagal", e)

                runOnUiThread {
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "Google Login gagal: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: () -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    Log.d(
                        "TulunginAuth",
                        "Login berhasil: ${user?.email}"
                    )

                    onSuccess()
                } else {
                    Log.e(
                        "TulunginAuth",
                        "Firebase login gagal",
                        task.exception
                    )
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