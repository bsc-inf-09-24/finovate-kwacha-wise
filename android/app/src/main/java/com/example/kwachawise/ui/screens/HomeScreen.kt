package com.example.kwachawise.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kwachawise.R
import com.example.kwachawise.data.AppTheme
import com.example.kwachawise.navigation.Screen
import com.example.kwachawise.ui.theme.ThemeAssets
import java.util.Locale

@Composable
fun HomeScreen(
    balance: Double = 20983.0,
    pendingCount: Int = 2,
    unreadNotificationsCount: Int = 0,
    appTheme: AppTheme = AppTheme.SYSTEM,
    onNavigate: (String) -> Unit,
    onThemeToggle: () -> Unit
) {
    val assets = ThemeAssets.current()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            HomeTopBar(
                unreadNotificationsCount = unreadNotificationsCount,
                appTheme = appTheme,
                onNotificationsClick = { onNavigate(Screen.Notifications.route) },
                onSearchClick = { onNavigate(Screen.Search.route) },
                onThemeToggle = onThemeToggle
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            BalanceCard(balance)

            if (pendingCount > 0) {
                PendingCTA(pendingCount, onClick = { onNavigate(Screen.ReviewPending.route) })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(
                        title = "Transactions",
                        icon = Icons.AutoMirrored.Filled.List,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.Transactions.route) }
                    )
                    ActionCard(
                        title = "Insights",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.Insights.route) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ActionCard(
                    title = "Add Cash Entry",
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigate(Screen.AddCashEntry.route) }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { onNavigate(Screen.PasteSms.route) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Paste SMS Text")
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    unreadNotificationsCount: Int = 0,
    appTheme: AppTheme,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onThemeToggle: () -> Unit
) {
    val assets = ThemeAssets.current()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = assets.appIcon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Hello Amikhy", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Your finances are looking good", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }
        Row {
            IconButton(onClick = onThemeToggle) {
                Icon(
                    imageVector = if (appTheme == AppTheme.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = Color.White
                )
            }
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge {
                                Text(unreadNotificationsCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    val amountFormatted = String.format(Locale.US, "%,.0f", balance)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Your available balance is", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Text(text = "K$amountFormatted", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("By this time last month, you spent slightly higher", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
fun PendingCTA(count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Sort your transactions", color = Color.White, fontWeight = FontWeight.Bold)
                Text("$count pending items to review", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
