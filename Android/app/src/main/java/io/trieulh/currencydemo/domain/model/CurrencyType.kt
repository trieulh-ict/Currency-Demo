package io.trieulh.currencydemo.domain.model

sealed class CurrencyType(val value: String?) {
    object All : CurrencyType(null)
    object Crypto : CurrencyType("crypto")
    object Fiat : CurrencyType("fiat")
}
