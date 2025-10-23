package io.trieulh.currencydemo.presentation.currencylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.trieulh.currencydemo.domain.model.CurrencyType
import io.trieulh.currencydemo.presentation.currencylist.components.CurrencyListItem

@Composable
fun CurrencyListScreen(
    modifier: Modifier = Modifier,
    viewModel: CurrencyListViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { viewModel.onEvent(CurrencyListContract.Event.OnClearDataClick) }) {
                Text("Clear")
            }
            Button(onClick = { viewModel.onEvent(CurrencyListContract.Event.OnInsertDataClick) }) {
                Text("Insert")
            }
            Button(onClick = {
                viewModel.onEvent(
                    CurrencyListContract.Event.OnLoadCurrencies(
                        CurrencyType.Crypto,
                        forceFetch = true
                    )
                )
            }) {
                Text("Crypto")
            }
            Button(onClick = {
                viewModel.onEvent(
                    CurrencyListContract.Event.OnLoadCurrencies(
                        CurrencyType.Fiat,
                        forceFetch = true
                    )
                )
            }) {
                Text("Fiat")
            }
            Button(onClick = {
                viewModel.onEvent(
                    CurrencyListContract.Event.OnLoadCurrencies(
                        CurrencyType.All,
                        forceFetch = false
                    )
                )
            }) {
                Text("All")
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
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
}
