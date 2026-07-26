package com.example.kwachawise.utils

import com.example.kwachawise.models.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParserTest {

    @Test
    fun testCaseA_MultipleAccounts() {
        val sms = "Micro Merchant Account: MWK0.00MWK \nMain Account: MWK5,193.82MWK.\nTransact more to increase Mpamba Ndikankhe limit amount."
        val balance = SmsParser.parseBalance(sms)
        assertEquals(5193.82, balance ?: 0.0, 0.001)
    }

    @Test
    fun testCaseB_CashIn() {
        val sms = """
            Cash In from 205586-STANLEY MKUMBA on 25/07/2026 18:07:23.
            Amt: 5,000.00MWK
            Fee: 0.00MWK
            Levy: MWK0.00
            Ref: DGP133J6PX7
            Bal: 6,153.82MWK
        """.trimIndent()
        val amount = SmsParser.parseAmount(sms)
        val balance = SmsParser.parseBalance(sms)
        
        assertEquals(5000.0, amount ?: 0.0, 0.001)
        assertEquals(6153.82, balance ?: 0.0, 0.001)
    }

    @Test
    fun testCaseC_InterestReceived() {
        val sms = "IS260726.0435.R06207. You have received  MK 355.82 from MONEY TRUST INTEREST on 26/07/26 at 04:35 AM. Bal: MK 368.58. Comment: INTEREST Q2 2026."
        val amount = SmsParser.parseAmount(sms)
        val balance = SmsParser.parseBalance(sms)
        
        assertEquals(355.82, amount ?: 0.0, 0.001)
        assertEquals(368.58, balance ?: 0.0, 0.001)
    }

    @Test
    fun testEscomSpent() {
        val sms = "You spent K3,332.00 on Escom Units. Fee: K0.00. Bal: K1,143.82."
        val transaction = SmsParser.parse(sms)
        assertNotNull(transaction)
        assertEquals(3332.0, transaction?.amount ?: 0.0, 0.0)
        assertEquals(TransactionType.EXPENSE, transaction?.type)
    }

    @Test
    fun testNegative_NoParseableAmount() {
        val sms = "Your airtime balance is low. Please recharge."
        val amount = SmsParser.parseAmount(sms)
        val transaction = SmsParser.parse(sms)
        
        assertNull(amount)
        assertNull(transaction)
    }

    @Test
    fun testCurrencyFormats() {
        assertEquals(5000.0, SmsParser.parseAmount("Amt: 5,000.00MWK") ?: 0.0, 0.0)
        assertEquals(5000.0, SmsParser.parseAmount("Amt: MWK 5,000.00") ?: 0.0, 0.0)
        assertEquals(5000.0, SmsParser.parseAmount("Amt: MK 5,000.00") ?: 0.0, 0.0)
        assertEquals(5000.0, SmsParser.parseAmount("Amt: 5,000.00") ?: 0.0, 0.0)
    }

    @Test
    fun testBroadDetection() {
        val sms = "Paid to 0999123456 MK 10,000. Fee MK 50. Ref: 12345."
        val amount = SmsParser.parseAmount(sms)
        assertEquals(10000.0, amount ?: 0.0, 0.0)
    }

    @Test
    fun testIncomeDetection() {
        val interestSms = "You have received interest of MK 500."
        assertEquals(TransactionType.INCOME, SmsParser.parse(interestSms)?.type)
        
        val salarySms = "Salary deposit: MK 500,000."
        assertEquals(TransactionType.INCOME, SmsParser.parse(salarySms)?.type)
    }

    @Test
    fun testExpenseDetection() {
        val airtimeSms = "You bought airtime for MK 1,000."
        assertEquals(TransactionType.EXPENSE, SmsParser.parse(airtimeSms)?.type)
        
        val billSms = "Bill payment to Water Board MK 20,000."
        assertEquals(TransactionType.EXPENSE, SmsParser.parse(billSms)?.type)
    }
}
