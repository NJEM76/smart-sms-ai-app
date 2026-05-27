package com.njem.smartsms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun SearchScreen(messages: List<SmsMessage>, onClose: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = if (query.length >= 2) {
        messages.filter {
            it.body.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true)
        }
    } else emptyList()

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceDark).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Tafuta SMS...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryColor,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = PrimaryColor
                ),
                singleLine = true
            )
        }

        if (query.length < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Andika angalau herufi 2 kutafuta", color = TextSecondary, fontSize = 14.sp)
            }
        } else if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hakuna matokeo kwa '$query'", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            Text(
                "${results.size} matokeo",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { message ->
                    SearchResultCard(message, query)
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(message: SmsMessage, query: String) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(message.address.take(1).uppercase(), color = categoryColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(message.address, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                    Surface(shape = RoundedCornerShape(8.dp), color = categoryColor.copy(alpha = 0.15f)) {
                        Text(categoryLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = categoryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(message.body, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
            }
        }
    }
}
