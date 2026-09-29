package com.pemmob.tulungin.ui.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.caverock.androidsvg.SVG
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.TulunginPrimary
import com.pemmob.tulungin.ui.theme.TulunginMintBackground
import java.text.NumberFormat
import java.util.Locale

internal val UserPrimary = TulunginPrimary
internal val UserMint = TulunginMintBackground
internal val UserInk = Color(0xFF020202)
internal val UserSecondary = Color(0xFF5F6368)
internal val UserOutline = Color(0xFFCAC4D0)
internal val UserSurface = Color(0xFFF9F9F9)
internal val UserSoft = Color(0xFFEEF3F3)
internal val UserFont = FontFamily(Font(R.font.inter_regular), Font(R.font.inter_medium, FontWeight.Medium), Font(R.font.inter_semibold, FontWeight.SemiBold), Font(R.font.inter_bold, FontWeight.Bold))
internal fun rupiah(value: Long): String = "Rp" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(value)

@Composable
internal fun UText(text: String, modifier: Modifier = Modifier, size: Int = 14, weight: FontWeight = FontWeight.Normal, color: Color = UserInk, lineHeight: Int = size + 6, align: TextAlign? = null) {
    Text(text, modifier, color = color, fontSize = size.sp, fontWeight = weight, fontFamily = UserFont, lineHeight = lineHeight.sp, textAlign = align, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
}

@Composable
internal fun FigmaAsset(name: String, modifier: Modifier = Modifier, description: String? = null) {
    val context = LocalContext.current
    val painter = remember(name) {
        val picture = SVG.getFromAsset(context.assets, "figma/$name.svg").renderToPicture()
        object : Painter() {
            override val intrinsicSize = Size(picture.width.toFloat(), picture.height.toFloat())
            override fun DrawScope.onDraw() {
                drawIntoCanvas {
                    val canvas = it.nativeCanvas
                    canvas.save()
                    canvas.scale(size.width / picture.width, size.height / picture.height)
                    canvas.drawPicture(picture)
                    canvas.restore()
                }
            }
        }
    }
    Image(painter, description, modifier)
}

@Composable
internal fun UserHeader(title: String, onBack: (() -> Unit)?) {
    Column(Modifier.fillMaxWidth().background(Color.White).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Box(Modifier.width(32.dp).height(48.dp), contentAlignment = Alignment.CenterStart) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-8).dp).size(40.dp)) {
                        FigmaAsset("back", Modifier.size(24.dp), "Kembali")
                    }
                }
                Spacer(Modifier.width(12.dp))
            }
            UText(title, size = 20, weight = FontWeight.Bold, lineHeight = 28)
        }
        HorizontalDivider(color = Color(0xFFF3F4F6))
    }
}

@Composable
internal fun UserBottomBar(selected: String, onTab: (String) -> Unit, onCreate: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val itemWidth = ((maxWidth - 24.dp) / 5).coerceAtMost(67.dp)
        Column(Modifier.padding(top = 38.5.dp).fillMaxWidth().background(UserPrimary, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().height(72.dp).padding(start = 12.dp, end = 12.dp, top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("home" to "Beranda", "jobs" to "Job Avail", "create" to "", "history" to "Histori", "profile" to "Profil").forEach { (route, label) ->
                    if (route == "create") Spacer(Modifier.width(itemWidth)) else {
                        val active = selected == route
                        Column(Modifier.width(itemWidth).clip(RoundedCornerShape(12.dp)).clickable { onTab(route) }.padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val base = when (route) { "jobs" -> "nav_job"; "history" -> "nav_history"; "profile" -> "nav_profile"; else -> "nav_home" }
                            Box(Modifier.size(60.dp, 36.dp).background(if (active && route != "home") UserMint else Color.Transparent, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                                FigmaAsset(base + if (active) "_selected" else "", if (route == "home") Modifier.size(60.dp, 36.dp) else Modifier.size(24.dp))
                            }
                            Spacer(Modifier.height(3.dp))
                            UText(label, size = 11, lineHeight = 16, color = if (active) Color(0xFFDFF7F7) else Color.White, weight = if (active) FontWeight.SemiBold else FontWeight.Medium, align = TextAlign.Center)
                        }
                    }
                }
            }
        }
        Box(Modifier.align(Alignment.TopCenter).size(79.2.dp).clip(CircleShape).clickable(onClick = onCreate), contentAlignment = Alignment.Center) {
            FigmaAsset("nav_notch", Modifier.size(79.2.dp))
            FigmaAsset("nav_circle", Modifier.size(75.dp))
            FigmaAsset("nav_add", Modifier.size(49.313.dp), "Buat Permintaan")
        }
    }
}

@Composable
internal fun UserContent(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 18.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
}

@Composable
internal fun UserButton(label: String, modifier: Modifier = Modifier, secondary: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 50.dp), enabled = enabled, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (secondary) UserMint else UserPrimary, contentColor = if (secondary) UserPrimary else Color.White), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        UText(label, color = if (enabled) (if (secondary) UserPrimary else Color.White) else UserSecondary, weight = FontWeight.SemiBold, align = TextAlign.Center, lineHeight = 20)
    }
}

@Composable
internal fun UserField(label: String, value: String, onValue: (String) -> Unit, placeholder: String = "", multiline: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    val focus = LocalFocusManager.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        UText(label, size = 13, weight = FontWeight.Medium, lineHeight = 20)
        BasicTextField(value, onValue, modifier = Modifier.fillMaxWidth().heightIn(min = if (multiline) 86.dp else 54.dp).clip(RoundedCornerShape(12.dp)).background(UserSurface).border(1.dp, UserOutline, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = if (multiline) 12.dp else 15.dp).semantics { contentDescription = label }, textStyle = TextStyle(color = UserInk, fontFamily = UserFont, fontSize = 14.sp, lineHeight = 20.sp, platformStyle = PlatformTextStyle(includeFontPadding = false)), singleLine = !multiline, maxLines = if (multiline) 5 else 1, cursorBrush = SolidColor(UserPrimary), keyboardOptions = KeyboardOptions(keyboardType = keyboard, imeAction = if (multiline) ImeAction.Default else ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Next) }), decorationBox = { inner ->
            Box { if (value.isEmpty()) UText(placeholder, color = UserSecondary, lineHeight = 20); inner() }
        })
    }
}

@Composable
internal fun UserSelector(label: String, value: String, placeholder: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        UText(label, size = 13, weight = FontWeight.Medium, lineHeight = 20)
        Box(Modifier.fillMaxWidth().heightIn(min = 54.dp).clip(RoundedCornerShape(12.dp)).background(UserSurface).border(1.dp, UserOutline, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp)) {
            UText(value.ifBlank { placeholder }, color = if (value.isBlank()) UserSecondary else UserInk, lineHeight = 20)
        }
    }
}

@Composable
internal fun UserCard(modifier: Modifier = Modifier, border: Color = UserMint, spacing: Dp = 6.dp, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth().then(if (onClick != null) Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick) else Modifier), shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, border), shadowElevation = if (border == UserMint) 1.dp else 0.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(spacing), content = content)
    }
}

@Composable
internal fun DetailCard(label: String, value: String, onClick: (() -> Unit)? = null) {
    Surface(Modifier.fillMaxWidth().heightIn(min = 65.dp).then(if (onClick != null) Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick) else Modifier), color = Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, UserMint), shadowElevation = 1.dp) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            UText(label, size = 12, color = UserSecondary, lineHeight = 17)
            UText(value, weight = FontWeight.SemiBold, lineHeight = 20)
        }
    }
}

@Composable
internal fun Notice(title: String, body: String) {
    Column(Modifier.fillMaxWidth().heightIn(min = 85.dp).background(UserMint, RoundedCornerShape(14.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        UText(title, weight = FontWeight.SemiBold, color = Color(0xFF071F24), lineHeight = 20)
        UText(body, size = 13, color = Color(0xFF071F24), lineHeight = 18)
    }
}

@Composable
internal fun StatusStrip(label: String, completed: Boolean = false, cancelled: Boolean = false) {
    Box(Modifier.fillMaxWidth().heightIn(min = 42.dp).background(if (completed) Color(0xFFC8E6C9) else if (cancelled) Color(0xFFFFDAD6) else UserMint, RoundedCornerShape(12.dp)).padding(14.dp, 11.dp)) {
        UText(label, size = 13, color = if (completed) Color(0xFF123F18) else UserPrimary, weight = FontWeight.SemiBold, lineHeight = 20)
    }
}

@Composable
internal fun ConfirmDialog(title: String, message: String, confirm: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, UserOutline, RoundedCornerShape(24.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            UText(title, size = 20, weight = FontWeight.Bold, lineHeight = 28)
            UText(message, color = UserSecondary, lineHeight = 21)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UserButton("Batal", Modifier.weight(1f), secondary = true, onClick = onDismiss)
                UserButton(confirm, Modifier.weight(1f), onClick = onConfirm)
            }
        }
    }
}

@Composable
internal fun ChoiceDialog(title: String, options: List<String>, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color.White, title = { UText(title, size = 20, weight = FontWeight.Bold) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) { options.forEach { option ->
            TextButton(onClick = { onSelect(option); onDismiss() }, modifier = Modifier.fillMaxWidth()) { UText(option, color = UserPrimary) }
        } }
    }, confirmButton = { TextButton(onDismiss) { UText("Tutup", color = UserPrimary) } })
}
