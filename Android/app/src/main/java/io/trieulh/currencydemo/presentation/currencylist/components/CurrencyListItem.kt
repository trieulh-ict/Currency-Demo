package io.trieulh.currencydemo.presentation.currencylist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo

@Composable
fun CurrencyListItem(currency: CurrencyInfo, searchQuery: String = "") {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currency.name.firstOrNull()?.toString().orEmpty(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Text(buildAnnotatedString {
                val startIndex = currency.name.indexOf(searchQuery, ignoreCase = true)
                if (searchQuery.isNotEmpty() && startIndex != -1) {
                    append(currency.name.substring(0, startIndex))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(currency.name.substring(startIndex, startIndex + searchQuery.length))
                    }
                    append(currency.name.substring(startIndex + searchQuery.length))
                } else {
                    append(currency.name)
                }
            })

            Spacer(modifier = Modifier.weight(1f))

            if (currency.code == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = currency.symbol)
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = stringResource(id = R.string.right_arrow_content_description),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyListItemPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(8.dp)) {
            CurrencyListItem(
                currency = CurrencyInfo(
                    id = "1", name = "Bitcoin", symbol = "BTC", code = null
                ), searchQuery = ""
            )

            CurrencyListItem(
                currency = CurrencyInfo(
                    id = "2", name = "Ethereum", symbol = "ETH", code = null
                ), searchQuery = "e"
            )
        }
    }
}