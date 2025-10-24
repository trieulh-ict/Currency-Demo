package io.trieulh.currencydemo.domain.usecase

import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCurrenciesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    operator fun invoke(keyword: String): Flow<Resource<List<CurrencyInfo>>> {
        return repository.searchCurrencies(keyword.trim())
    }
}
