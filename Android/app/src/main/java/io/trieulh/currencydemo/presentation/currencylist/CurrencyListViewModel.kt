package io.trieulh.currencydemo.presentation.currencylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.model.CurrencyType
import io.trieulh.currencydemo.domain.usecase.GetAllCurrenciesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase
) : ViewModel(), CurrencyListContract {

    private val _state = MutableStateFlow(CurrencyListContract.State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CurrencyListContract.Effect>()
    val effect = _effect.asSharedFlow()

    private var getCurrenciesJob: Job? = null

    init {
        onEvent(CurrencyListContract.Event.OnCreate)
    }

    fun onEvent(event: CurrencyListContract.Event) {
        when (event) {
            CurrencyListContract.Event.OnCreate -> {
                getAllCurrencies(forceFetch = true)
            }

            CurrencyListContract.Event.OnClearDataClick -> {
                viewModelScope.launch {
                    getAllCurrenciesUseCase.clear()
                }
            }

            CurrencyListContract.Event.OnInsertDataClick -> {
                getAllCurrencies(forceFetch = true)
            }

            is CurrencyListContract.Event.OnLoadCurrencies -> {
                getAllCurrencies(event.type, event.forceFetch)
            }
        }
    }

    private fun getAllCurrencies(
        type: CurrencyType = CurrencyType.All,
        forceFetch: Boolean = false
    ) {
        getCurrenciesJob?.cancel()
        getCurrenciesJob = viewModelScope.launch {
            getAllCurrenciesUseCase(type, forceFetch).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                currencies = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _effect.emit(
                            CurrencyListContract.Effect.ShowError(
                                result.message ?: "Unknown error"
                            )
                        )
                    }

                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}
