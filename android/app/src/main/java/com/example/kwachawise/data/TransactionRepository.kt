package com.example.kwachawise.data

import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import com.example.kwachawise.models.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
        .map { entities -> entities.map { it.toDomain() } }

    val pendingTransactions: Flow<List<Transaction>> = transactionDao.getTransactionsByTag(TransactionTag.UNSORTED.name)
        .map { entities -> entities.map { it.toDomain() } }

    val sortedTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
        .map { entities -> 
            entities.filter { it.tag != TransactionTag.UNSORTED.name }
                .map { it.toDomain() } 
        }

    val balance: Flow<Double> = allTransactions.map { transactions ->
        transactions.sumOf { 
            if (it.type == TransactionType.INCOME) it.amount else -it.amount 
        }
    }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    suspend fun updateTag(transactionId: String, tag: TransactionTag, note: String?) {
        val entity = transactionDao.getTransactionById(transactionId)
        if (entity != null) {
            transactionDao.updateTransaction(entity.copy(tag = tag.name, note = note))
        }
    }

    suspend fun getAiInsights(): String? {
        val transactions = transactionDao.getAllTransactions().map { entities ->
            entities.joinToString("\n") { 
                "${it.date}: ${it.type} ${it.amount} - ${it.description}" 
            }
        }
        // This is a flow, but we need a one-time snapshot or the latest value.
        // For simplicity, we'll collect the latest list.
        // Actually, repository should ideally have a non-flow way or we collect in VM.
        // Let's just pass the summary string.
        return null // Will be handled in VM or by passing data
    }
}
