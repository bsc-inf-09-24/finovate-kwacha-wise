package com.example.kwachawise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import com.example.kwachawise.models.TransactionType
import com.example.kwachawise.utils.DateTimeUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    groupedTransactions: Map<String, List<Transaction>>,
    searchQuery: String,
    selectedFilter: TransactionTag?,
    onQueryChange: (String) -> Unit,
    onFilterSelected: (TransactionTag?) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = { Text("Transactions") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search description or category") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                FilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected
                )
            }
        }
    ) { padding ->
        if (groupedTransactions.isEmpty()) {
            EmptyTransactionsState(
                isSearch = searchQuery.isNotEmpty() || selectedFilter != null,
                onClearFilters = {
                    onQueryChange("")
                    onFilterSelected(null)
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedTransactions.forEach { (dateHeader, transactions) ->
                    stickyHeader {
                        Text(
                            text = dateHeader,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(transactions) { transaction ->
                        TransactionListItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsState(
    isSearch: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearch) Icons.Default.Search else Icons.AutoMirrored.Filled.List,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearch) "No transactions found" else "No transactions yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (isSearch) {
            Text(
                text = "Try adjusting your search or filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onClearFilters) {
                Text("Clear All Filters")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateTimeUtils.formatDate(transaction.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = DateTimeUtils.formatTime(transaction.createdAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}K${String.format(Locale.US, "%,.2f", transaction.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}
