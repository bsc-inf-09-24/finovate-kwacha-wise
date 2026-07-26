package com.example.kwachawise.models

enum class TransactionTag {
    BUSINESS, PERSONAL, UNSORTED
}

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val id: String,
    val amount: Double,
    val description: String,
    val type: TransactionType,
    val rawText: String? = null,
    val tag: TransactionTag = TransactionTag.UNSORTED,
    val date: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
