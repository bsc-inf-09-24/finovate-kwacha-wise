package com.example.kwachawise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kwachawise.navigation.Screen
import com.example.kwachawise.ui.screens.*
import com.example.kwachawise.ui.theme.KwachaWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KwachaWiseTheme {
                KwachaWiseApp()
            }
        }
    }
}

@Composable
fun KwachaWiseApp() {
    val navController = rememberNavController()
    
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
            ReviewPendingScreen(
                onTagTransaction = { id, tag, note -> 
                    // To be implemented with real logic later
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Transactions.route) {
            TransactionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AddCashEntry.route) {
            AddCashEntryScreen(
                onSave = { amount, desc, tag -> 
                    // To be implemented with real logic later
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
                    // To be implemented with real logic later
                    navController.navigate(Screen.ReviewPending.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
