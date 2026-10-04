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
    fun browseFilterAndApplyJob() {
        compose.setContent { UserApp(model) {} }
        compose.onNodeWithText("Job Avail").performClick()
        compose.onNodeWithText("Cari Job").assertIsDisplayed()
        capture("02-job-available")
    }
}
