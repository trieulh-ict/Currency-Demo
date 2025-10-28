package io.trieulh.currencydemo.presentation.currencylist

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class CurrencyListScreenKtTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun rendersEmptyStateWhenNoCurrencies() {
        val state = CurrencyListContract.State(currencies = emptyList(), isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithText(context.getString(R.string.currency_list_title))
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.search_title))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("infoIcon").assertExists()
        composeRule.onNodeWithText(context.getString(R.string.message_no_results_full))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.button_clear)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.button_all)).assertIsDisplayed()
    }

    @Test
    fun showsLoadingIndicatorWhileFetching() {
        val state = CurrencyListContract.State(currencies = emptyList(), isLoading = true)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNode(hasTestTag("loadingIndicator")).assertIsDisplayed()
    }

    @Test
    fun displaysCurrencyRowsWhenDataAvailable() {
        val state = CurrencyListContract.State(
            currencies = listOf(
                CurrencyInfo("btc", "Bitcoin", "BTC"),
                CurrencyInfo("eth", "Ethereum", "ETH")
            ),
            isLoading = false
        )

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("Ethereum").assertIsDisplayed()
    }

    @Test
    fun buttonInteractionsEmitExpectedEvents() {
        val events = mutableListOf<CurrencyListContract.Event>()
        val state = CurrencyListContract.State(isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = { events += it })
        }

        composeRule.onNodeWithContentDescription(context.getString(R.string.search_title))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.button_clear)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.button_insert)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.button_crypto)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.button_fiat)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.button_all)).performClick()

        val expected = listOf(
            CurrencyListContract.Event.OnSearchClick,
            CurrencyListContract.Event.OnClearDataClick,
            CurrencyListContract.Event.OnInsertDataClick,
            CurrencyListContract.Event.OnLoadCurrencies(CurrencyType.Crypto, true),
            CurrencyListContract.Event.OnLoadCurrencies(CurrencyType.Fiat, true),
            CurrencyListContract.Event.OnLoadCurrencies(CurrencyType.All, false)
        )
        assertEquals(expected, events)
    }

    @Test
    fun uiUpdatesWhenStateChanges() {
        val state = mutableStateOf(
            CurrencyListContract.State(isLoading = true, currencies = emptyList())
        )

        composeRule.setContent {
            CurrencyListContent(state = state.value, onEvent = {})
        }
        composeRule.onNode(hasTestTag("loadingIndicator")).assertIsDisplayed()

        state.value = CurrencyListContract.State(
            isLoading = false,
            currencies = listOf(CurrencyInfo("btc", "Bitcoin", "BTC"))
        )
        composeRule.mainClock.advanceTimeByFrame()

        val infoNodes = composeRule.onAllNodesWithTag("infoIcon").fetchSemanticsNodes()
        assertEquals(0, infoNodes.size)
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }
}
