# Kwacha Wize

**Financial inclusion through intelligent, automated record-keeping.**

Kwacha Wize is an AI-powered Android app that helps Malawian micro-entrepreneurs
automatically turn everyday mobile money and cash transactions into clean,
organized financial records — separating business from personal spending
without manual bookkeeping, and generating AI-driven financial insights to
help SMEs become credit-ready.

Built in a 48-hour hackathon.

---

## The Problem

Many Malawian SMEs struggle to access formal financing because they lack
reliable financial records. Although most of their transactions run through
Airtel Money, TNM Mpamba, and cash, these records are scattered or mixed
with personal expenses — making it difficult for banks to assess financial
health and creditworthiness.

Without consistent, verifiable records, even financially active SMEs are
treated as high-risk or "unbanked" by formal lenders, locking out businesses
that are, in practice, viable.

## The Solution

Kwacha Wize is a financial assistant that:

1. **Captures transactions automatically** — a background listener reads
   incoming Airtel Money, TNM Mpamba, and bank alert SMS messages and stores
   them the instant they arrive, with zero user effort.
2. **Lets users record cash** — for transactions that never touch a phone
   (informal cash sales, cash purchases), a simple manual entry form keeps
   the record complete.
3. **Separates business from personal in one tap** — every captured
   transaction sits in a "Review Pending" queue until the user tags it
   💼 Business or 🏠 Personal, with an optional note.
4. **Turns tagged data into insight** — on demand, tagged transactions are
   sent to an AI model (Groq / Llama 3.1) which returns a short financial
   health signal (Healthy / Watch / At Risk) and three actionable
   recommendations.

The result: a Malawian SME owner ends each week with a clean, categorized
transaction history — the exact kind of record a lender needs to assess
creditworthiness, produced with almost no manual effort.

---

## Why This Design (Key Product Decisions)

**Store first, sort second, enrich with AI last.**
Every incoming SMS is saved immediately and untagged. Classification
(Business/Personal) is a deliberate, later, human decision — not an
AI guess made in the background. This removes fragile provider-detection
logic from the critical path and keeps the AI model in the loop only where
it adds real value: summarizing already-clean data, not guessing at messy
data in real time.

**AI is for insight, not classification.**
Groq is never called per-transaction. It's called once, on demand, over a
batch of already-tagged transactions — cheaper, faster, and removes AI
latency and cost from the core capture flow entirely.

**Manual cash entry, because not every transaction leaves a paper trail.**
Cash is still how a large share of informal commerce in Malawi happens.
A transaction record that only covers mobile money isn't representative
enough to be useful for a lender, so manual entry is a first-class part of
the flow, not an afterthought.

**A simple, hub-based navigation, not a busy multi-tab dashboard.**
Given the time budget, the UI is intentionally centered on one Home screen
with clear entry points, rather than a persistent tab bar with multiple
parallel dashboards. This kept the build achievable in 48 hours without
compromising the core user journey: capture → review → tag → get insight.

---

## User Flow

```
Splash
  │
  ▼
Welcome ("Let's get started")
  │
  ▼
Home ── aggregated balance, unsorted count badge
  │
  ├──► Review Pending ── tap 💼 Business / 🏠 Personal, optional note
  │
  ├──► Add Cash Entry ── amount, description, Business/Personal toggle
  │
  ├──► Transactions ── full tagged list, filterable
  │
  └──► Insights ── "Get insights" → health signal + 3 recommendations
```

---

## Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Language / UI | Kotlin + Jetpack Compose | Modern declarative UI, fast to build |
| Local storage | Room (SQLite) | Offline-first, zero setup overhead |
| Networking | Retrofit + OkHttp → Groq API | Ultra-fast, low-cost LLM inference |
| Background capture | `BroadcastReceiver` on `SMS_RECEIVED` | Native, no polling, no extra permissions beyond SMS |
| Async | Kotlin Coroutines + Flow | Reactive UI updates from Room, non-blocking network calls |
| AI model | Llama 3.1 8B Instant (via Groq) | Fast enough for on-demand insight generation, low cost |

No backend server, no cloud database — the app is fully offline-capable
except for the on-demand AI insights call.

---

## How Transaction Capture Works

1. A `BroadcastReceiver` listens for incoming SMS.
2. Regex extracts the transaction amount and, where present, the account
   balance from the message body — covering Airtel Money, TNM Mpamba, and
   common bank alert formats.
3. The transaction is written to Room immediately as `UNSORTED`.
4. The Home screen's balance figure is simply the most recent detected
   balance across all stored transactions — no separate balance table
   needed.
5. The user later tags the transaction from the Review Pending screen.

Manual cash transactions skip the `UNSORTED` step entirely — the user
tags type and description at the point of entry.

---

## AI Insights

When the user requests insights, all tagged (non-`UNSORTED`) transactions
are batched into a lightweight JSON payload and sent to Groq with a system
prompt instructing it to act as a financial coach for Malawian
micro-entrepreneurs. The model returns:

- A one-line financial health signal — **Healthy / Watch / At Risk**
- Three short, actionable recommendations

If the AI call fails (no network, invalid key), the app falls back to a
generic offline recommendation set rather than showing an error — the
insights screen never goes blank during a demo.

---

## Known Limitations / Next Steps

- `RECEIVE_SMS` is a restricted Android permission unless Kwacha Wize is
  set as the user's default SMS handler. This is acceptable for a
  sideloaded hackathon build; becoming a default SMS handler (or migrating
  to a Play-Store-compliant capture method) is the clear next step for a
  production release.
- Regex-based parsing currently targets the message formats used by
  Airtel Money, TNM Mpamba, and common bank alerts; broader provider
  coverage would need a wider sample set of real SMS formats.
- No cloud sync or multi-device support yet — all data is local to the
  device via Room.
- No authentication layer — onboarding is a static welcome screen, not a
  real account system, for this build.
- Financial health signal is currently AI-generated free text rather than
  a formally modeled credit-scoring algorithm; a production version aimed
  at lenders would need a defined, auditable scoring methodology.

---

## Team
* Mike Prosper Kamanga - UI/UX Designer and Developer
* Patrick Solomon - Developer
* Thokozani Mofolo - Project Manager / Communicator (Group Leader)
* Denis Decal - Business Strategist
* Dominic Smith - Researcher
