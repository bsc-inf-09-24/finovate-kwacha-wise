# Implementation Plan - Transaction Functionality & SMS Integration

Implement a robust, lightweight architecture to automatically capture transactions from SMS and allow users to sort them into Business or Personal categories using Room for persistence.

## User Review Required

> [!IMPORTANT]
> **SMS Permissions**: The app will require `RECEIVE_SMS` and `READ_SMS` permissions. We need to implement a permission request flow in the UI.
> **SMS Parsing Logic**: We will start with a generic parser for common Malawian banking SMS formats (e.g., FDH, NBM, Mpamba). We may need to refine this based on actual SMS samples.

## Proposed Changes

### Dependencies & Setup

#### [MODIFY] [libs.versions.toml](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/gradle/libs.versions.toml)
- Add Room library versions and definitions.
- Add KSP (Kotlin Symbol Processing) plugin.

#### [MODIFY] [build.gradle.kts (App)](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/build.gradle.kts)
- Apply KSP plugin.
- Add Room implementation and compiler dependencies.

### Data Layer (Room)

#### [NEW] [TransactionEntity.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/data/TransactionEntity.kt)
- Define a Room entity that maps to our `Transaction` model.

#### [NEW] [TransactionDao.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/data/TransactionDao.kt)
- Define methods for inserting, updating, and querying transactions (filtering by `tag`).

#### [NEW] [AppDatabase.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/data/AppDatabase.kt)
- Room database singleton.

### SMS Integration

#### [NEW] [SmsReceiver.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/receiver/SmsReceiver.kt)
- A `BroadcastReceiver` that listens for incoming SMS.
- Extracts amount, description, and date from the message body.
- Inserts a new `UNSORTED` transaction into the database.

#### [NEW] [SmsParser.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/utils/SmsParser.kt)
- Utility to extract transaction details from raw SMS text using regex.

### Architecture (ViewModel & Repository)

#### [NEW] [TransactionRepository.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/data/TransactionRepository.kt)
- Mediates between the database and the ViewModels.

#### [NEW] [TransactionViewModel.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/ui/viewmodel/TransactionViewModel.kt)
- Manages the state for `TransactionsScreen` and `ReviewPendingScreen`.
- Exposes `Flow`s for pending and sorted transactions.

### UI Integration

#### [MODIFY] [MainActivity.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/MainActivity.kt)
- Handle SMS permission requests.
- Initialize the `TransactionViewModel` and pass it to screens.

#### [MODIFY] [ReviewPendingScreen.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/ui/screens/ReviewPendingScreen.kt)
- Connect to `ViewModel` to display real pending transactions.
- Call `ViewModel.updateTransactionTag()` when the user sorts a transaction.

#### [MODIFY] [TransactionsScreen.kt](file:///home/patrick/Documents/PROJECTS/finovate-kwacha-wise/android/app/src/main/java/com/example/kwachawise/ui/screens/TransactionsScreen.kt)
- Connect to `ViewModel` to display real sorted transactions.

## Verification Plan

### Automated Tests
- **SmsParserTest**: Unit tests for regex parsing logic with various SMS samples.
- **TransactionDaoTest**: Instrument tests for Room database operations.

### Manual Verification
- Deploy to device/emulator.
- Send a mock SMS to the device and verify it appears in "Review Pending".
- Tag the transaction and verify it moves to the "Transactions" list.
- Check that persistence works after app restart.
