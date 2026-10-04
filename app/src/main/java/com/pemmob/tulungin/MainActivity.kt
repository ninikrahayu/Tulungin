package com.pemmob.tulungin

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.pemmob.tulungin.ui.user.rupiah
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.cloudinary.android.MediaManager
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            MapLibre.getInstance(this)
        }

        runCatching {
            MediaManager.init(this, mapOf(
                "cloud_name" to "tulungin-cloud",
                "api_key" to "123456789012345"
            ))
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        enableEdgeToEdge()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        setContent {
            TulunginTheme {
                var backStack by rememberSaveable { mutableStateOf(listOf("login")) }
                val currentScreen = backStack.lastOrNull() ?: "login"
                var completingGoogleProfile by rememberSaveable {
                    mutableStateOf(false)
                }
                val authMessage by userViewModel.message.collectAsState()

                LaunchedEffect(authMessage, currentScreen) {
                    if (currentScreen != "user" && authMessage != null) {
                        Toast.makeText(this@MainActivity, authMessage, Toast.LENGTH_LONG).show()
                        userViewModel.clearMessage()
                    }
                }

                var selectedUser by remember {
                    mutableStateOf(
                        UserItem("", "", "", "", "", false)
                    )
                }

                var selectedJob by remember {
                    mutableStateOf(
                        JobItem("", "", "", "", "", "", "", "", 0L, null)
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
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                checkAndEnsureAdmin(auth.currentUser?.uid) {
                                                    backStack = listOf("admin")
                                                }
                                            } else {
                                                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task2 ->
                                                    if (task2.isSuccessful) {
                                                        checkAndEnsureAdmin(auth.currentUser?.uid) {
                                                            backStack = listOf("admin")
                                                        }
                                                    } else {
                                                        auth.signInAnonymously().addOnCompleteListener {
                                                            checkAndEnsureAdmin(auth.currentUser?.uid) {
                                                                backStack = listOf("admin")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                } else {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                syncEmailUser {
                                                    backStack = listOf("user")
                                                }
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
                                signInWithGoogle(
                                    onSuccess = {
                                        val target =
                                            if (
                                                auth.currentUser?.email
                                                    ?.trim()
                                                    ?.equals("admin@tulungin.demo", true) == true
                                            ) {
                                                "admin"
                                            } else {
                                                "user"
                                            }

                                        backStack = listOf(target)
                                    },
                                    onIncomplete = {
                                        completingGoogleProfile = true
                                        backStack = listOf("register")
                                    }
                                )
                            },
                            onRegisterClick = { navigateTo("register") }
                        )
                    }
                    "user" -> {
                        UserApp(userViewModel) {
                            auth.signOut()
                            backStack = listOf("login")
                        }
                    }
                    "register" -> {
                        RegisterScreen(
                            onRegisterClick = { name, email, password, phone, address ->
                                registerWithEmailPassword(name, email, password, phone, address) {
                                    backStack = listOf("user")
                                }
                            },
                            onGoogleSignUpClick = {
                                signInWithGoogle(
                                    onSuccess = {
                                        backStack = listOf("user")
                                    },
                                    onIncomplete = {
                                        completingGoogleProfile = true
                                        backStack = listOf("register")
                                    }
                                )
                            },
                            onLoginClick = {
                                navigateBack()
                            },
                            isCompletingProfile = completingGoogleProfile,
                            initialName = auth.currentUser?.displayName ?: "",
                            initialEmail = auth.currentUser?.email ?: "",
                            onCompleteProfileClick = { phone, address ->
                                completeGoogleProfile(
                                    phone = phone,
                                    address = address
                                ) {
                                    completingGoogleProfile = false
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
                            email = selectedUser.email,
                            address = selectedUser.address,
                            isVerified = selectedUser.isVerified,
                            onBackClick = { navigateBack() },
                            onDeactivateClick = {
                                firestore.collection("users").document(selectedUser.id).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this@MainActivity, "Akun berhasil dihapus dari Firestore.", Toast.LENGTH_SHORT).show()
                                        navigateBack()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@MainActivity, "Gagal menghapus akun: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
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
                            needDetail = selectedJob.description,
                            location = selectedJob.location,
                            time = selectedJob.scheduledAt,
                            fee = rupiah(selectedJob.fee),
                            helper = selectedJob.helperName ?: "Belum ada",
                            onBackClick = { navigateBack() },
                            onDeactivateClick = {
                                firestore.collection("jobs").document(selectedJob.id).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this@MainActivity, "Job berhasil dihapus dari Firestore.", Toast.LENGTH_SHORT).show()
                                        navigateBack()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@MainActivity, "Gagal menghapus job: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
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

    private fun checkAndEnsureAdmin(uid: String?, onReady: () -> Unit) {
        if (uid == null) {
            onReady()
            return
        }
        val userRef = firestore.collection("users").document(uid)
        userRef.get().addOnSuccessListener { doc ->
            if (!doc.exists() || doc.getString("role") != "admin") {
                userRef.set(
                    mapOf(
                        "name" to "Admin Tulungin",
                        "email" to "admin@tulungin.demo",
                        "role" to "admin",
                        "verified" to true,
                        "verificationRequested" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).addOnCompleteListener { onReady() }
            } else {
                onReady()
            }
        }.addOnFailureListener { onReady() }
    }

    private fun signInWithGoogle(
        onSuccess: () -> Unit,
        onIncomplete: () -> Unit
    ) {
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
                        onSuccess = onSuccess,
                        onIncomplete = onIncomplete
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
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val trimmedAddress = address.trim()

        when {
            trimmedName.isBlank() -> {
                Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show()
                return
            }
            trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
                return
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return
            }
            trimmedPhone.isBlank() || !Regex("^\\+?[0-9]{10,15}$").matches(trimmedPhone) -> {
                Toast.makeText(this, "Nomor HP tidak valid (10-15 digit)", Toast.LENGTH_SHORT).show()
                return
            }
            trimmedAddress.isBlank() -> {
                Toast.makeText(this, "Alamat wajib diisi", Toast.LENGTH_SHORT).show()
                return
            }
        }

        auth.createUserWithEmailAndPassword(trimmedEmail, password)
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
                        "name" to trimmedName,
                        "email" to trimmedEmail,
                        "phone" to trimmedPhone,
                        "address" to trimmedAddress,
                        "photoUrl" to "",
                        "role" to "user",
                        "verified" to false,
                        "verificationRequested" to false,
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
        onSuccess: () -> Unit,
        onIncomplete: () -> Unit
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

                    syncGoogleUser(
                        onSuccess = onSuccess,
                        onIncomplete = onIncomplete
                    )
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

    private fun syncGoogleUser(
        onSuccess: () -> Unit,
        onIncomplete: () -> Unit
    ) {
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

                    // Profile belum lengkap
                    if (
                        name.isBlank() ||
                        phone.isBlank() ||
                        address.isBlank()
                    ) {
                        Log.d(
                            "TulunginAuth",
                            "Profile Google belum lengkap: ${user.uid}"
                        )

                        onIncomplete()
                        return@addOnSuccessListener
                    }

                    // Profile sudah lengkap
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
                        "Profile Google lengkap: ${user.uid}"
                    )

                    onSuccess()

                } else {

                    // User Google baru
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
                        "verified" to false,
                        "verificationRequested" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    userRef.set(userData)
                        .addOnSuccessListener {

                            Log.d(
                                "TulunginAuth",
                                "Profile Google baru dibuat: ${user.uid}"
                            )

                            // Karena HP dan alamat masih kosong,
                            // jangan masuk UserApp dulu.
                            onIncomplete()
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

    private fun completeGoogleProfile(
        phone: String,
        address: String,
        onSuccess: () -> Unit
    ) {
        val trimmedPhone = phone.trim()
        val trimmedAddress = address.trim()

        when {
            trimmedPhone.isBlank() || !Regex("^\\+?[0-9]{10,15}$").matches(trimmedPhone) -> {
                Toast.makeText(this, "Nomor HP tidak valid (10-15 digit)", Toast.LENGTH_SHORT).show()
                return
            }
            trimmedAddress.isBlank() -> {
                Toast.makeText(this, "Alamat wajib diisi", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "User belum login ke Firebase Auth.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val userRef = firestore.collection("users").document(user.uid)
        userRef.set(
            mapOf(
                "name" to (user.displayName ?: "User"),
                "email" to (user.email ?: ""),
                "phone" to trimmedPhone,
                "address" to trimmedAddress,
                "role" to "user",
                "verified" to false,
                "verificationRequested" to false,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this,
                "Gagal menyimpan profile: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun syncEmailUser(onSuccess: () -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "User tidak ditemukan.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        firestore
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(
                        this,
                        "Data profile user tidak ditemukan.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val name = document.getString("name") ?: ""
                val email = document.getString("email")
                    ?: user.email
                    ?: ""
                val phone = document.getString("phone") ?: ""
                val address = document.getString("address") ?: ""

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
                    "Profile email user dimuat: ${user.uid}"
                )

                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(
                    "TulunginAuth",
                    "Gagal mengambil profile email user",
                    e
                )

                Toast.makeText(
                    this,
                    "Gagal mengambil data profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
