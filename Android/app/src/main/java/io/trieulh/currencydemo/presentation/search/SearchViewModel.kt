package io.trieulh.currencydemo.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.usecase.SearchCurrenciesUseCase
import io.trieulh.currencydemo.domain.util.DispatcherProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCurrenciesUseCase: SearchCurrenciesUseCase,
    private val dispatchers: DispatcherProvider
) : ViewModel(), SearchContract {

    private val _uiState = MutableStateFlow(SearchContract.State())
    override val uiState: StateFlow<SearchContract.State> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SearchContract.Effect>()
    override val effect = _effect.asSharedFlow()

    private val _searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch(dispatchers.IO) {
            _searchQueryFlow
                .debounce(1.seconds)
                .distinctUntilChanged()
                .collect { query ->
                    searchCurrencies(query)
                }
        }
    }

    override fun onEvent(event: SearchContract.Event) {
        when (event) {
            is SearchContract.Event.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                _searchQueryFlow.value = event.query
            }

            SearchContract.Event.OnToggleSearch -> {
                _uiState.update { it.copy(isSearchActive = !it.isSearchActive) }
                if (!uiState.value.isSearchActive) {
                    _uiState.update { it.copy(searchQuery = "", searchResults = null) }
                    _searchQueryFlow.value = ""
                }
            }

            SearchContract.Event.OnClearSearch -> {
                _uiState.update { it.copy(searchQuery = "", searchResults = null) }
                _searchQueryFlow.value = ""
            }

            SearchContract.Event.OnBackClick -> {
                viewModelScope.launch {
                    _effect.emit(SearchContract.Effect.NavigateBack)
                }
            }
        }
    }

    private fun searchCurrencies(keyword: String) {
        val trimmedKeyword = keyword.trim()
        if (trimmedKeyword.isBlank()) {
            _uiState.update { it.copy(searchResults = null) }
            return
        }

        viewModelScope.launch(dispatchers.IO) {
            searchCurrenciesUseCase(trimmedKeyword).collect {
                when (it) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                searchResults = it.data
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                searchResults = null
                            )
                        }
                        _effect.emit(SearchContract.Effect.ShowError(it.message ?: "Unknown error"))
                    }

                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, searchResults = null) }
                    }
                }
            }
        }
    }
}
