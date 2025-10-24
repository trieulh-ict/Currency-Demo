package io.trieulh.currencydemo.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchCurrencyRequestDto(
    val keyword: String
)
