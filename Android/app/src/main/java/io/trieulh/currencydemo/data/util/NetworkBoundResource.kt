
package io.trieulh.currencydemo.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

abstract class NetworkBoundResource<ResultType, RequestType> {

    fun asFlow(): Flow<Resource<ResultType>> = flow {
        emit(Resource.Loading())
        val dbData = loadFromDb().first()

        if (shouldFetch(dbData)) {
            emit(Resource.Loading(dbData))

            try {
                val apiResponse = fetchFromNetwork()
                saveNetworkResult(apiResponse)
                emitAll(loadFromDb().map { Resource.Success(it) })
            } catch (throwable: Throwable) {
                onFetchFailed(throwable)
                emitAll(loadFromDb().map { Resource.Error(throwable.message ?: "Unknown error", it) })
            }
        } else {
            emitAll(loadFromDb().map { Resource.Success(it) })
        }
    }

    protected open fun onFetchFailed(throwable: Throwable) {}

    protected abstract fun loadFromDb(): Flow<ResultType>

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract suspend fun fetchFromNetwork(): RequestType

    protected abstract suspend fun saveNetworkResult(item: RequestType)
}
