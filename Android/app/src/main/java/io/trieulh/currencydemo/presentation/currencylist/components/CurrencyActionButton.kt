package io.trieulh.currencydemo.presentation.currencylist.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

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