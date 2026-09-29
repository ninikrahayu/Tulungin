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
import androidx.compose.foundation.layout.offset
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
import com.pemmob.tulungin.ui.theme.TulunginDangerBorder
import com.pemmob.tulungin.ui.theme.TulunginDangerContainer
import com.pemmob.tulungin.ui.theme.TulunginDangerText
import com.pemmob.tulungin.ui.theme.TulunginInactiveStep
import com.pemmob.tulungin.ui.theme.TulunginMintBackground
import com.pemmob.tulungin.ui.theme.TulunginMintBorder
import com.pemmob.tulungin.ui.theme.TulunginPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextSecondary
import com.pemmob.tulungin.ui.theme.TulunginTheme

@Composable
fun DetailJob(
    modifier: Modifier = Modifier,
    jobTitle: String = "Bantu Pindahan Kos",
    statusText: String = "Sedang dikerjakan",
    needDetail: String = "Membantu memindahkan barang dari kamar kos ke tempat baru.",
    category: String = "Jasa Rumah",
    location: String = "Jl. Kampus No. 12, Purwokerto",
    time: String = "28 September 2026 · 09.00",
    fee: String = "Rp50.000",
    requester: String = "Andi Pratama",
    helper: String = "Budi Santoso",
    currentProgressStep: Int = 2, // 0: Job dibuat, 1: Job diambil, 2: Sedang dikerjakan, 3: Bukti penyelesaian, 4: Selesai
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
                text = "Detail Job",
                color = TulunginTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Job Title ("Bantu Pindahan Kos")
        Text(
            text = jobTitle,
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Status Badge / Banner ("Sedang dikerjakan")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TulunginMintBackground)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = statusText,
                color = TulunginPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card "Detail Kebutuhan"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(TulunginMintBackground)
                .padding(16.dp)
        ) {
            Text(
                text = "Detail Kebutuhan",
                color = TulunginPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = needDetail,
                color = TulunginPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info Cards (Each in its own rounded outlined card)
        // 1. Kategori
        JobInfoCard(
            label = "Kategori",
            value = category
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Lokasi
        JobInfoCard(
            label = "Lokasi",
            value = location
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Waktu
        JobInfoCard(
            label = "Waktu",
            value = time
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Upah Jasa
        JobInfoCard(
            label = "Upah Jasa",
            value = fee
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Peminta
        JobInfoCard(
            label = "Peminta",
            value = requester
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Penulung
        JobInfoCard(
            label = "Penulung",
            value = helper
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: "Progres Job"
        Text(
            text = "Progres Job",
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Progres Job Timeline Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.2.dp, TulunginMintBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            val progressSteps = listOf(
                "Job dibuat",
                "Job diambil",
                "Sedang dikerjakan",
                "Bukti penyelesaian",
                "Selesai"
            )

            progressSteps.forEachIndexed { index, stepTitle ->
                DetailTimelineItem(
                    title = stepTitle,
                    isCompleted = index <= currentProgressStep,
                    isCurrent = index == currentProgressStep,
                    isLast = index == progressSteps.lastIndex
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Button: "Nonaktifkan Job"
        Button(
            onClick = onDeactivateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
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
                    painter = painterResource(id = R.drawable.ic_block),
                    contentDescription = null,
                    tint = TulunginDangerText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nonaktifkan Job",
                    color = TulunginDangerText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun JobInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.2.dp, TulunginMintBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            color = TulunginTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TulunginTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailTimelineItem(
    title: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical Indicator (Dot + connecting line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(16.dp)
        ) {
            // Circle Dot
            if (isCompleted || isCurrent) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(TulunginPrimary, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.White, CircleShape)
                        .border(1.8.dp, TulunginInactiveStep, CircleShape)
                )
            }

            // Connecting Line downwards to next step
            if (!isLast) {
                val lineColor = if (isCompleted && !isCurrent) TulunginPrimary else TulunginInactiveStep
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(lineColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Step Label Text
        Text(
            text = title,
            color = if (isCompleted || isCurrent) TulunginTextPrimary else TulunginTextSecondary,
            fontSize = 14.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.offset(y = (-3).dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailJobPreview() {
    TulunginTheme {
        DetailJob()
    }
}
