package com.njem.smartsms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.data.model.SmsMessage
import com.njem.smartsms.ui.theme.*

@Composable
fun StatsScreen(messages: List<SmsMessage>) {
    val total = messages.size
    val bank = messages.count { it.category == SmsCategory.BANK }
    val otp = messages.count { it.category == SmsCategory.OTP }
    val spam = messages.count { it.category == SmsCategory.SPAM }
    val ads = messages.count { it.category == SmsCategory.ADS }
    val personal = messages.count { it.category == SmsCategory.PERSONAL }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "📊 SMS Statistics",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Jumla ya SMS: $total",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        StatBar("💰 Bank", bank, total, BankColor)
        StatBar("👤 Personal", personal, total, PersonalColor)
        StatBar("📢 Ads", ads, total, AdsColor)
        StatBar("🚫 Spam", spam, total, SpamColor)
        StatBar("🔑 OTP", otp, total, OtpColor)
    }
}

@Composable
fun StatBar(label: String, count: Int, total: Int, color: androidx.compose.ui.graphics.Color) {
    val percent = if (total > 0) count.toFloat() / total else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("$count SMS", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = percent,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = SurfaceDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${(percent * 100).toInt()}% ya SMS zote",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
