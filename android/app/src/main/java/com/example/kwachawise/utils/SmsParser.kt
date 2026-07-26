package com.example.kwachawise.utils

import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import com.example.kwachawise.models.TransactionType
import java.util.*
import java.util.regex.Pattern

object SmsParser {
    fun parse(smsBody: String): Transaction? {
        val amount = extractAmount(smsBody) ?: return null
        val description = extractDescription(smsBody) ?: "Transaction"
        val date = extractDate(smsBody) ?: "Today"
        val type = inferType(smsBody)

        return Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            description = description,
            type = type,
            rawText = smsBody,
            tag = TransactionTag.UNSORTED,
            date = date
        )
    }

    private fun inferType(body: String): TransactionType {
        val lowerBody = body.lowercase()
        return when {
            lowerBody.contains("received") || lowerBody.contains("cash in") -> TransactionType.INCOME
            lowerBody.contains("paid") || lowerBody.contains("pay ") || lowerBody.contains("spent") -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE // Default to expense if unsure
        }
    }

    private fun extractAmount(body: String): Double? {
        // Pattern 1: Amt: 5,000.00MWK or Amount:200
        val amtPattern = Pattern.compile("(?:Amt|Amount):\\s?([\\d,.]+)")
        val amtMatcher = amtPattern.matcher(body)
        if (amtMatcher.find()) {
            return amtMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }

        // Pattern 2: MWK10,000 or MK 5,000 or MK5987.76
        val mwkPattern = Pattern.compile("(?:MWK|MK)\\s?([\\d,.]+)")
        val mwkMatcher = mwkPattern.matcher(body)
        if (mwkMatcher.find()) {
            return mwkMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }

        return null
    }

    private fun extractDescription(body: String): String? {
        if (body.contains("Pay ESCOM", ignoreCase = true) || body.contains("paid to ESCOM", ignoreCase = true)) {
            return "ESCOM Payment"
        }
        if (body.contains("Cash In", ignoreCase = true)) {
            return "Cash In"
        }
        if (body.contains("Received", ignoreCase = true)) {
            val fromPattern = Pattern.compile("from\\s+([^\\d]+)")
            val fromMatcher = fromPattern.matcher(body)
            if (fromMatcher.find()) {
                return "Received from ${fromMatcher.group(1)?.trim()}"
            }
            return "Received Money"
        }
        return null
    }

    private fun extractDate(body: String): String? {
        // Pattern: 25/07/2026 18:07:23 or 27/06/26 12:19 PM
        val datePattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{2,4}\\s+\\d{2}:\\d{2}(?::\\d{2})?\\s?(?:AM|PM)?)", Pattern.CASE_INSENSITIVE)
        val dateMatcher = datePattern.matcher(body)
        if (dateMatcher.find()) {
            return dateMatcher.group(1)
        }
        return null
    }
}
