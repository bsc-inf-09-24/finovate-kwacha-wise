package com.example.kwachawise.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val description: String,
    val rawText: String?,
    val tag: String,
    val date: String,
    val note: String?
)

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        description = description,
        rawText = rawText,
        tag = TransactionTag.valueOf(tag),
        date = date,
        note = note
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        description = description,
        rawText = rawText,
        tag = tag.name,
        date = date,
        note = note
    )
}
