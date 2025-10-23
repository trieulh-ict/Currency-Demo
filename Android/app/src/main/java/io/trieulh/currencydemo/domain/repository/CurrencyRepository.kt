package io.trieulh.currencydemo.domain.repository

import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.data.util.Resource
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    fun getCurrencies(): Flow<Resource<List<CurrencyInfo>>>
}
