package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.*

@Composable
fun DetailUser(
    modifier: Modifier = Modifier,
    name: String = "",
    email: String = "",
    phone: String = "",
    address: String = "",
    isVerified: Boolean = false,
    isActive: Boolean = true,
    isSelf: Boolean = false,
    onBackClick: () -> Unit = {},
    onToggleActiveClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showToggleActiveConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Kembali",
                    tint = TulunginTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Detail User/Akun",
                color = TulunginTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(TulunginPrimary, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = name.ifBlank { "Pengguna" },
                color = TulunginTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .background(
                        if (isVerified) TulunginSuccessContainer else TulunginWarningContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (isVerified) TulunginSuccessDot else TulunginWarningDot,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isVerified) "Terverifikasi" else "Belum Diverifikasi",
                    color = if (isVerified) TulunginSuccessContent else TulunginWarningContent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Informasi Akun",
            color = TulunginTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, TulunginMintBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            InfoFieldItem(
                label = "Nama Lengkap",
                value = name.ifBlank { "-" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            InfoFieldItem(
                label = "Email",
                value = email.ifBlank { "-" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            InfoFieldItem(
                label = "Nomor HP",
                value = phone.ifBlank { "-" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            InfoFieldItem(
                label = "Alamat",
                value = address.ifBlank { "-" }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            Column {
                Text(
                    text = "Status Akun",
                    color = TulunginTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (isVerified) TulunginSuccessDot else TulunginWarningDot,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVerified) "Terverifikasi" else "Belum Diverifikasi",
                        color = if (isVerified) TulunginSuccessContent else TulunginWarningContent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            Column {
                Text(
                    text = "Status Aktif",
                    color = TulunginTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (isActive) TulunginSuccessDot else TulunginDangerBorder,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActive) "Aktif" else "Nonaktif",
                        color = if (isActive) TulunginSuccessContent else TulunginDangerText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (isSelf) {
            Text(
                text = "Tidak dapat menonaktifkan atau menghapus akunmu sendiri.",
                color = TulunginTextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Button(
                onClick = { showToggleActiveConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.2.dp,
                    if (isActive) TulunginWarningDot else TulunginSuccessDot
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) TulunginWarningContainer else TulunginSuccessContainer,
                    contentColor = if (isActive) TulunginWarningContent else TulunginSuccessContent
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = if (isActive) R.drawable.ic_block else R.drawable.ic_person),
                        contentDescription = null,
                        tint = if (isActive) TulunginWarningContent else TulunginSuccessContent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "Nonaktifkan Akun" else "Aktifkan Akun",
                        color = if (isActive) TulunginWarningContent else TulunginSuccessContent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, TulunginDangerBorder),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TulunginDangerContainer,
                    contentColor = TulunginDangerText
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = null,
                        tint = TulunginDangerText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Akun",
                        color = TulunginDangerText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showToggleActiveConfirm) {
        AlertDialog(
            onDismissRequest = { showToggleActiveConfirm = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = if (isActive) "Nonaktifkan akun ini?" else "Aktifkan akun ini?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isActive) {
                        "User tidak akan bisa login sampai akun ini diaktifkan kembali. Data akun tidak akan dihapus."
                    } else {
                        "User akan bisa login kembali setelah akun ini diaktifkan."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showToggleActiveConfirm = false
                    onToggleActiveClick()
                }) {
                    Text(
                        text = if (isActive) "Nonaktifkan" else "Aktifkan",
                        color = TulunginPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleActiveConfirm = false }) {
                    Text("Batal", color = TulunginTextSecondary)
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color.White,
            title = { Text(text = "Hapus akun permanen?", fontWeight = FontWeight.Bold) },
            text = {
                Text(text = "Data akun ini akan dihapus secara permanen dari database dan tidak dapat dikembalikan.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteClick()
                }) {
                    Text("Hapus", color = TulunginDangerText, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = TulunginTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun InfoFieldItem(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TulunginTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TulunginTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailUserPreview() {
    TulunginTheme {
        DetailUser()
    }
}
