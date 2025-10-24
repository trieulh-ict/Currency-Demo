package io.trieulh.currencydemo.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CurrencyInfo(
    val id: String, val name: String, val symbol: String, val code: String? = null
)
