package io.trieulh.currencydemo.presentation.currencylist

import io.trieulh.currencydemo.domain.model.CurrencyInfo

interface CurrencyListContract {

    data class State(
        val currencies: List<CurrencyInfo> = emptyList(), val isLoading: Boolean = false
    )

    sealed class Event {
        object OnCreate : Event()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
    }
}
