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
import com.pemmob.tulungin.ui.theme.*

@Composable
fun DetailJob(
    modifier: Modifier = Modifier,
    jobTitle: String = "",
    statusText: String = "",
    needDetail: String = "",
    category: String = "",
    location: String = "",
    time: String = "",
    fee: String = "",
    requester: String = "",
    helper: String = "",
    currentProgressStep: Int = 0,
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

        Text(
            text = jobTitle.ifBlank { "Job" },
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TulunginMintBackground)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = statusText.ifBlank { "AVAILABLE" },
                color = TulunginPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                text = needDetail.ifBlank { "Tidak ada detail." },
                color = TulunginPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        JobInfoCard(label = "Kategori", value = category.ifBlank { "-" })
        Spacer(modifier = Modifier.height(12.dp))
        JobInfoCard(label = "Lokasi", value = location.ifBlank { "-" })
        Spacer(modifier = Modifier.height(12.dp))
        JobInfoCard(label = "Waktu", value = time.ifBlank { "-" })
        Spacer(modifier = Modifier.height(12.dp))
        JobInfoCard(label = "Upah Jasa", value = fee.ifBlank { "Rp0" })
        Spacer(modifier = Modifier.height(12.dp))
        JobInfoCard(label = "Peminta", value = requester.ifBlank { "-" })
        Spacer(modifier = Modifier.height(12.dp))
        JobInfoCard(label = "Penulung", value = helper.ifBlank { "Belum ada" })

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Progres Job",
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

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
                    text = "Hapus Job",
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(16.dp)
        ) {
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
