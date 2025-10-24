package io.trieulh.currencydemo.presentation.currencylist.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.trieulh.currencydemo.R

@Composable
fun RowScope.CurrencyActionButton(labelResId: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = stringResource(id = labelResId),
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyActionButtonPreview() {
    MaterialTheme {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            CurrencyActionButton(labelResId = R.string.button_clear, onClick = {})
            CurrencyActionButton(labelResId = R.string.button_insert, onClick = {})
            CurrencyActionButton(labelResId = R.string.button_crypto, onClick = {})
            CurrencyActionButton(labelResId = R.string.button_fiat, onClick = {})
            CurrencyActionButton(labelResId = R.string.button_all, onClick = {})
        }
    }
}