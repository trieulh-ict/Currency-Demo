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
import io.trieulh.currencydemo.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val api: CurrencyApi,
    private val dao: CurrencyInfoDao
) : CurrencyRepository {

    override fun getCurrencies(): Flow<Resource<List<CurrencyInfo>>> {
        return object : NetworkBoundResource<List<CurrencyInfo>, List<CurrencyInfoDto>>() {
            override fun loadFromDb(): Flow<List<CurrencyInfo>> {
                return dao.getCurrencies().map { entities ->
                    entities.map { it.toDomain() }
                }
            }

            override fun shouldFetch(data: List<CurrencyInfo>?): Boolean {
                return data.isNullOrEmpty()
            }

            override suspend fun fetchFromNetwork(): List<CurrencyInfoDto> {
                return api.getCurrencies()
            }

            override suspend fun saveNetworkResult(item: List<CurrencyInfoDto>) {
                dao.clearCurrencies()
                dao.insertCurrencies(item.map { it.toDomain().toEntity() })
            }
        }.asFlow()
    }
}