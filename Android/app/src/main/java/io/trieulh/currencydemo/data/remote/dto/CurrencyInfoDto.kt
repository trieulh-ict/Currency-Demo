
package io.trieulh.currencydemo.data.remote.dto

import io.trieulh.currencydemo.domain.model.CurrencyInfo
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyInfoDto(
    val id: String,
    val name: String,
    val symbol: String,
    val code: String? = null
)

fun CurrencyInfoDto.toDomain() = CurrencyInfo(
    id = id,
    name = name,
    symbol = symbol,
    code = code
)
