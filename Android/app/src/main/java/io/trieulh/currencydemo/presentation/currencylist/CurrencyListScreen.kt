package io.trieulh.currencydemo.presentation.currencylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyType
import io.trieulh.currencydemo.presentation.currencylist.components.CurrencyListItem
import io.trieulh.currencydemo.presentation.navigation.Screen

@Composable
fun CurrencyListScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CurrencyListViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
            IconButton(onClick = { navController.navigate(Screen.SearchScreen.route) }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.currencies.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(id = R.string.no_results_message),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.no_results_message),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Box {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.currencies, key = { it.id }) { currency ->
                            CurrencyListItem(currency = currency)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { viewModel.onEvent(CurrencyListContract.Event.OnClearDataClick) }) {
                Text(stringResource(id = R.string.button_clear))
            }
            Button(onClick = { viewModel.onEvent(CurrencyListContract.Event.OnInsertDataClick) }) {
                Text(stringResource(id = R.string.button_insert))
            }
            Button(
                onClick = {
                    viewModel.onEvent(
                        CurrencyListContract.Event.OnLoadCurrencies(
                            CurrencyType.Crypto, forceFetch = true
                        )
                    )
                }) {
                Text(stringResource(id = R.string.button_crypto))
            }
            Button(
                onClick = {
                    viewModel.onEvent(
                        CurrencyListContract.Event.OnLoadCurrencies(
                            CurrencyType.Fiat, forceFetch = true
                        )
                    )
                }) {
                Text(stringResource(id = R.string.button_fiat))
            }
            Button(
                onClick = {
                    viewModel.onEvent(
                        CurrencyListContract.Event.OnLoadCurrencies(
                            CurrencyType.All, forceFetch = false
                        )
                    )
                }) {
                Text(stringResource(id = R.string.button_all))
            }
        }
    }
}
