package com.njem.smartsms.ui.screens

import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import android.provider.ContactsContract
import com.njem.smartsms.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeSmsScreen(initialRecipient: String = "", onClose: () -> Unit) {
    val context = LocalContext.current
    var recipient by remember { mutableStateOf(initialRecipient) }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val contactPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numberIndex >= 0) {
                        recipient = c.getString(numberIndex)?.replace(" ", "") ?: ""
                    }
                }
            }
        }
    }

    fun sendSms() {
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
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        TopAppBar(
            title = { Text("✍️ Andika SMS", color = TextPrimary, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            },
            actions = {
                IconButton(onClick = { sendSms() }, enabled = !isSending) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("📱 Namba ya Mpokeaji", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = TextSecondary,
                            cursorColor = PrimaryColor,
                            focusedLabelColor = PrimaryColor
                        ),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryColor) }
                    )
                    IconButton(onClick = { contactPicker.launch(null) }) {
                        Icon(Icons.Default.Contacts, contentDescription = "Contacts", tint = PrimaryColor)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("💬 Andika ujumbe wako...", color = TextSecondary) },
                    modifier = Modifier.fillMaxSize().padding(4.dp),
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${message.length} herufi", color = TextSecondary, fontSize = 12.sp)
                Button(
                    onClick = { sendSms() },
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
