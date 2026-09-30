package com.pemmob.tulungin.user

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.pemmob.tulungin.ui.user.*
import com.pemmob.tulungin.data.user.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UserFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var model: UserViewModel

    @Before
    fun prepare() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("tulungin_user_demo_v1", 0).edit().clear().commit()
        model = UserViewModel(app)
    }

    private fun capture(name: String) {
        val folder = File("build/user-previews").apply { mkdirs() }
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test
    fun browseFilterApplyAndStartJob() {
        compose.setContent { UserApp(model) {} }
        compose.onNodeWithText("Halo, Andi!").assertIsDisplayed()
        capture("01-beranda")
        compose.onNodeWithText("Job Avail").performClick()
        compose.onNodeWithText("Cari Job").assertIsDisplayed()
        capture("02-job-available")
        compose.onNodeWithText("Filter Job").performClick()
        capture("03-filter")
        compose.onNodeWithText("Terapkan Filter").performClick()
        compose.onNodeWithText("Bantu Pindahan Kos").performClick()
        capture("04-detail-job")
        compose.onNodeWithText("Lamar Job").performClick()
        compose.onNodeWithText("Lamar Job?").assertIsDisplayed()
        compose.onAllNodesWithText("Lamar").onLast().performClick()
        compose.onNodeWithText("Cari Job").assertIsDisplayed()
        capture("05-lamaran-terkirim")
    }

    @Test
    fun requesterCanConfirmRateAndPayWithoutBackend() {
        compose.setContent { UserApp(model) {} }
        compose.onNodeWithText("Menunggu konfirmasi").performScrollTo().performClick()
        compose.onNodeWithText("Bukti dari Penulung").assertIsDisplayed()
        capture("06-konfirmasi")
        compose.onNodeWithText("Konfirmasi Selesai").performClick()
        compose.onNodeWithText("Selesai").performClick()
        compose.onNodeWithText("Beri Ulasan").performScrollTo().performClick()
        capture("07-ulasan")
        compose.onAllNodesWithText("☆")[4].performClick()
        compose.onNodeWithContentDescription("Ulasan").performTextInput("Cepat dan sangat membantu.")
        compose.onNodeWithText("Kirim Ulasan").performScrollTo().performClick()
        compose.onNodeWithText("Pembayaran").performScrollTo().performClick()
        capture("08-pembayaran")
        compose.onNodeWithText("Pilih metode pembayaran").performClick()
        compose.onNodeWithText("QRIS (simulasi)").performClick()
        compose.onNodeWithText("Lanjut Bayar").performClick()
        compose.onNodeWithText("Simulasikan").performClick()
        compose.onNodeWithText("Pembayaran simulasi berhasil").assertIsDisplayed()
    }

    @Test
    fun profileChatAndSupportAreReachable() {
        compose.setContent { UserApp(model) {} }
        compose.onNodeWithText("Profil").performClick()
        capture("09-profil")
        compose.onNodeWithText("Chat").performScrollTo().performClick()
        compose.onNodeWithText("Andi Pratama · Bantu Pindahan Kos").performClick()
        capture("10-chat")
        compose.onNodeWithContentDescription("Pesan").performTextInput("Halo, saya berangkat sekarang.")
        compose.onNodeWithText("Kirim").performClick()
        compose.onNodeWithText("Halo, saya berangkat sekarang.").assertExists()
    }

    @Test
    fun demoStateSurvivesRepositoryRecreation() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val first = LocalDemoRepository(app)
        val id = first.createJob(JobDraft("Antar buku", "Pengantaran", "Antarkan buku ke kampus.", "Jl. Kampus 12", "30 September 2026 · 10.00", 15000))
        first.sendSupport("Tolong bantu periksa permintaan saya.")
        val second = LocalDemoRepository(app)
        Assert.assertTrue(second.snapshot.value.jobs.any { it.id == id })
        Assert.assertEquals(1, second.snapshot.value.tickets.size)
    }
}
