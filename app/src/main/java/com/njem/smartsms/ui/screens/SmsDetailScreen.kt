package com.njem.smartsms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.data.model.SmsMessage
import com.njem.smartsms.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetailScreen(message: SmsMessage, onBack: () -> Unit, onReply: (String) -> Unit) {
    val categoryColor = when (message.category) {
        SmsCategory.BANK -> BankColor
        SmsCategory.OTP -> OtpColor
        SmsCategory.SPAM -> SpamColor
        SmsCategory.ADS -> AdsColor
        SmsCategory.PERSONAL -> PersonalColor
    }
    val categoryLabel = when (message.category) {
        SmsCategory.BANK -> "💰 Bank"
        SmsCategory.OTP -> "🔑 OTP"
        SmsCategory.SPAM -> "🚫 Spam"
        SmsCategory.ADS -> "📢 Ads"
        SmsCategory.PERSONAL -> "👤 Personal"
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(message.address, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { onReply(message.address) }) {
                        Icon(Icons.Default.Reply, contentDescription = "Reply", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(message.address.take(1).uppercase(), color = categoryColor, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(message.address, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(dateFormat.format(Date(message.date)), color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = categoryColor.copy(alpha = 0.15f)) {
                    Text(categoryLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = categoryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Text(
                    message.body,
                    modifier = Modifier.padding(16.dp),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            Button(
                onClick = { onReply(message.address) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Reply, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Jibu SMS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
