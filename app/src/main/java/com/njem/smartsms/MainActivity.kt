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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.njem.smartsms.data.SmsRepository
import com.njem.smartsms.data.model.Conversation
import com.njem.smartsms.data.model.SmsMessage
import com.njem.smartsms.ui.screens.ChatScreen
import com.njem.smartsms.ui.screens.ComposeSmsScreen
import com.njem.smartsms.ui.screens.ConversationListScreen
import com.njem.smartsms.ui.screens.SearchScreen
import com.njem.smartsms.ui.screens.SettingsScreen
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
    var selectedScreen by remember { mutableStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var showCompose by remember { mutableStateOf(false) }
    var selectedConversation by remember { mutableStateOf<Conversation?>(null) }
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var allMessages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    suspend fun loadData() {
        withContext(Dispatchers.IO) {
            val repo = SmsRepository(context)
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
            if (hasSmsPermission) {
                val convs = repo.getConversations()
                val msgs = repo.getAllSms()
                withContext(Dispatchers.Main) {
                    conversations = convs
                    allMessages = msgs
                    isLoading = false
                }
            } else {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    LaunchedEffect(refreshTrigger) { loadData() }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                refreshTrigger++
            }
        }
        val filter = IntentFilter().apply {
            addAction("com.njem.smartsms.SMS_RECEIVED")
            addAction("android.provider.Telephony.SMS_RECEIVED")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    BackHandler(enabled = showSearch || showCompose || selectedConversation != null) {
        when {
            selectedConversation != null -> selectedConversation = null
            showSearch -> showSearch = false
            showCompose -> showCompose = false
        }
    }

    when {
        selectedConversation != null -> {
            ChatScreen(
                conversation = selectedConversation!!,
                onBack = { selectedConversation = null; refreshTrigger++ }
            )
            return
        }
        showSearch -> { SearchScreen(allMessages) { showSearch = false }; return }
        showCompose -> {
            ComposeSmsScreen { showCompose = false; refreshTrigger++ }
            return
        }
    }

    Scaffold(
        topBar = { SmartTopBar(conversations.sumOf { it.unreadCount }) { showSearch = true } },
        bottomBar = { SmartBottomBar(selectedScreen) { selectedScreen = it } },
        floatingActionButton = {
            if (selectedScreen == 0) {
                FloatingActionButton(onClick = { showCompose = true }, containerColor = PrimaryColor, shape = CircleShape) {
                    Icon(Icons.Default.Edit, contentDescription = "New SMS", tint = Color.White)
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedScreen) {
                0 -> ConversationListScreen(conversations, isLoading) { selectedConversation = it }
                1 -> StatsScreen(allMessages)
                2 -> SettingsScreen()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTopBar(unreadCount: Int, onSearchClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text("Smart SMS AI", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    if (unreadCount > 0) "$unreadCount SMS mpya • by NJEM MABULA"
                    else "by NJEM MABULA",
                    color = if (unreadCount > 0) AccentColor else SecondaryColor,
                    fontSize = 11.sp,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
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
fun SmartBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = SurfaceDark) {
        NavigationBarItem(
            selected = selected == 0, onClick = { onSelect(0) },
            icon = { Icon(Icons.Default.Message, contentDescription = "Messages") },
            label = { Text("Messages") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, selectedTextColor = PrimaryColor, unselectedIconColor = TextSecondary)
        )
        NavigationBarItem(
            selected = selected == 1, onClick = { onSelect(1) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
            label = { Text("Stats") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, selectedTextColor = PrimaryColor, unselectedIconColor = TextSecondary)
        )
        NavigationBarItem(
            selected = selected == 2, onClick = { onSelect(2) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryColor, selectedTextColor = PrimaryColor, unselectedIconColor = TextSecondary)
        )
    }
}
