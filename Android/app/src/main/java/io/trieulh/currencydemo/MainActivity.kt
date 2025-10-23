package io.trieulh.currencydemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.trieulh.currencydemo.presentation.currencylist.CurrencyListScreen
import io.trieulh.currencydemo.presentation.navigation.Screen
import io.trieulh.currencydemo.presentation.search.SearchScreen
import io.trieulh.currencydemo.ui.theme.CurrencyDemoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyDemoTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.CurrencyListScreen.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.CurrencyListScreen.route) {
                            CurrencyListScreen(navController = navController)
                        }
                        composable(Screen.SearchScreen.route) {
                            SearchScreen()
                        }
                    }
                }
            }
        }
    }
}
