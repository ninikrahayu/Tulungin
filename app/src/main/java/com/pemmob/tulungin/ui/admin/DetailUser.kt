package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.TulunginBorderGrey
import com.pemmob.tulungin.ui.theme.TulunginDangerBorder
import com.pemmob.tulungin.ui.theme.TulunginDeactivateBg
import com.pemmob.tulungin.ui.theme.TulunginDeactivateText
import com.pemmob.tulungin.ui.theme.TulunginMintBorder
import com.pemmob.tulungin.ui.theme.TulunginPrimary
import com.pemmob.tulungin.ui.theme.TulunginSuccessContainer
import com.pemmob.tulungin.ui.theme.TulunginSuccessContent
import com.pemmob.tulungin.ui.theme.TulunginSuccessDot
import com.pemmob.tulungin.ui.theme.TulunginTextMuted
import com.pemmob.tulungin.ui.theme.TulunginTextPrimary
import com.pemmob.tulungin.ui.theme.TulunginTheme
import com.pemmob.tulungin.ui.theme.TulunginWarningContainer
import com.pemmob.tulungin.ui.theme.TulunginWarningContent
import com.pemmob.tulungin.ui.theme.TulunginWarningDot

@Composable
fun DetailUser(
    modifier: Modifier = Modifier,
    name: String = "Andi Pratama",
    email: String = "andi@email.com",
    phone: String = "081234567890",
    address: String = "Jl. Mayjen Sungkono No. 12B, Kelurahan Selabaya, Kalimanah, Purbalingga",
    isVerified: Boolean = true,
    onBackClick: () -> Unit = {},
    onDeactivateClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        // Top Header
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
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Avatar & Name
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
                text = name,
                color = TulunginTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Verification Badge
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

        // Section Title: Informasi Akun
        Text(
            text = "Informasi Akun",
            color = TulunginTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Informasi Akun Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.2.dp, TulunginMintBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            // 1. Nama Lengkap
            InfoFieldItem(
                label = "Nama Lengkap",
                value = name
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            // 2. Email
            InfoFieldItem(
                label = "Email",
                value = email
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            // 3. Nomor HP
            InfoFieldItem(
                label = "Nomor HP",
                value = phone
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            // 4. Alamat
            InfoFieldItem(
                label = "Alamat",
                value = address
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = TulunginBorderGrey
            )

            // 5. Status Akun
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
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Button: Nonaktifkan Akun
        Button(
            onClick = onDeactivateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, TulunginDangerBorder),
            colors = ButtonDefaults.buttonColors(
                containerColor = TulunginDeactivateBg,
                contentColor = TulunginDeactivateText
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_block),
                    contentDescription = null,
                    tint = TulunginDeactivateText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nonaktifkan Akun",
                    color = TulunginDeactivateText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoFieldItem(
    label: String,
    value: String
) {
    Column {
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
            lineHeight = 20.sp
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
