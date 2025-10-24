package io.trieulh.currencydemo.presentation.currencylist

import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import io.trieulh.currencydemo.navigation.Screen
import io.trieulh.currencydemo.presentation.currencylist.components.CurrencyActionButton
import io.trieulh.currencydemo.presentation.currencylist.components.CurrencyListItem
import io.trieulh.currencydemo.ui.theme.CurrencyDemoTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CurrencyListScreen(
    navController: NavController, viewModel: CurrencyListViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CurrencyListContract.Effect.NavigateToSearch -> {
                    navController.navigate(Screen.SearchScreen.route)
                }

                is CurrencyListContract.Effect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    CurrencyListContent(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun CurrencyListContent(
    state: CurrencyListContract.State,
    onEvent: (CurrencyListContract.Event) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.currency_list_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
            IconButton(onClick = { onEvent(CurrencyListContract.Event.OnSearchClick) }) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.search_title)
                )
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
                        contentDescription = stringResource(id = R.string.message_no_results_full),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.message_no_results_full),
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
            CurrencyActionButton(
                labelResId = R.string.button_clear,
                onClick = { onEvent(CurrencyListContract.Event.OnClearDataClick) })
            CurrencyActionButton(
                labelResId = R.string.button_insert,
                onClick = { onEvent(CurrencyListContract.Event.OnInsertDataClick) })
            CurrencyActionButton(
                labelResId = R.string.button_crypto, onClick = {
                    onEvent(
                        CurrencyListContract.Event.OnLoadCurrencies(
                            CurrencyType.Crypto, forceFetch = true
                        )
                    )
                })
            CurrencyActionButton(
                labelResId = R.string.button_fiat, onClick = {
                    onEvent(
                        CurrencyListContract.Event.OnLoadCurrencies(
                            CurrencyType.Fiat, forceFetch = true
                        )
                    )
                })
            CurrencyActionButton(
                labelResId = R.string.button_all, onClick = {
                    onEvent(
                        CurrencyListContract.Event.OnLoadCurrencies(
                            CurrencyType.All, forceFetch = false
                        )
                    )
                })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyListPreview() {
    val sampleState = CurrencyListContract.State(
        isLoading = false, currencies = listOf(
            CurrencyInfo(id = "1", name = "Bitcoin", symbol = "BTC"),
            CurrencyInfo(id = "2", name = "US Dollar", symbol = "USD")
        )
    )
    CurrencyDemoTheme {
        CurrencyListContent(state = sampleState)
    }
}
