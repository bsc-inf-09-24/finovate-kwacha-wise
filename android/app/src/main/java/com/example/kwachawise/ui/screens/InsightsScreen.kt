package com.example.kwachawise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kwachawise.models.MockData
import com.example.kwachawise.ui.theme.KwachaSuccess
import com.example.kwachawise.ui.theme.KwachaWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    healthSignal: String? = null,
    onViewInsights: () -> Unit,
    onBack: () -> Unit
) {
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(healthSignal) {
        if (healthSignal != null) {
            loading = false
        }
    }

    val signal = remember(healthSignal) {
        healthSignal?.lineSequence()?.find { it.startsWith("SIGNAL:") }
            ?.substringAfter("SIGNAL:")?.trim() ?: "Calculating..."
    }

    val recommendations = remember(healthSignal) {
        healthSignal?.lineSequence()?.filter { it.startsWith("ADVICE:") }
            ?.map { it.substringAfter("ADVICE:").trim() }?.toList() ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (healthSignal == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator()
                    } else {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = if (loading) "Analyzing transactions..." else "Get your insights",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "If you want to check the financial health of your business, continue by clicking the button below.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { 
                        loading = true
                        onViewInsights() 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !loading
                ) {
                    Text("View Insights")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = "Financial Health",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HealthSignalCard(signal)
                }

                item {
                    Text(
                        text = "Recommendations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(recommendations) { recommendation ->
                    RecommendationItem(recommendation)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { onViewInsights() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Refresh Analysis")
                    }
                }
            }
        }
    }
}

@Composable
fun HealthSignalCard(signal: String) {
    val color = when (signal) {
        "Healthy" -> KwachaSuccess
        "Watch" -> KwachaWarning
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = signal,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RecommendationItem(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}
