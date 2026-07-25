package com.example.kwachawise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kwachawise.data.AppDatabase
import com.example.kwachawise.data.TransactionRepository
import com.example.kwachawise.models.Transaction
import com.example.kwachawise.models.TransactionTag
import com.example.kwachawise.navigation.Screen
import com.example.kwachawise.ui.screens.*
import com.example.kwachawise.ui.theme.KwachaWiseTheme
import com.example.kwachawise.ui.viewmodel.TransactionViewModel
import com.example.kwachawise.ui.viewmodel.TransactionViewModelFactory
import com.example.kwachawise.utils.SmsParser

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkSmsPermission()
        
        setContent {
            KwachaWiseTheme {
                KwachaWiseApp()
            }
        }
    }

    private fun checkSmsPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
        }
    }
}

@Composable
fun KwachaWiseApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = TransactionRepository(database.transactionDao())
    val viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )
    
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(onStartClicked = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(onNavigate = { route ->
                navController.navigate(route)
            })
        }
        composable(Screen.ReviewPending.route) {
            val pendingTransactions by viewModel.pendingTransactions.collectAsState()
            ReviewPendingScreen(
                pendingTransactions = pendingTransactions,
                onTagTransaction = { id, tag, note -> 
                    viewModel.updateTransactionTag(id, tag, note)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Transactions.route) {
            val transactions by viewModel.sortedTransactions.collectAsState()
            TransactionsScreen(
                transactions = transactions,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddCashEntry.route) {
            AddCashEntryScreen(
                onSave = { amount, desc, tag -> 
                    val transaction = Transaction(
                        id = java.util.UUID.randomUUID().toString(),
                        amount = amount,
                        description = desc,
                        tag = tag,
                        date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.US).format(java.util.Date())
                    )
                    viewModel.addManualTransaction(transaction)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Insights.route) {
            InsightsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PasteSms.route) {
            PasteSmsScreen(
                onParse = { text ->
                    val transaction = SmsParser.parse(text)
                    if (transaction != null) {
                        viewModel.addManualTransaction(transaction)
                        navController.navigate(Screen.ReviewPending.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
