package com.njem.smartsms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.njem.smartsms.data.SmsRepository
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.data.model.SmsMessage
import com.njem.smartsms.ui.screens.ComposeSmsScreen
import com.njem.smartsms.ui.screens.SearchScreen
import com.njem.smartsms.ui.screens.SettingsScreen
import com.njem.smartsms.ui.screens.SmsDetailScreen
import com.njem.smartsms.ui.screens.StatsScreen
import com.njem.smartsms.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class MainActivity : ComponentActivity() {
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) requestPermissions.launch(notGranted.toTypedArray())
        setContent { SmartSmsTheme { SmartSmsApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSmsApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var selectedScreen by remember { mutableStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var showCompose by remember { mutableStateOf(false) }
    var composeRecipient by remember { mutableStateOf("") }
    var selectedMessage by remember { mutableStateOf<SmsMessage?>(null) }
    val tabs = listOf("All", "Personal", "Bank", "OTP", "Ads", "Spam")
    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    fun loadMessages() {
        isLoading = true
    }

    LaunchedEffect(refreshTrigger) {
        withContext(Dispatchers.IO) {
            val repo = SmsRepository(context)
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
            val loaded = if (hasSmsPermission) repo.getAllSms() else listOf(
                SmsMessage(1, "M-PESA", "TZS 50,000 imetumwa.", System.currentTimeMillis(), false, SmsCategory.BANK),
                SmsMessage(2, "AIRTEL", "Nambari yako ya uthibitisho ni 847291.", System.currentTimeMillis() - 3600000, true, SmsCategory.OTP),
                SmsMessage(3, "John Doe", "Habari! Tutaonana saa ngapi?", System.currentTimeMillis() - 7200000, true, SmsCategory.PERSONAL)
            )
            withContext(Dispatchers.Main) {
                messages = loaded
                isLoading = false
            }
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                refreshTrigger++
            }
        }
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    BackHandler(enabled = showSearch || showCompose || selectedMessage != null) {
        when {
            selectedMessage != null -> selectedMessage = null
            showSearch -> showSearch = false
            showCompose -> { showCompose = false; composeRecipient = "" }
        }
    }

    when {
        selectedMessage != null -> {
            SmsDetailScreen(
                message = selectedMessage!!,
                onBack = { selectedMessage = null },
                onReply = { address ->
                    composeRecipient = address
                    selectedMessage = null
                    showCompose = true
                }
            )
            return
        }
        showSearch -> { SearchScreen(messages) { showSearch = false }; return }
        showCompose -> {
            ComposeSmsScreen(initialRecipient = composeRecipient) {
                showCompose = false
                composeRecipient = ""
                refreshTrigger++
            }
            return
        }
    }

    Scaffold(
        topBar = { SmartTopBar { showSearch = true } },
        bottomBar = { SmartBottomBar(selectedScreen) { selectedScreen = it } },
        floatingActionButton = {
            if (selectedScreen == 0) {
                FloatingActionButton(
                    onClick = { showCompose = true },
                    containerColor = PrimaryColor,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "New SMS", tint = Color.White)
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedScreen) {
                0 -> MessagesScreen(messages, isLoading, selectedTab, tabs, { selectedTab = it }) { selectedMessage = it }
                1 -> StatsScreen(messages)
                2 -> SettingsScreen()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTopBar(onSearchClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text("Smart SMS AI", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("by NJEM MABULA", color = SecondaryColor, fontSize = 11.sp)
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
    )
}

@Composable
fun MessagesScreen(messages: List<SmsMessage>, isLoading: Boolean, selectedTab: Int, tabs: List<String>, onTabSelect: (Int) -> Unit, onMessageClick: (SmsMessage) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        CategoryTabs(tabs, selectedTab, onTabSelect)
        StatsRow(messages)
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Inapakia SMS...", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            MessagesList(messages, selectedTab, onMessageClick)
        }
    }
}

@Composable
fun CategoryTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    ScrollableTabRow(selectedTabIndex = selected, containerColor = SurfaceDark, contentColor = PrimaryColor, edgePadding = 8.dp) {
        tabs.forEachIndexed { index, tab ->
            Tab(selected = selected == index, onClick = { onSelect(index) },
                text = { Text(tab, color = if (selected == index) PrimaryColor else TextSecondary, fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

@Composable
fun StatsRow(messages: List<SmsMessage>) {
    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("Total", messages.size.toString(), PrimaryColor, Modifier.weight(1f))
        StatCard("Spam", messages.count { it.category == SmsCategory.SPAM }.toString(), SpamColor, Modifier.weight(1f))
        StatCard("Bank", messages.count { it.category == SmsCategory.BANK }.toString(), BankColor, Modifier.weight(1f))
        StatCard("OTP", messages.count { it.category == SmsCategory.OTP }.toString(), OtpColor, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun MessagesList(messages: List<SmsMessage>, selectedTab: Int, onMessageClick: (SmsMessage) -> Unit) {
    val filtered = when (selectedTab) {
        0 -> messages
        1 -> messages.filter { it.category == SmsCategory.PERSONAL }
        2 -> messages.filter { it.category == SmsCategory.BANK }
        3 -> messages.filter { it.category == SmsCategory.OTP }
        4 -> messages.filter { it.category == SmsCategory.ADS }
        5 -> messages.filter { it.category == SmsCategory.SPAM }
        else -> messages
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filtered) { message -> MessageCard(message, onMessageClick) }
    }
}

@Composable
fun MessageCard(message: SmsMessage, onClick: (SmsMessage) -> Unit) {
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
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick(message) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
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

@Composable
fun SmartBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = SurfaceDark) {
        NavigationBarItem(selected = selected == 0, onClick = { onSelect(0) }, icon = { Icon(Icons.Default.Message, contentDescription = "Messages") }, label = { Text("Messages") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, selectedTextColor = PrimaryColor, unselectedIconColor = TextSecondary))
        NavigationBarItem(selected = selected == 1, onClick = { onSelect(1) }, icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") }, label = { Text("Stats") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, selectedTextColor = PrimaryColor, unselectedIconColor = TextSecondary))
        NavigationBarItem(selected = selected == 2, onClick = { onSelect(2) }, icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }, label = { Text("Settings") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, selectedTextColor = PrimaryColor, unselectedIconColor = TextSecondary))
    }
}
