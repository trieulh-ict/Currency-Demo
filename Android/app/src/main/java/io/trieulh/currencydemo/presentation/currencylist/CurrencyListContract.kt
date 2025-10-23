package io.trieulh.currencydemo.presentation.currencylist

import io.trieulh.currencydemo.domain.model.CurrencyInfo

import io.trieulh.currencydemo.domain.model.CurrencyType

interface CurrencyListContract {

    data class State(
        val currencies: List<CurrencyInfo> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class Event {
        object OnCreate : Event()
        object OnClearDataClick : Event()
        object OnInsertDataClick : Event()
        data class OnLoadCurrencies(
            val type: CurrencyType = CurrencyType.All,
            val forceFetch: Boolean = false
        ) : Event()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
    }
}