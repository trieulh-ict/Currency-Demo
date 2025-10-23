package io.trieulh.currencydemo.domain.repository

import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    fun getCurrencies(
        type: CurrencyType = CurrencyType.All,
        forceFetch: Boolean = false
    ): Flow<Resource<List<CurrencyInfo>>>

    suspend fun clearCurrencies()

    suspend fun insertCurrencies(currencies: List<CurrencyInfo>)
}
