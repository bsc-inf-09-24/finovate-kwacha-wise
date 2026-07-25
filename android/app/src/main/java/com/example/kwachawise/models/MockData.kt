package com.example.kwachawise.models

import java.util.UUID

object MockData {
    val transactions = listOf(
        Transaction(
            id = UUID.randomUUID().toString(),
            amount = 12000.0,
            description = "FDH Bank Transfer",
            rawText = "Txn: 123456 Amt: 12000.00 from FDH",
            tag = TransactionTag.UNSORTED,
            date = "Sep 01, 2:24 PM"
        ),
        Transaction(
            id = UUID.randomUUID().toString(),
            amount = 950.0,
            description = "NBM Payment",
            tag = TransactionTag.BUSINESS,
            date = "Sep 01, 10:15 AM"
        ),
        Transaction(
            id = UUID.randomUUID().toString(),
            amount = 1050.0,
            description = "Mpamba Cash In",
            tag = TransactionTag.PERSONAL,
            date = "Aug 31, 6:30 PM"
        ),
        Transaction(
            id = UUID.randomUUID().toString(),
            amount = 3332.0,
            description = "Escom Units",
            rawText = "You spent K3,332.00 on Escom",
            tag = TransactionTag.UNSORTED,
            date = "Aug 30, 9:00 AM"
        )
    )

    val insights = "Healthy"
    val recommendations = listOf(
        "Your business spending is down 15% this month.",
        "Consider setting aside K5,000 for upcoming tax payments.",
        "You have 3 unsorted transactions that could affect your report."
    )
}
