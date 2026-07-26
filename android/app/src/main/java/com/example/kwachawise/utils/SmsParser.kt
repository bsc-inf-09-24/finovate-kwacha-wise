package com.example.kwachawise.utils

import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import com.example.kwachawise.models.TransactionType
import java.util.*

object SmsParser {
    // Priority order for currency codes to prevent partial matches (e.g. MK vs MKW)
    private const val CURRENCY_REGEX = """(?:MWK|MKW|MK|K)?\s*([\d,]+(?:\.\d{2})?)(?:\s*(?:MWK|MKW|MK))?"""

    // Amount patterns in priority order
    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?:Amt|Amount|Total):\s*$CURRENCY_REGEX""", RegexOption.IGNORE_CASE),
        Regex("""(?:received|spent|paid|sent)\s+.*?$CURRENCY_REGEX""", RegexOption.IGNORE_CASE),
        Regex("""Cash\s+In\s+.*?$CURRENCY_REGEX""", RegexOption.IGNORE_CASE)
    )

    // Balance patterns in priority order
    private val BALANCE_LABELS = Regex("""(?:Bal|Balance|Avail Bal|New Balance):\s*$CURRENCY_REGEX""", RegexOption.IGNORE_CASE)
    private val MAIN_ACCOUNT = Regex("""Main\s+Account:\s*$CURRENCY_REGEX""", RegexOption.IGNORE_CASE)
    private val ANY_ACCOUNT = Regex("""Account:\s*$CURRENCY_REGEX""", RegexOption.IGNORE_CASE)

    // Labels to explicitly skip when looking for a general amount
    private val EXCLUSION_LABELS = listOf("Fee:", "Levy:", "Charge:", "Ref:", "Bal:", "Balance:")

    fun parse(smsBody: String): Transaction? {
        val amount = parseAmount(smsBody) ?: return null
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

    fun parseAmount(sms: String): Double? {
        // 1. Try priority patterns first
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(sms)
            if (match != null) {
                val valueStr = match.groups[1]?.value ?: continue
                val amount = valueStr.replace(",", "").toDoubleOrNull()
                if (amount != null && amount > 0) return amount
            }
        }

        // 2. Fallback: Find any currency-shaped number that isn't preceded by an exclusion label
        val allNumbers = Regex(CURRENCY_REGEX).findAll(sms)
        for (match in allNumbers) {
            val startIndex = match.range.first
            val precedingText = sms.substring(0, startIndex).trim()
            
            val isExcluded = EXCLUSION_LABELS.any { precedingText.endsWith(it, ignoreCase = true) }
            if (!isExcluded) {
                val amount = match.groups[1]?.value?.replace(",", "")?.toDoubleOrNull()
                if (amount != null && amount > 0) return amount
            }
        }
        
        return null
    }

    fun parseBalance(sms: String): Double? {
        val labelMatches = BALANCE_LABELS.findAll(sms).toList()
        if (labelMatches.isNotEmpty()) {
            return labelMatches.last().groups[1]?.value?.replace(",", "")?.toDoubleOrNull()
        }

        val mainMatch = MAIN_ACCOUNT.find(sms)
        if (mainMatch != null) {
            return mainMatch.groups[1]?.value?.replace(",", "")?.toDoubleOrNull()
        }

        val accountMatches = ANY_ACCOUNT.findAll(sms).toList()
        if (accountMatches.isNotEmpty()) {
            return accountMatches.last().groups[1]?.value?.replace(",", "")?.toDoubleOrNull()
        }

        return null
    }

    private fun inferType(body: String): TransactionType {
        val lowerBody = body.lowercase()
        return when {
            lowerBody.contains("received") || lowerBody.contains("cash in") -> TransactionType.INCOME
            lowerBody.contains("paid") || lowerBody.contains("spent") || lowerBody.contains("sent") -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE
        }
    }

    private fun extractDescription(body: String): String? {
        if (body.contains("ESCOM", ignoreCase = true)) return "ESCOM Payment"
        if (body.contains("Cash In", ignoreCase = true)) return "Cash In"
        
        val receivedMatch = Regex("""received\s+.*?from\s+([^0-9\.]+?)(?:\s+on|\s+at|\.)""", RegexOption.IGNORE_CASE).find(body)
        if (receivedMatch != null) {
            return "Received from ${receivedMatch.groups[1]?.value?.trim()}"
        }
        
        return null
    }

    private fun extractDate(body: String): String? {
        val datePattern = Regex("""(\d{2}/\d{2}/\d{2,4}\s+\d{2}:\d{2}(?::\d{2})?\s?(?:AM|PM)?)""", RegexOption.IGNORE_CASE)
        val dateMatch = datePattern.find(body)
        return dateMatch?.groups[1]?.value
    }
}
