package io.trieulh.currencydemo.presentation.currencylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.usecase.GetAllCurrenciesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase
) : ViewModel(), CurrencyListContract {

    private val _state = MutableStateFlow(CurrencyListContract.State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CurrencyListContract.Effect>()
    val effect = _effect.asSharedFlow()

    init {
        onEvent(CurrencyListContract.Event.OnCreate)
    }

    fun onEvent(event: CurrencyListContract.Event) {
        when (event) {
            CurrencyListContract.Event.OnCreate -> {
                getAllCurrenciesUseCase().onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.update { it.copy(
                                currencies = result.data ?: emptyList(),
                                isLoading = false
                            ) }
                        }
                        is Resource.Error -> {
                            _state.update { it.copy(isLoading = false) }
                            _effect.emit(CurrencyListContract.Effect.ShowError(result.message ?: "Unknown error"))
                        }
                        is Resource.Loading -> {
                            _state.update { it.copy(isLoading = true) }
                        }
                    }
                }.launchIn(viewModelScope)
            }
        }
    }
}