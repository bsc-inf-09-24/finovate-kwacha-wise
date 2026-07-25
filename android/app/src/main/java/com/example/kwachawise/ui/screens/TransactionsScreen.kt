package com.example.kwachawise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kwachawise.models.MockData
import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<Transaction> = MockData.transactions,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf<TransactionTag?>(null) }
    
    val filteredTransactions = if (selectedFilter == null) {
        transactions
    } else {
        transactions.filter { it.tag == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open full filters */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTransactions) { transaction ->
                    TransactionListItem(transaction)
                }
            }
        }
    }
}

@Composable
fun FilterChips(
    selectedFilter: TransactionTag?,
    onFilterSelected: (TransactionTag?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            label = { Text("All") }
        )
        FilterChip(
            selected = selectedFilter == TransactionTag.BUSINESS,
            onClick = { onFilterSelected(TransactionTag.BUSINESS) },
            label = { Text("Business") }
        )
        FilterChip(
            selected = selectedFilter == TransactionTag.PERSONAL,
            onClick = { onFilterSelected(TransactionTag.PERSONAL) },
            label = { Text("Personal") }
        )
        FilterChip(
            selected = selectedFilter == TransactionTag.UNSORTED,
            onClick = { onFilterSelected(TransactionTag.UNSORTED) },
            label = { Text("Unsorted") }
        )
    }
}

@Composable
fun TransactionListItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (transaction.tag) {
                            TransactionTag.BUSINESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            TransactionTag.PERSONAL -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            TransactionTag.UNSORTED -> Color.Gray.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (transaction.tag) {
                        TransactionTag.BUSINESS -> "💼"
                        TransactionTag.PERSONAL -> "🏠"
                        TransactionTag.UNSORTED -> "❓"
                    },
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = transaction.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "K${String.format(Locale.US, "%,.2f", transaction.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.amount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
