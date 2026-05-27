package com.njem.smartsms.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.njem.smartsms.ui.theme.*
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    var spamFilter by remember { mutableStateOf(true) }
    var autoDeleteOtp by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var smartReply by remember { mutableStateOf(false) }
    var isDefault by remember { mutableStateOf(DefaultSmsHelper.isDefault(context)) }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⚙️ Mipangilio", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Smart SMS AI — by NJEM MABULA", color = SecondaryColor, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDefault) BankColor.copy(alpha = 0.15f) else SpamColor.copy(alpha = 0.15f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isDefault) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isDefault) BankColor else SpamColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isDefault) "✅ App ya Default ya SMS" else "⚠️ Si Default SMS App",
                        color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                    Text(
                        if (isDefault) "App hii ndiyo inayodhibiti SMS zako" else "Bonyeza kuweka kama default",
                        color = TextSecondary, fontSize = 11.sp
                    )
                }
                if (!isDefault) {
                    Button(
                        onClick = {
                            activity?.let { DefaultSmsHelper.requestDefault(it) }
                            isDefault = DefaultSmsHelper.isDefault(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Weka", fontSize = 12.sp)
                    }
                }
            }
        }

        SettingItem(Icons.Default.Block, "Zuia Spam", "Gundua na zuia SMS za spam", spamFilter) { spamFilter = it }
        SettingItem(Icons.Default.DeleteSweep, "Futa OTP Kiotomatiki", "Futa OTP baada ya masaa 24", autoDeleteOtp) { autoDeleteOtp = it }
        SettingItem(Icons.Default.Notifications, "Arifa", "Pokea arifa za SMS mpya", notifications) { notifications = it }
        SettingItem(Icons.Default.AutoAwesome, "Majibu ya AI", "Mapendekezo ya majibu kwa AI", smartReply) { smartReply = it }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kuhusu App", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Smart SMS AI v1.0", color = TextSecondary, fontSize = 12.sp)
                Text("Imetengenezwa na NJEM MABULA", color = TextSecondary, fontSize = 12.sp)
                Text("AI inafanya kazi offline 100%", color = SecondaryColor, fontSize = 12.sp)
            }
        }
    }
}
@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor, checkedTrackColor = PrimaryColor.copy(alpha = 0.5f))
            )
        }
    }
}
