package io.trieulh.currencydemo.data.remote

import io.trieulh.currencydemo.data.remote.dto.CurrencyInfoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {

    @GET("currencies")
    suspend fun getCurrencies(@Query("type") type: String? = null): List<CurrencyInfoDto>
}