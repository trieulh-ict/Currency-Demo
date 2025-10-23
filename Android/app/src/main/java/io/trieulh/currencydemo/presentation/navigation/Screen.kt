package io.trieulh.currencydemo.presentation.navigation

sealed class Screen(val route: String) {
    object CurrencyListScreen : Screen("currency_list_screen")
    object SearchScreen : Screen("search_screen")
}
