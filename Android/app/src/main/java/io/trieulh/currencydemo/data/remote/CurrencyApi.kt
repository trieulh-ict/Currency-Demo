package io.trieulh.currencydemo.data.remote

import io.trieulh.currencydemo.data.remote.dto.CurrencyInfoDto
import io.trieulh.currencydemo.data.remote.dto.SearchCurrencyRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CurrencyApi {

    @GET("currencies")
    suspend fun getCurrencies(@Query("type") type: String? = null): List<CurrencyInfoDto>

    @POST("currencies/search")
    suspend fun searchCurrencies(@Body request: SearchCurrencyRequestDto): List<CurrencyInfoDto>
}