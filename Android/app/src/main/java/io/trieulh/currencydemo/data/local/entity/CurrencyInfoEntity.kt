
package io.trieulh.currencydemo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.trieulh.currencydemo.domain.model.CurrencyInfo

@Entity(tableName = "currencies")
data class CurrencyInfoEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val symbol: String,
    val code: String?
)

fun CurrencyInfoEntity.toDomain() = CurrencyInfo(
    id = id,
    name = name,
    symbol = symbol,
    code = code
)

fun CurrencyInfo.toEntity() = CurrencyInfoEntity(
    id = id,
    name = name,
    symbol = symbol,
    code = code
)
