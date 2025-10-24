package io.trieulh.currencydemo.presentation.currencylist

import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CurrencyListContract {
    val uiState: StateFlow<State>
    val effect: Flow<Effect>
    fun onEvent(event: Event)

    data class State(
        val currencies: List<CurrencyInfo> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class Event {
        object OnCreate : Event()
        object OnClearDataClick : Event()
        object OnInsertDataClick : Event()
        object OnSearchClick : Event()
        data class OnLoadCurrencies(
            val type: CurrencyType = CurrencyType.All,
            val forceFetch: Boolean = false
        ) : Event()
    }

    sealed class Effect {
        object NavigateToSearch : Effect()
        data class ShowError(val message: String) : Effect()
    }
}