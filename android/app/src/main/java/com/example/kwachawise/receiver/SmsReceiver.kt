package com.example.kwachawise.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.kwachawise.data.AppDatabase
import com.example.kwachawise.data.toEntity
import com.example.kwachawise.utils.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val body = message.messageBody
                Log.d("SmsReceiver", "Received SMS: $body")
                
                val transaction = SmsParser.parse(body)
                if (transaction != null) {
                    Log.d("SmsReceiver", "Parsed Transaction: $transaction")
                    saveTransaction(context, transaction.toEntity())
                }
            }
        }
    }

    private fun saveTransaction(context: Context, transaction: com.example.kwachawise.data.TransactionEntity) {
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            db.transactionDao().insertTransaction(transaction)
        }
    }
}
