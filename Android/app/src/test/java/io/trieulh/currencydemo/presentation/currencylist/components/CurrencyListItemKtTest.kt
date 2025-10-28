package io.trieulh.currencydemo.presentation.currencylist.components

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class CurrencyListItemKtTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun currencyNameAndAvatarInitialAreShown() {
        val currency = CurrencyInfo(id = "btc", name = "Bitcoin", code = null, symbol = "BTC")

        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }

        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("B").assertIsDisplayed()
    }

    @Test
    fun symbolAndArrowShowWhenCodeIsNull() {
        val currency = CurrencyInfo(id = "btc", name = "Bitcoin", code = null, symbol = "BTC")

        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }

        composeRule.onNodeWithText("BTC").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            context.getString(R.string.right_arrow_content_description)
        ).assertIsDisplayed()
    }

    @Test
    fun symbolAndArrowHiddenWhenCodePresent() {
        val currency = CurrencyInfo(id = "usd", name = "US Dollar", code = "USD", symbol = "$")

        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }

        composeRule.onNodeWithText("$").assertDoesNotExist()
        composeRule.onNodeWithContentDescription(
            context.getString(R.string.right_arrow_content_description)
        ).assertDoesNotExist()
    }

    @Test
    fun highlightAppliedToMatchingQuerySegment() {
        val currency = CurrencyInfo(id = "eth", name = "Ethereum", code = null, symbol = "ETH")

        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "eth")
        }

        val semantics = composeRule.onNodeWithText("Ethereum")
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            ?.first()

        val highlightedRange = semantics?.spanStyles?.firstOrNull {
            it.item.fontWeight == FontWeight.Bold
        }

        assertNotNull(highlightedRange)
        assertEquals(0, highlightedRange!!.start)
        assertEquals(3, highlightedRange.end)
    }

    @Test
    fun searchQueryWithoutMatchLeavesTextUnstyled() {
        val currency = CurrencyInfo(id = "ada", name = "Cardano", code = null, symbol = "ADA")

        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "zzz")
        }

        val semantics = composeRule.onNodeWithText("Cardano")
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            ?.first()

        val highlightedRange = semantics?.spanStyles?.firstOrNull {
            it.item.fontWeight == FontWeight.Bold
        }

        assertNull(highlightedRange)
    }
}
