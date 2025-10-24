package io.trieulh.currencydemo.presentation.search

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.presentation.currencylist.components.CurrencyListItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SearchContract.Effect.NavigateBack -> {
                    navController.popBackStack()
                }

                is SearchContract.Effect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SearchContent(
        state = state,
        onEvent = { event -> viewModel.onEvent(event) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    state: SearchContract.State,
    onEvent: (SearchContract.Event) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0.dp), title = {
                TextField(
                    value = state.searchQuery,
                    onValueChange = {
                        onEvent(SearchContract.Event.OnSearchQueryChange(it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(id = R.string.search_currencies_placeholder)) },
                    singleLine = true,
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onEvent(SearchContract.Event.OnClearSearch) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.clear_search_content_description)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
            }, navigationIcon = {
                IconButton(onClick = { onEvent(SearchContract.Event.OnBackClick) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_content_description)
                    )
                }
            })
        }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.loading_message))
                }
            } else if (state.searchResults?.isEmpty() == true && state.searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            text = stringResource(id = R.string.message_no_results),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(id = R.string.try_btc_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(state.searchResults ?: emptyList(), key = { it.id }) {
                        CurrencyListItem(currency = it, searchQuery = state.searchQuery)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    val sampleState = SearchContract.State(
        searchQuery = "bitcoin", isLoading = false, searchResults = listOf(
            // Assuming a sample currency item, replace with actual sample data if available
            CurrencyInfo(
                id = "btc",
                name = "Bitcoin",
                symbol = "BTC",
                // other properties with sample values as needed
            ), CurrencyInfo(
                id = "eth",
                name = "Ethereum",
                symbol = "ETH",
            )
        )
    )
    SearchContent(
        state = sampleState,
        onEvent = {},
    )
}