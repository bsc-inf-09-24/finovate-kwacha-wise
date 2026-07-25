# KwachaWise — Android Architecture (v3)

**Brand:** Kwacha Wize — indigo "k" mark (#5B4FE8-ish) on black rounded-square,
pixel-style wordmark. Icon assets in `kwachawise-android-icons.zip`
(mdpi→xxxhdpi mipmaps + Play Store 512px) — drop `mipmap-*/` into
`app/src/main/res/`.

**Changelog from v2:** adds manual cash entry (was promised in the concept
brief but missing from v2), a paste-SMS fallback screen (demo insurance),
and a financial health signal folded into the Groq output (concept brief
promises "financial health reports" — v2 only had generic recommendations).
No changes to the core store-first/sort-second/enrich-with-AI flow.

---

## 1. Stack (unchanged)

| Layer | Choice |
|---|---|
| Language / UI | Kotlin + Jetpack Compose |
| DB | Room (SQLite) |
| Networking | Retrofit + OkHttp → Groq |
| Background | `BroadcastReceiver` on `SMS_RECEIVED` |
| Async | Coroutines + Flow |

---

## 2. Data flow: store first, sort second, enrich with AI

```
[ Incoming SMS ]                    [ Manual entry: cash / paste-SMS ]
      │                                          │
      ▼                                          │
[ BroadcastReceiver ] ── extract body/sender     │
      │                                          │
      ▼                                          │
[ Regex: amount + balance ]                      │
      │                                          │
      ▼                                          ▼
[ Room: insert as UNSORTED* ] ◄──────────► [ Compose Feed: "Review Pending" ]
                                                    │
                                       user taps 💼 Business / 🏠 Personal
                                       + optional note
                                                    │
                                                    ▼
                                       [ Room: update type + description ]
                                                    │
                                    (on demand, not per-SMS) ▼
                                    [ Groq: batched insights + health signal ]
```

`*` Manual cash entries skip UNSORTED — the user tags type/description at
the point of entry, so they're written directly as BUSINESS/PERSONAL.
Paste-SMS entries go through the same regex path as real SMS and land as
UNSORTED like normal.

Groq stays out of the per-SMS/per-entry path entirely — it only runs when
the user opens Insights, and only over already-tagged rows.

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
    var description: String = "",
    val source: String = "SMS"       // "SMS" | "MANUAL" — NEW in v3
)

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'UNSORTED' ORDER BY timestamp DESC")
    fun getUnsorted(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type != 'UNSORTED' ORDER BY timestamp DESC")
    fun getTagged(): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET type = :type, description = :description WHERE id = :id")
    suspend fun updateTransactionTag(id: Int, type: String, description: String)

    @Query("""SELECT detectedBalance FROM transactions
              WHERE detectedBalance IS NOT NULL
              ORDER BY timestamp DESC LIMIT 1""")
    fun getLatestBalance(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE type = 'UNSORTED'")
    fun getUnsortedCount(): Flow<Int>
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

`SmsReceiver` writes straight to Room on `Dispatchers.IO`, unchanged from v2.

Home screen balance = latest non-null `detectedBalance` across all rows —
one query, no separate balance table.

---

## 5. NEW — Manual cash entry

Closes a gap between the concept brief ("allows users to record cash
deposits") and v2's architecture, which only modeled SMS-derived rows.

- Screen: simple form — amount, description, Business/Personal toggle
- Writes directly to Room as a **tagged** row (`source = "MANUAL"`), no
  UNSORTED step needed since the user already knows what it is
- Appears in the Transactions list alongside SMS-derived rows, distinguished
  by the `source` field
- Counts toward Groq insights the same as any tagged transaction

```kotlin
suspend fun insertManual(amount: Double, type: String, description: String) {
    dao.insertTransaction(
        TransactionEntity(
            rawSms = "Manual entry",
            sender = "MANUAL",
            timestamp = System.currentTimeMillis(),
            parsedAmount = amount,
            detectedBalance = null,
            type = type,
            description = description,
            source = "MANUAL"
        )
    )
}
```

---

## 6. NEW — Paste-SMS fallback

Demo insurance in case live SMS delivery is awkward on the presentation
device (emulator, no SIM, permission friction in front of judges).

- A text field where the user pastes SMS body text
- Runs through the **same** `SmsParser.parseAmount` / `parseBalance`
- Writes to Room as UNSORTED, `source = "SMS"`, `sender = "PASTED"`
- Then flows through the normal Review Pending → tag → insights pipeline
  with zero special-casing downstream

---

## 7. Groq — insights + health signal, called on demand

```kotlin
interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Header("Authorization") auth: String, @Body req: GroqRequest): GroqResponse
}
```

System prompt (updated to cover the concept brief's "financial health
reports" promise, which v2 didn't address):

```json
{
  "model": "llama-3.1-8b-instant",
  "messages": [
    {
      "role": "system",
      "content": "You are KwachaWise, an AI financial coach for Malawian micro-entrepreneurs. Analyze the categorized transactions. Respond with: (1) a one-line financial health signal — Healthy, Watch, or At Risk — with a short reason, (2) 3 short, actionable financial recommendations."
    },
    {
      "role": "user",
      "content": "[{\"type\":\"BUSINESS\",\"amount\":15000,\"desc\":\"Bought inventory\"},{\"type\":\"PERSONAL\",\"amount\":2000,\"desc\":\"Airtime\"}]"
    }
  ]
}
```

**Resilience requirement (new):** wrap the Groq call in try/catch with a
hardcoded fallback insights string. A flaky API key or no network must
never blank out the Insights screen during a live demo.

API key via `local.properties` → `BuildConfig`, never hardcoded — unchanged
from v2.

---

## 8. Screens (updated)

1. **Home** — balance (from `detectedBalance`), unsorted count badge, entry
   points to all other screens including manual entry
2. **Review Pending** — feed of `UNSORTED` rows (from SMS or pasted SMS),
   tap-to-tag Business/Personal + note
3. **Transactions** — full tagged list (SMS + manual), filterable by type
4. **Insights** — "Get insights" button → Groq call → health signal + 3
   recommendations, with offline fallback text
5. **Add Cash Transaction (NEW)** — manual entry form
6. **Paste SMS (NEW, optional)** — text field + parse button, demo fallback

---

## 9. Build order (24h budget, updated)

1. Room entity/DAO + SmsReceiver + regex — test against real SMS (2-3 hrs, critical path)
2. Review Pending feed wired to `getUnsorted()` Flow (2 hrs)
3. Transactions list + Home balance (2 hrs)
4. Manual cash entry screen (30 min — closes concept-brief gap)
5. Groq insights call with health signal + offline fallback (1-1.5 hrs)
6. Paste-SMS fallback screen (20-30 min — demo safety net, do this if time allows)
7. Launcher icons from `kwachawise-android-icons.zip` + app name (15 min)

Same caveat as before for a Play Store release: `RECEIVE_SMS` is a
restricted permission unless you're the default SMS handler — fine for a
sideloaded hackathon build, flag as a known next step for judges.
