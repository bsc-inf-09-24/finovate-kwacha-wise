package com.example.kwachawise.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object ReviewPending : Screen("review_pending")
    object Transactions : Screen("transactions")
    object AddCashEntry : Screen("add_cash_entry")
    object Insights : Screen("insights")
    object PasteSms : Screen("paste_sms")
    object Search : Screen("search")
    object Notifications : Screen("notifications")
}
