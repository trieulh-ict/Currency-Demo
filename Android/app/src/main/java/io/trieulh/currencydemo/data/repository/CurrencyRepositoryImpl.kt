package io.trieulh.currencydemo.data.repository

import io.trieulh.currencydemo.data.local.CurrencyInfoDao
import io.trieulh.currencydemo.data.local.entity.toDomain
import io.trieulh.currencydemo.data.local.entity.toEntity
import io.trieulh.currencydemo.data.remote.CurrencyApi
import io.trieulh.currencydemo.data.remote.dto.CurrencyInfoDto
import io.trieulh.currencydemo.data.remote.dto.toDomain
import io.trieulh.currencydemo.data.util.NetworkBoundResource
import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import io.trieulh.currencydemo.domain.repository.CurrencyRepository
import io.trieulh.currencydemo.domain.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val api: CurrencyApi,
    private val dao: CurrencyInfoDao,
    private val dispatcherProvider: DispatcherProvider
) : CurrencyRepository {

    override fun getCurrencies(
        type: CurrencyType, forceFetch: Boolean
    ): Flow<Resource<List<CurrencyInfo>>> {
        return object : NetworkBoundResource<List<CurrencyInfo>, List<CurrencyInfoDto>>() {
            override fun loadFromDb(): Flow<List<CurrencyInfo>> {
                return when (type) {
                    CurrencyType.Fiat -> dao.getFiatCurrencies()
                    CurrencyType.Crypto -> dao.getCryptoCurrencies()
                    CurrencyType.All -> dao.getCurrencies()
                }.map { entities ->
                    entities.map { it.toDomain() }.sortedWith(compareBy { it.code != null })
                }
            }

            override fun shouldFetch(data: List<CurrencyInfo>?): Boolean {
                return forceFetch || data.isNullOrEmpty()
            }

            override suspend fun fetchFromNetwork(): List<CurrencyInfoDto> {
                return withContext(dispatcherProvider.IO) {
                    api.getCurrencies(type.value)
                }
            }

            override suspend fun saveNetworkResult(item: List<CurrencyInfoDto>) {
                dao.insertCurrencies(item.map { it.toDomain().toEntity() })
            }
        }.asFlow()
    }

    override suspend fun clearCurrencies() {
        dao.clearCurrencies()
    }

    override suspend fun insertCurrencies(currencies: List<CurrencyInfo>) {
        dao.insertCurrencies(currencies.map { it.toEntity() })
    }
}
