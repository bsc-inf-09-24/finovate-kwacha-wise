package com.example.kwachawise.models

enum class TransactionTag {
    BUSINESS, PERSONAL, UNSORTED
}

data class Transaction(
    val id: String,
    val amount: Double,
    val description: String,
    val rawText: String? = null,
    val tag: TransactionTag = TransactionTag.UNSORTED,
    val date: String,
    val note: String? = null
)
