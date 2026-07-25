package com.example.kwachawise.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SmsParserTest {

    @Test
    fun testParseCashIn() {
        val sms = "Cash In from 205586-STANLEY MKUMBA on 25/07/2026 18:07:23. Amt: 5,000.00MWK Fee: 0.00MWK Levy: MWK0.00 Ref: DGP133J6PX7 Bal: 6,153.82MWK"
        val transaction = SmsParser.parse(sms)
        assertNotNull(transaction)
        assertEquals(5000.0, transaction?.amount)
        assertEquals("Cash In", transaction?.description)
        assertEquals("25/07/2026 18:07:23", transaction?.date)
    }

    @Test
    fun testParseEscom() {
        val sms = "Pay ESCOM successful: Token:62019572325916589786 MeterNo.:94101325705 Amount:200 Units:2.1 Fee:0.00MWK Ref: DGO933FNUYV Bal: 1,143.82MWK"
        val transaction = SmsParser.parse(sms)
        assertNotNull(transaction)
        assertEquals(200.0, transaction?.amount)
        assertEquals("ESCOM Payment", transaction?.description)
    }

    @Test
    fun testParseReceived() {
        val sms = "PP260627.1219.S50021. Received MWK10,000 from Yamikani Laja 0987616610 on 27/06/26 12:19 PM.Bal: MK 12987.76."
        val transaction = SmsParser.parse(sms)
        assertNotNull(transaction)
        assertEquals(10000.0, transaction?.amount)
        assertEquals("Received from Yamikani Laja", transaction?.description)
        assertEquals("27/06/26 12:19 PM", transaction?.date)
    }

    @Test
    fun testParseEscomConfirmed() {
        val sms = "BP260628.1705.2G5186 Confirmed. MK 5,000 successfully paid to ESCOM on 28/06/26 05:05 PM. Service Fee: MK0. Bal: MK5987.76."
        val transaction = SmsParser.parse(sms)
        assertNotNull(transaction)
        assertEquals(5000.0, transaction?.amount)
        assertEquals("ESCOM Payment", transaction?.description)
        assertEquals("28/06/26 05:05 PM", transaction?.date)
    }
}
