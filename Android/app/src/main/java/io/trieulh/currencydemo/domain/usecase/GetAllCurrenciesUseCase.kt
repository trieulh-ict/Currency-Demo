package io.trieulh.currencydemo.domain.usecase

import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import io.trieulh.currencydemo.domain.repository.CurrencyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetAllCurrenciesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        type: CurrencyType = CurrencyType.All, forceFetch: Boolean = false
    ): Flow<Resource<List<CurrencyInfo>>> {
        return flowOf(Pair(type, forceFetch))
            .flatMapLatest { (t, f) ->
                repository.getCurrencies(
                    t,
                    f
                )
            }
    }

    suspend fun clear() {
        repository.clearCurrencies()
    }

    suspend fun insert(currencies: List<CurrencyInfo>) {
        repository.insertCurrencies(currencies)
    }
}
