
package io.trieulh.currencydemo.data.remote

import io.trieulh.currencydemo.data.remote.dto.CurrencyInfoDto
import retrofit2.http.GET

interface CurrencyApi {

    @GET("currencies")
    suspend fun getCurrencies(): List<CurrencyInfoDto>
}
