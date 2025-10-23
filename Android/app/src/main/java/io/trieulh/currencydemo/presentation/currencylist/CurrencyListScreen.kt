package io.trieulh.currencydemo.presentation.currencylist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.trieulh.currencydemo.presentation.currencylist.components.CurrencyListItem

@Composable
fun CurrencyListScreen(
    modifier: Modifier = Modifier, viewModel: CurrencyListViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.currencies) { currency ->
                    CurrencyListItem(currency = currency)
                }
            }
        }
    }
}
