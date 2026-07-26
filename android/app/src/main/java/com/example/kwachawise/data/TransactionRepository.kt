package com.example.kwachawise.data

import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
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

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    suspend fun updateTag(transactionId: String, tag: TransactionTag, note: String?) {
        val entity = transactionDao.getTransactionById(transactionId)
        if (entity != null) {
            transactionDao.updateTransaction(entity.copy(tag = tag.name, note = note))
        }
    }
}
