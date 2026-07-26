package com.example.kwachawise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kwachawise.data.GroqClient
import com.example.kwachawise.data.TransactionRepository
import com.example.kwachawise.models.Notification
import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    
    val pendingTransactions: StateFlow<List<Transaction>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortedTransactions: StateFlow<List<Transaction>> = repository.sortedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Double> = repository.balance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingCount: StateFlow<Int> = pendingTransactions.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _aiInsights = MutableStateFlow<String?>(null)
    val aiInsights: StateFlow<String?> = _aiInsights.asStateFlow()

    // Search Logic
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Transaction>> = combine(
        sortedTransactions,
        _searchQuery
    ) { transactions, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            transactions.filter { 
                it.description.contains(query, ignoreCase = true) || 
                it.tag.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Notifications Logic
    private val _notifications = MutableStateFlow(
        listOf(
            Notification(
                id = UUID.randomUUID().toString(),
                title = "New Transaction",
                message = "You received MK 10,000 from Yamikani.",
                date = "Today, 10:00 AM"
            ),
            Notification(
                id = UUID.randomUUID().toString(),
                title = "AI Insight Ready",
                message = "Your weekly financial report is ready to view.",
                date = "Yesterday, 6:00 PM"
            )
        )
    )
    val notifications = _notifications.asStateFlow()

    val unreadNotificationsCount = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun updateTransactionTag(transactionId: String, tag: TransactionTag, note: String?) {
        viewModelScope.launch {
            repository.updateTag(transactionId, tag, note)
        }
    }

    fun addManualTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insert(transaction)
        }
    }

    fun fetchAiInsights() {
        viewModelScope.launch {
            val transactions = sortedTransactions.value
            if (transactions.isEmpty()) {
                _aiInsights.value = "SIGNAL: Watch\nADVICE: Start recording transactions to get AI insights.\nADVICE: Sort pending SMS entries.\nADVICE: Use 'Add Cash Entry' for manual records."
                return@launch
            }

            val summary = transactions.joinToString("\n") { 
                "${it.date}: ${it.type} ${it.amount} - ${it.description}" 
            }
            val insights = GroqClient.getFinancialInsights(summary)
            if (insights == null) {
                _aiInsights.value = "SIGNAL: Watch\nADVICE: Unable to connect to AI advisor.\nADVICE: Check your internet connection.\nADVICE: Try again in a few minutes."
            } else {
                _aiInsights.value = insights
            }
        }
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
