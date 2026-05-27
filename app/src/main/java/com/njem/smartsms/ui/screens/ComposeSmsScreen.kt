package com.njem.smartsms.ui.screens

import android.app.Activity
import android.telephony.SmsManager
import android.widget.Toast
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeSmsScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text("✍️ Andika SMS", color = TextPrimary, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (recipient.isNotEmpty() && message.isNotEmpty()) {
                            isSending = true
                            try {
                                val smsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                                    context.getSystemService(SmsManager::class.java)
                                } else {
                                    @Suppress("DEPRECATION")
                                    SmsManager.getDefault()
                                }
                                val parts = smsManager.divideMessage(message)
                                if (parts.size > 1) {
                                    smsManager.sendMultipartTextMessage(recipient, null, parts, null, null)
                                } else {
                                    smsManager.sendTextMessage(recipient, null, message, null, null)
                                }
                                Toast.makeText(context, "✅ SMS imetumwa!", Toast.LENGTH_SHORT).show()
                                onClose()
                            } catch (e: Exception) {
                                Toast.makeText(context, "❌ Imeshindwa: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                            isSending = false
                        } else {
                            Toast.makeText(context, "Jaza namba na ujumbe!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isSending
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("📱 Namba ya Mpokeaji", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = TextSecondary,
                            cursorColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor
                        ),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryColor)
                        }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(4.dp).fillMaxSize()) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("💬 Andika ujumbe wako...", color = TextSecondary) },
                        modifier = Modifier.fillMaxSize(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = TextSecondary,
                            cursorColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${message.length} herufi",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Button(
                    onClick = {
                        if (recipient.isNotEmpty() && message.isNotEmpty()) {
                            isSending = true
                            try {
                                val smsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                                    context.getSystemService(SmsManager::class.java)
                                } else {
                                    @Suppress("DEPRECATION")
                                    SmsManager.getDefault()
                                }
                                val parts = smsManager.divideMessage(message)
                                if (parts.size > 1) {
                                    smsManager.sendMultipartTextMessage(recipient, null, parts, null, null)
                                } else {
                                    smsManager.sendTextMessage(recipient, null, message, null, null)
                                }
                                Toast.makeText(context, "✅ SMS imetumwa!", Toast.LENGTH_SHORT).show()
                                onClose()
                            } catch (e: Exception) {
                                Toast.makeText(context, "❌ Imeshindwa: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                            isSending = false
                        } else {
                            Toast.makeText(context, "Jaza namba na ujumbe!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSending,
                    modifier = Modifier.height(48.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, tint = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tuma SMS", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
