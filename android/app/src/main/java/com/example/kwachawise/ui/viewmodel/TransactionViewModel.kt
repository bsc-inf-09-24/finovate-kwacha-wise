package com.example.kwachawise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kwachawise.data.AiAnalysisEntity
import com.example.kwachawise.data.GroqClient
import com.example.kwachawise.data.TransactionRepository
import com.example.kwachawise.models.Notification
import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import com.example.kwachawise.models.TransactionType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(FlowPreview::class)
class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    
    val pendingTransactions: StateFlow<List<Transaction>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortedTransactions: StateFlow<List<Transaction>> = repository.sortedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Double> = repository.balance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingCount: StateFlow<Int> = pendingTransactions.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val aiInsights: StateFlow<String?> = repository.latestAiAnalysis
        .map { it?.result }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val aiAnalysisHistory: StateFlow<List<AiAnalysisEntity>> = repository.aiAnalysisHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isNewDataAvailable: StateFlow<Boolean> = combine(
        sortedTransactions,
        repository.latestAiAnalysis
    ) { transactions, latestAnalysis ->
        if (transactions.isEmpty()) return@combine false
        if (latestAnalysis == null) return@combine true
        
        val currentHash = transactions.hashCode()
        currentHash != latestAnalysis.transactionHash
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Search and Filtering Logic
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<TransactionTag?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    val searchResults: StateFlow<List<Transaction>> = combine(
        sortedTransactions,
        _searchQuery.debounce(300)
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

    val filteredTransactions: StateFlow<Map<String, List<Transaction>>> = combine(
        sortedTransactions,
        _searchQuery.debounce(300),
        _selectedTag
    ) { transactions, query, tag ->
        val filtered = transactions.filter { transaction ->
            val matchesQuery = query.isBlank() || 
                    transaction.description.contains(query, ignoreCase = true) ||
                    transaction.tag.name.contains(query, ignoreCase = true)
            
            val matchesTag = tag == null || transaction.tag == tag
            
            matchesQuery && matchesTag
        }
        
        // Grouping logic
        filtered.groupBy { com.example.kwachawise.utils.DateTimeUtils.getDayHeader(it.createdAt) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: TransactionTag?) {
        _selectedTag.value = tag
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

    // Documents logic
    val allDocuments = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGeneratingDocument = MutableStateFlow(false)
    val isGeneratingDocument = _isGeneratingDocument.asStateFlow()

    fun generateDocument(startDate: Long, endDate: Long, title: String) {
        viewModelScope.launch {
            _isGeneratingDocument.value = true
            try {
                val transactions = sortedTransactions.value.filter {
                    it.createdAt in startDate..endDate
                }

                if (transactions.isEmpty()) {
                    // Handle empty state if needed
                    return@launch
                }

                val summary = transactions.joinToString("\n") {
                    "${com.example.kwachawise.utils.DateTimeUtils.formatDate(it.createdAt)}: ${it.type} ${it.amount} - ${it.description}"
                }

                val periodStr = "${com.example.kwachawise.utils.DateTimeUtils.formatDate(startDate)} to ${com.example.kwachawise.utils.DateTimeUtils.formatDate(endDate)}"
                val report = GroqClient.generateBankReport("Amikhy's Business", periodStr, summary)

                if (report != null) {
                    val document = com.example.kwachawise.data.DocumentEntity(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        content = report,
                        timestamp = System.currentTimeMillis(),
                        startDate = startDate,
                        endDate = endDate
                    )
                    repository.saveDocument(document)
                }
            } finally {
                _isGeneratingDocument.value = false
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun finalizeTransaction(transactionId: String, type: TransactionType, tag: TransactionTag, note: String?) {
        viewModelScope.launch {
            repository.finalizeTransaction(transactionId, type, tag, note)
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
                val fallback = "SIGNAL: Watch\nADVICE: Start recording transactions to get AI insights.\nADVICE: Sort pending SMS entries.\nADVICE: Use 'Add Cash Entry' for manual records."
                repository.saveAiAnalysis(AiAnalysisEntity(UUID.randomUUID().toString(), fallback, System.currentTimeMillis(), transactions.hashCode()))
                return@launch
            }

            val summary = transactions.joinToString("\n") { 
                "${it.date}: ${it.type} ${it.amount} - ${it.description}" 
            }
            val insights = GroqClient.getFinancialInsights(summary)
            if (insights == null) {
                // We don't save nulls to history, but we could notify UI via a temporary state if needed
                // For now, let's just keep the last successful analysis
            } else {
                repository.saveAiAnalysis(
                    AiAnalysisEntity(
                        id = UUID.randomUUID().toString(),
                        result = insights,
                        timestamp = System.currentTimeMillis(),
                        transactionHash = transactions.hashCode()
                    )
                )
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
