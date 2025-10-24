package io.trieulh.currencydemo.presentation.search

import io.trieulh.currencydemo.domain.model.CurrencyInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SearchContract {
    val uiState: StateFlow<State>
    val effect: Flow<Effect>
    fun onEvent(event: Event)

    data class State(
        val searchQuery: String = "",
        val searchResults: List<CurrencyInfo>? = null,
        val isLoading: Boolean = false,
        val isSearchActive: Boolean = false
    )

    sealed class Event {
        data class OnSearchQueryChange(val query: String) : Event()
        object OnToggleSearch : Event()
        object OnClearSearch : Event()
        object OnBackClick : Event()
    }

    sealed class Effect {
        object NavigateBack : Effect()
        data class ShowError(val message: String) : Effect()
    }
}
