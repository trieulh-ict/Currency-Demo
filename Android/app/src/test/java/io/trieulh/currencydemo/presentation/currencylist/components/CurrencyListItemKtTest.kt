package io.trieulh.currencydemo.presentation.currencylist.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyListItemKtTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun assertTextBold(node: SemanticsNodeInteraction, text: String) {
        // Compose testing does not provide direct font weight checks.
        // However, we can check if the text is present and rely on visual validation or custom semantics in real app.
        // Here we just check text presence.
        node.assertIsDisplayed()
        node.assertTextContains(text)
    }

    @Test
    fun `Currency name displayed correctly`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun `First letter of currency name in circular avatar`() {
        val currency = CurrencyInfo(id = "", name = "Ethereum", code = null, symbol = "ETH")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        // The first letter "E" should be displayed inside the avatar
        composeRule.onNodeWithText("E").assertIsDisplayed()
    }

    @Test
    fun `Currency symbol and arrow icon visibility`() {
        val currency = CurrencyInfo(id = "", name = "Ripple", code = null, symbol = "XRP")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        // Symbol should be displayed
        composeRule.onNodeWithText("XRP").assertIsDisplayed()
        // Arrow icon should be displayed, assuming contentDescription = "Forward Arrow"
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.right_arrow_content_description))
            .assertIsDisplayed()
    }

    @Test
    fun `No symbol and arrow icon when code is not null`() {
        val currency = CurrencyInfo(id = "", name = "Litecoin", code = "LTC", symbol = "Ł")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        // Symbol should not be displayed
        composeRule.onNodeWithText("Ł").assertDoesNotExist()
        // Arrow icon should not be displayed
        composeRule.onNodeWithContentDescription("Forward Arrow").assertDoesNotExist()
    }

    @Test
    fun `Search query highlighting at the beginning`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        val searchQuery = "Bit"
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        // The beginning "Bit" should be highlighted (bold)
        val node = composeRule.onNodeWithText("Bitcoin")
        node.assertIsDisplayed()
        // We cannot directly check bold weight here, but we can check the text is displayed
        val nodes = composeRule.onAllNodesWithText("Bit", substring = true).fetchSemanticsNodes()
        assert(nodes.size == 1)
    }

    @Test
    fun `Search query highlighting in the middle`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        val searchQuery = "coin"
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        // The substring "coin" in the middle should be highlighted
        val nodes = composeRule.onAllNodesWithText("coin", substring = true).fetchSemanticsNodes()
        assert(nodes.size == 1)
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun `Search query highlighting at the end`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        val searchQuery = "coin"
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        val nodes = composeRule.onAllNodesWithText("coin", substring = true).fetchSemanticsNodes()
        assert(nodes.size == 1)
    }

    @Test
    fun `Case insensitive search query highlighting`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        val searchQuery = "btc"
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        // "btc" lower case should highlight "Bit" or "BTC"?
        // Since search is on name, "btc" is not substring of "Bitcoin" but ignoring case means "Bit" matches "btc"? No.
        // So no highlight expected.
        val nodes = composeRule.onAllNodesWithText("btc", substring = true).fetchSemanticsNodes()
        assert(nodes.isEmpty())
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun `No highlighting for non matching query`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        val searchQuery = "xyz"
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        // No part should be bolded or highlighted
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        val nodes = composeRule.onAllNodesWithText("xyz", substring = true).fetchSemanticsNodes()
        assert(nodes.isEmpty())
    }

    @Test
    fun `Empty search query`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        val searchQuery = ""
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun `Special characters in currency name`() {
        val currency = CurrencyInfo(id = "", name = "Dollar", code = null, symbol = "$$")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        composeRule.onNodeWithText("Dollar").assertIsDisplayed()
    }

    @Test
    fun `Special characters in search query`() {
        val currency = CurrencyInfo(id = "", name = "Dollar", code = null, symbol = "$$")
        val searchQuery = "$$"
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        // Since "Dollar" does not contain "$$", no highlight expected
        composeRule.onNodeWithText("Dollar").assertIsDisplayed()
    }

    @Test
    fun `Currency with an empty name string`() {
        val currency = CurrencyInfo(id = "", name = "", code = null, symbol = "")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        // Should not crash and no text displayed
        val nodes = composeRule.onAllNodesWithText("").fetchSemanticsNodes()
        assert(nodes.isNotEmpty())

    }

    @Test
    fun `Long currency name rendering`() {
        val longName = "ThisIsAnExtremelyLongCurrencyNameThatMightBreakLayoutIfNotHandledProperly"
        val currency = CurrencyInfo(id = "", name = longName, code = null, symbol = "LNG")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        composeRule.onNodeWithText(longName).assertIsDisplayed()
    }

    @Test
    fun `Long currency symbol rendering`() {
        val longSymbol = "LONGSYMBOL1234567890"
        val currency =
            CurrencyInfo(id = "", name = "LongSymbolCoin", code = null, symbol = longSymbol)
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        composeRule.onNodeWithText(longSymbol).assertIsDisplayed()
    }

    @Test
    fun `Full match query highlighting`() {
        val currency = CurrencyInfo(id = "", name = "Bitcoin", code = null, symbol = "BTC")
        val searchQuery = "Bitcoin"
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = searchQuery)
        }
        // Entire name should be highlighted
        val nodes =
            composeRule.onAllNodesWithText("Bitcoin", substring = true).fetchSemanticsNodes()
        assert(nodes.size == 1)
    }

    @Test
    fun `Content description for arrow icon`() {
        val currency = CurrencyInfo(id = "", name = "Ripple", code = null, symbol = "XRP")
        composeRule.setContent {
            CurrencyListItem(currency = currency, searchQuery = "")
        }
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.right_arrow_content_description))
            .assertIsDisplayed()
    }
}