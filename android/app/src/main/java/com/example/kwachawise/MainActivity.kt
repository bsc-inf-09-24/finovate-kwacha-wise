package com.example.kwachawise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kwachawise.data.AppDatabase
import com.example.kwachawise.data.AppTheme
import com.example.kwachawise.data.ThemePreferences
import com.example.kwachawise.data.TransactionRepository
import com.example.kwachawise.models.Transaction
import com.example.kwachawise.navigation.Screen
import com.example.kwachawise.ui.screens.*
import com.example.kwachawise.ui.theme.KwachaWiseTheme
import com.example.kwachawise.ui.viewmodel.TransactionViewModel
import com.example.kwachawise.ui.viewmodel.TransactionViewModelFactory
import com.example.kwachawise.utils.SmsParser
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var showRationale by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handle result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkSmsPermission()
        
        val themePreferences = ThemePreferences(this)
        
        setContent {
            val appTheme by themePreferences.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
            
            KwachaWiseTheme(appTheme = appTheme) {
                if (showRationale) {
                    PermissionRationaleDialog(
                        onConfirm = {
                            showRationale = false
                            requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                        },
                        onDismiss = { showRationale = false }
                    )
                }
                KwachaWiseApp(themePreferences = themePreferences)
            }
        }
    }

    private fun checkSmsPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED -> {
                // Already have permission
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS) -> {
                showRationale = true
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            }
        }
    }
}

@Composable
fun PermissionRationaleDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Automate Your Records") },
        text = { Text("KwachaWise can automatically detect Airtel Money and Mpamba transactions from your SMS alerts. This saves you from manual bookkeeping. We only read transaction alerts.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Allow") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("No thanks") }
        }
    )
}

@Composable
fun KwachaWiseApp(themePreferences: ThemePreferences) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = TransactionRepository(
        database.transactionDao(),
        database.aiAnalysisDao(),
        database.documentDao()
    )
    val viewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )
    val appTheme by themePreferences.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
    val coroutineScope = rememberCoroutineScope()
    
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
            val balance by viewModel.balance.collectAsState()
            val pendingCount by viewModel.pendingCount.collectAsState()
            val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
            
            HomeScreen(
                balance = balance,
                pendingCount = pendingCount,
                unreadNotificationsCount = unreadNotificationsCount,
                appTheme = appTheme,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onThemeToggle = {
                    val newTheme = if (appTheme == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK
                    coroutineScope.launch {
                        themePreferences.saveTheme(newTheme)
                    }
                }
            )
        }
        composable(Screen.ReviewPending.route) {
            val pendingTransactions by viewModel.pendingTransactions.collectAsState()
            ReviewPendingScreen(
                pendingTransactions = pendingTransactions,
                onTagTransaction = { id, type, tag, note -> 
                    viewModel.finalizeTransaction(id, type, tag, note)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Transactions.route) {
            val groupedTransactions by viewModel.filteredTransactions.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val selectedTag by viewModel.selectedTag.collectAsState()
            
            TransactionsScreen(
                groupedTransactions = groupedTransactions,
                searchQuery = searchQuery,
                selectedFilter = selectedTag,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onFilterSelected = { viewModel.setSelectedTag(it) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddCashEntry.route) {
            AddCashEntryScreen(
                onSave = { amount, desc, tag, type -> 
                    val transaction = Transaction(
                        id = java.util.UUID.randomUUID().toString(),
                        amount = amount,
                        description = desc,
                        type = type,
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
            val insights by viewModel.aiInsights.collectAsState()
            val history by viewModel.aiAnalysisHistory.collectAsState()
            val isNewDataAvailable by viewModel.isNewDataAvailable.collectAsState()
            InsightsScreen(
                healthSignal = insights,
                history = history,
                isNewDataAvailable = isNewDataAvailable,
                onViewInsights = { viewModel.fetchAiInsights() },
                onViewDocuments = { navController.navigate(Screen.Documents.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Documents.route) {
            val documents by viewModel.allDocuments.collectAsState()
            val isGenerating by viewModel.isGeneratingDocument.collectAsState()
            DocumentListScreen(
                documents = documents,
                isGenerating = isGenerating,
                onGenerate = { start, end, title -> viewModel.generateDocument(start, end, title) },
                onDocumentClick = { id -> navController.navigate(Screen.DocumentDetail.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DocumentDetail.route) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId")
            val documents by viewModel.allDocuments.collectAsState()
            val document = documents.find { it.id == documentId }
            DocumentDetailScreen(
                document = document,
                onBack = { navController.popBackStack() }
            )
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
        composable(Screen.Search.route) {
            val query by viewModel.searchQuery.collectAsState()
            val results by viewModel.searchResults.collectAsState()
            SearchScreen(
                query = query,
                searchResults = results,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Notifications.route) {
            val notifications by viewModel.notifications.collectAsState()
            NotificationsScreen(
                notifications = notifications,
                onNotificationClick = { viewModel.markNotificationAsRead(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
