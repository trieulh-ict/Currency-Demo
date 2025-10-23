package io.trieulh.currencydemo.domain.usecase

import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.repository.CurrencyRepository
import io.trieulh.currencydemo.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCurrenciesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {

    operator fun invoke(): Flow<Resource<List<CurrencyInfo>>> {
        return repository.getCurrencies()
    }
}
