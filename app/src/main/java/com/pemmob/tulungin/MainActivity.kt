package com.pemmob.tulungin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
import com.pemmob.tulungin.ui.theme.TulunginTheme
import com.pemmob.tulungin.ui.user.UserApp
import com.pemmob.tulungin.ui.user.UserViewModel
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        enableEdgeToEdge()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
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
                                    Toast.makeText(this@MainActivity, "Isi email dan password untuk masuk.", Toast.LENGTH_SHORT).show()
                                } else if (email.trim().equals("admin@tulungin.demo", true)) {
                                    backStack = listOf("admin")
                                } else {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                backStack = listOf("user")
                                            } else {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Login gagal: ${task.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }

                                }
                            },
                            onGoogleLoginClick = {
                                signInWithGoogle {
                                    val target = if (auth.currentUser?.email?.trim()?.equals("admin@tulungin.demo", true) == true) "admin" else "user"
                                    backStack = listOf(target)
                                }
                            },
                            onRegisterClick = { navigateTo("register") }
                        )
                    }
                    "user" -> {
                        UserApp(userViewModel) { backStack = listOf("login") }
                    }
                    "register" -> {
                        RegisterScreen(
                            onLoginClick = {
                                navigateBack()
                            },

                            onRegisterClick = { name, email, password, phone, address ->

                                if (password.length < 6) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Password minimal 6 karakter.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@RegisterScreen
                                }

                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val user = auth.currentUser

                                            if (user == null) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "User berhasil dibuat, tapi UID tidak ditemukan.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                return@addOnCompleteListener
                                            }

                                            val userData = hashMapOf(
                                                "name" to name,
                                                "email" to email,
                                                "phone" to phone,
                                                "address" to address,
                                                "photoUrl" to "",
                                                "role" to "user",
                                                "createdAt" to FieldValue.serverTimestamp(),
                                                "updatedAt" to FieldValue.serverTimestamp(),
                                                "verified" to false
                                            )

                                            firestore
                                                .collection("users")
                                                .document(user.uid)
                                                .set(userData)
                                                .addOnSuccessListener {

                                                    // Tetap update state lokal aplikasi lu
                                                    userViewModel.perform {
                                                        updateProfile(
                                                            snapshot.value.profile.copy(
                                                                name = name,
                                                                email = email,
                                                                phone = phone,
                                                                address = address
                                                            )
                                                        )
                                                    }

                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Akun berhasil dibuat.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    backStack = listOf("user")
                                                }
                                                .addOnFailureListener { e ->

                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Gagal menyimpan profil: ${e.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    Log.e(
                                                        "TulunginAuth",
                                                        "Firestore gagal menyimpan user",
                                                        e
                                                    )
                                                }

                                        } else {

                                            Toast.makeText(
                                                this@MainActivity,
                                                "Register gagal: ${task.exception?.message}",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            Log.e(
                                                "TulunginAuth",
                                                "Firebase Auth register gagal",
                                                task.exception
                                            )
                                        }
                                    }
                            },

                            onGoogleSignUpClick = {
                                signInWithGoogle {
                                    backStack = listOf("user")
                                }
                            }
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
                    Toast.makeText(
                        this@MainActivity,
                        "Google Login gagal: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun registerWithEmailPassword(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        if (
            fullName.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            phone.isBlank() ||
            address.isBlank()
        ) {
            Toast.makeText(
                this,
                "Semua field harus diisi",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = auth.currentUser

                    if (user == null) {
                        Toast.makeText(
                            this,
                            "User berhasil dibuat, tapi data user tidak ditemukan",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val userData = hashMapOf(
                        "name" to fullName,
                        "email" to email,
                        "phone" to phone,
                        "address" to address,
                        "photoUrl" to "",
                        "role" to "user",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    firestore
                        .collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {

                            Log.d(
                                "TulunginAuth",
                                "Register + Firestore berhasil: ${user.uid}"
                            )

                            Toast.makeText(
                                this,
                                "Akun berhasil dibuat!",
                                Toast.LENGTH_SHORT
                            ).show()

                            onSuccess()
                        }
                        .addOnFailureListener { e ->

                            Log.e(
                                "TulunginAuth",
                                "Gagal menyimpan user ke Firestore",
                                e
                            )

                            Toast.makeText(
                                this,
                                "Akun dibuat, tetapi gagal menyimpan profil",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                } else {

                    Log.e(
                        "TulunginAuth",
                        "Register gagal",
                        task.exception
                    )

                    Toast.makeText(
                        this,
                        "Register gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
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

                    syncGoogleUser {
                        onSuccess()
                    }
                } else {
                    Log.e(
                        "TulunginAuth",
                        "Firebase login gagal",
                        task.exception
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Firebase Auth Gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun syncGoogleUser(onSuccess: () -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "User Google tidak ditemukan.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val userRef = firestore
            .collection("users")
            .document(user.uid)

        userRef.get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    // User Google sudah pernah terdaftar
                    val name = document.getString("name")
                        ?: user.displayName
                        ?: ""

                    val email = document.getString("email")
                        ?: user.email
                        ?: ""

                    val phone = document.getString("phone")
                        ?: ""

                    val address = document.getString("address")
                        ?: ""

                    userViewModel.perform {
                        updateProfile(
                            snapshot.value.profile.copy(
                                name = name,
                                email = email,
                                phone = phone,
                                address = address
                            )
                        )
                    }

                    Log.d(
                        "TulunginAuth",
                        "Profile Google ditemukan: ${user.uid}"
                    )

                    onSuccess()

                } else {

                    // Google user baru
                    val name = user.displayName ?: ""
                    val email = user.email ?: ""
                    val photoUrl = user.photoUrl?.toString() ?: ""

                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to "",
                        "address" to "",
                        "photoUrl" to photoUrl,
                        "role" to "user",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    userRef.set(userData)
                        .addOnSuccessListener {

                            userViewModel.perform {
                                updateProfile(
                                    snapshot.value.profile.copy(
                                        name = name,
                                        email = email,
                                        phone = "",
                                        address = ""
                                    )
                                )
                            }

                            Log.d(
                                "TulunginAuth",
                                "Profile Google baru dibuat: ${user.uid}"
                            )

                            onSuccess()
                        }
                        .addOnFailureListener { e ->

                            Log.e(
                                "TulunginAuth",
                                "Gagal membuat profile Google",
                                e
                            )

                            Toast.makeText(
                                this,
                                "Gagal menyimpan profile Google.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->

                Log.e(
                    "TulunginAuth",
                    "Gagal membaca profile Google",
                    e
                )

                Toast.makeText(
                    this,
                    "Gagal mengambil data profile.",
                    Toast.LENGTH_LONG
                ).show()
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