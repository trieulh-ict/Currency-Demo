package io.trieulh.currencydemo.presentation.currencylist.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.trieulh.currencydemo.domain.model.CurrencyInfo

@Composable
fun CurrencyListItem(currency: CurrencyInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = currency.name)
        Spacer(modifier = Modifier.weight(1f))
        if (currency.code == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = currency.symbol)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "Right Arrow"
                )
            }
        }
    }
}