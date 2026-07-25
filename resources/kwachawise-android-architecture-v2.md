# KwachaWise — Android Architecture (v2)

**Brand:** Kwacha Wize — indigo "k" mark (#5B4FE8-ish) on black rounded-square,
pixel-style wordmark. Icon assets generated in `kwachawise-android-icons.zip`
(mdpi→xxxhdpi mipmaps + Play Store 512px) from the mark you supplied — drop
`mipmap-*/` straight into `app/src/main/res/`.

---

## 1. Stack (unchanged, confirmed fast)

| Layer | Choice |
|---|---|
| Language / UI | Kotlin + Jetpack Compose |
| DB | Room (SQLite) |
| Networking | Retrofit + OkHttp → Groq |
| Background | `BroadcastReceiver` on `SMS_RECEIVED` |
| Async | Coroutines + Flow |

---

## 2. Simplified flow: store first, sort second, enrich with AI

This is the right call — it removes provider-detection edge cases from the
critical path. Every SMS gets stored immediately, untagged; sorting and AI
happen later, async, off the phone's back.

```
[ Incoming SMS ]
      │
      ▼
[ BroadcastReceiver ] ── extract body, sender, timestamp
      │
      ▼
[ Regex: amount + balance ] ── best-effort, non-blocking
      │
      ▼
[ Room: insert as UNSORTED ] ◄────────► [ Compose Feed: "Review Pending" ]
                                                    │
                                       user taps 💼 Business / 🏠 Personal
                                       + optional note
                                                    │
                                                    ▼
                                       [ Room: update type + description ]
                                                    │
                                    (on demand, not per-SMS) ▼
                                       [ Groq: batched insights/summary ]
```

Key difference from v1: **Groq is not in the per-SMS path at all now.**
Categorization is manual (Business/Personal tap), and Groq only runs when
the user asks for insights — it summarizes already-tagged data instead of
classifying each transaction. Cheaper, faster, and removes Groq latency from
the ingestion path entirely — nothing to await while an SMS is being saved.

---

## 3. Room layer

```kotlin
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawSms: String,
    val sender: String,
    val timestamp: Long,
    val parsedAmount: Double?,
    val detectedBalance: Double?,
    var type: String = "UNSORTED",   // "BUSINESS" | "PERSONAL" | "UNSORTED"
    var description: String = ""
)

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'UNSORTED' ORDER BY timestamp DESC")
    fun getUnsorted(): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET type = :type, description = :description WHERE id = :id")
    suspend fun updateTransactionTag(id: Int, type: String, description: String)
}

@Database(entities = [TransactionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context) = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context, AppDatabase::class.java, "kwachawise.db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
```

---

## 4. SMS capture + regex (amount + running balance)

```xml
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<receiver android:name=".sms.SmsReceiver" android:exported="true">
    <intent-filter android:priority="999">
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

```kotlin
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString("") { it.messageBody }
        val sender = messages.firstOrNull()?.originatingAddress ?: return

        val entity = TransactionEntity(
            rawSms = body,
            sender = sender,
            timestamp = System.currentTimeMillis(),
            parsedAmount = SmsParser.parseAmount(body),
            detectedBalance = SmsParser.parseBalance(body)
        )

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.get(context.applicationContext).transactionDao().insertTransaction(entity)
        }
    }
}

object SmsParser {
    private val AMOUNT = Regex(
        """(?:MWK|MK|MKW)\s*([\d,]+(?:\.\d{2})?)""", RegexOption.IGNORE_CASE
    )
    private val BALANCE = Regex(
        """(?i)(?:new\s+balance|avail\s+bal|balance|bal)[:\s]*(?:MWK|MK|K)?\s*([\d,]+(?:\.\d{2})?)"""
    )

    fun parseAmount(sms: String): Double? =
        AMOUNT.find(sms)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

    fun parseBalance(sms: String): Double? =
        BALANCE.find(sms)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
}
```

Home screen's balance figure = most recent non-null `detectedBalance` across
all rows (`SELECT detectedBalance FROM transactions WHERE detectedBalance IS
NOT NULL ORDER BY timestamp DESC LIMIT 1`) — one query, no separate balance
table needed unless you want per-account balances later.

---

## 5. Groq — insights only, called on demand

```kotlin
interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Header("Authorization") auth: String, @Body req: GroqRequest): GroqResponse
}

data class GroqRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<Message>
)
data class Message(val role: String, val content: String)
data class GroqResponse(val choices: List<Choice>)
data class Choice(val message: Message)
```

Payload built from tagged rows only (`type != 'UNSORTED'`):

```json
{
  "model": "llama-3.1-8b-instant",
  "messages": [
    { "role": "system", "content": "You are KwachaWise, an AI financial coach for Malawian micro-entrepreneurs. Analyze the categorized transactions and give 3 short, actionable financial recommendations." },
    { "role": "user", "content": "[{\"type\":\"BUSINESS\",\"amount\":15000,\"desc\":\"Bought inventory\"},{\"type\":\"PERSONAL\",\"amount\":2000,\"desc\":\"Airtime\"}]" }
  ]
}
```

API key via `local.properties` → `BuildConfig`, never hardcoded:
```
// local.properties
groq.api.key=gsk_xxx
// app/build.gradle.kts
buildConfigField("String", "GROQ_API_KEY", "\"${'$'}{project.findProperty("groq.api.key")}\"")
```

---

## 6. Screens (minimum for demo)

1. **Home** — balance (from `detectedBalance`), unsorted count badge
2. **Review Pending** — feed of `UNSORTED` rows, tap-to-tag Business/Personal + note
3. **Transactions** — full tagged list, filterable
4. **Insights** — "Get insights" button → Groq call → 3 recommendations

---

## 7. Build order (24h budget)

1. Room entity/DAO + SmsReceiver + regex — copy-paste above, test against real SMS (2-3 hrs, critical path)
2. Manual paste-SMS fallback screen, same parser (30 min — demo safety net)
3. Review Pending feed wired to `getUnsorted()` Flow (2 hrs)
4. Transactions list + Home balance (2 hrs)
5. Groq insights call wired to a button (1 hr)
6. Launcher icons from `kwachawise-android-icons.zip` + app name (15 min)

Same caveat as before still holds for a Play Store release: `RECEIVE_SMS` is
a restricted permission unless you're the user's default SMS handler — fine
for a sideloaded hackathon build, flag as a known next step for judges.
