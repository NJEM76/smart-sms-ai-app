package com.njem.smartsms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.data.model.SmsMessage
import com.njem.smartsms.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartSmsTheme {
                SmartSmsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSmsApp() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Personal", "Bank", "OTP", "Spam")

    val sampleMessages = listOf(
        SmsMessage(1, "M-PESA", "TZS 50,000 imetumwa. Akaunti yako: TZS 234,500", System.currentTimeMillis(), false, SmsCategory.BANK),
        SmsMessage(2, "AIRTEL", "Nambari yako ya uthibitisho ni 847291. Usiishirikishe na mtu.", System.currentTimeMillis() - 3600000, true, SmsCategory.OTP),
        SmsMessage(3, "John Doe", "Habari! Tutaonana saa ngapi leo jioni?", System.currentTimeMillis() - 7200000, true, SmsCategory.PERSONAL),
        SmsMessage(4, "VODACOM", "Pata 50% punguzo la data! Jiunge sasa. Bonyeza hapa.", System.currentTimeMillis() - 10800000, true, SmsCategory.ADS),
        SmsMessage(5, "UNKNOWN", "Umeshinda zawadi! Tuma nambari yako sasa kupata TZS 1,000,000!", System.currentTimeMillis() - 14400000, true, SmsCategory.SPAM)
    )

    Scaffold(
        topBar = { SmartTopBar() },
        bottomBar = { SmartBottomBar() },
        floatingActionButton = { SmartFAB() },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundDark)
        ) {
            CategoryTabs(tabs, selectedTab) { selectedTab = it }
            StatsRow(sampleMessages)
            MessagesList(sampleMessages, selectedTab, tabs)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Smart SMS AI",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "Powered by AI • Offline",
                    color = SecondaryColor,
                    fontSize = 11.sp
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceDark
        )
    )
}

@Composable
fun CategoryTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = selected,
        containerColor = SurfaceDark,
        contentColor = PrimaryColor,
        edgePadding = 8.dp
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = {
                    Text(
                        tab,
                        color = if (selected == index) PrimaryColor else TextSecondary,
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun StatsRow(messages: List<SmsMessage>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Total", messages.size.toString(), PrimaryColor, Modifier.weight(1f))
        StatCard("Spam", messages.count { it.category == SmsCategory.SPAM }.toString(), SpamColor, Modifier.weight(1f))
        StatCard("Bank", messages.count { it.category == SmsCategory.BANK }.toString(), BankColor, Modifier.weight(1f))
        StatCard("OTP", messages.count { it.category == SmsCategory.OTP }.toString(), OtpColor, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun MessagesList(messages: List<SmsMessage>, selectedTab: Int, tabs: List<String>) {
    val filtered = when (selectedTab) {
        0 -> messages
        1 -> messages.filter { it.category == SmsCategory.PERSONAL }
        2 -> messages.filter { it.category == SmsCategory.BANK }
        3 -> messages.filter { it.category == SmsCategory.OTP }
        4 -> messages.filter { it.category == SmsCategory.SPAM }
        else -> messages
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtered) { message ->
            MessageCard(message)
        }
    }
}

@Composable
fun MessageCard(message: SmsMessage) {
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
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    message.address.take(1).uppercase(),
                    color = categoryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        message.address,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            categoryLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = categoryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    message.body,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun SmartBottomBar() {
    NavigationBar(containerColor = SurfaceDark) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Message, contentDescription = "Messages") },
            label = { Text("Messages") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                unselectedIconColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
            label = { Text("Stats") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                unselectedIconColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryColor,
                selectedTextColor = PrimaryColor,
                unselectedIconColor = TextSecondary
            )
        )
    }
}

@Composable
fun SmartFAB() {
    FloatingActionButton(
        onClick = {},
        containerColor = PrimaryColor,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Edit, contentDescription = "New SMS", tint = Color.White)
    }
}
