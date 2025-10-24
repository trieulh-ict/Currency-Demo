package io.trieulh.currencydemo.presentation.currencylist

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CurrencyListScreenKtTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun currencyListScreen_initial_state_check() {
        val state = CurrencyListContract.State(
            currencies = emptyList(),
            isLoading = false
        )

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        // Title and search icon
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.currency_list_title))
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Search").assertIsDisplayed()

        // Bottom action buttons
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_clear))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_insert))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_crypto))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_fiat))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_all))
            .assertIsDisplayed()

        // Info icon and empty message
        composeRule.onNodeWithTag("infoIcon").assertExists()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.message_no_results_full)
        ).assertIsDisplayed()
    }

    @Test
    fun currencyListContent_displays_loading_indicator() {
        val state = CurrencyListContract.State(
            currencies = emptyList(),
            isLoading = true
        )

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNode(hasTestTag("loadingIndicator")).assertIsDisplayed()
    }

    @Test
    fun currencyListContent_hides_loading_indicator() {
        val state = CurrencyListContract.State(
            currencies = emptyList(),
            isLoading = false
        )

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNode(hasTestTag("loadingIndicator")).assertDoesNotExist()
    }

    @Test
    fun currencyListContent_displays_empty_state_message_and_info_icon() {
        val state = CurrencyListContract.State(
            currencies = emptyList(),
            isLoading = false
        )

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.message_no_results_full))
            .assertIsDisplayed()

        composeRule.onNodeWithTag("infoIcon").assertExists()
    }

    @Test
    fun currencyListContent_displays_list_of_currencies() {
        val fakeList = listOf(
            CurrencyInfo(id = "btc", name = "Bitcoin", symbol = "BTC"),
            CurrencyInfo(id = "eth", name = "Ethereum", symbol = "ETH")
        )

        val state = CurrencyListContract.State(
            currencies = fakeList,
            isLoading = false
        )

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("Ethereum").assertIsDisplayed()
    }

    @Test
    fun currencyListContent_search_button_click_event() {
        var lastEvent: CurrencyListContract.Event? = null
        val state = CurrencyListContract.State(isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = { event -> lastEvent = event })
        }

        composeRule.onNodeWithContentDescription("Search").performClick()
        assert(lastEvent is CurrencyListContract.Event.OnSearchClick)
    }

    @Test
    fun currencyListContent_clear_data_button_click_event() {
        var lastEvent: CurrencyListContract.Event? = null
        val state = CurrencyListContract.State(isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = { event -> lastEvent = event })
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_clear))
            .performClick()
        assert(lastEvent is CurrencyListContract.Event.OnClearDataClick)
    }

    @Test
    fun currencyListContent_insert_data_button_click_event() {
        var lastEvent: CurrencyListContract.Event? = null
        val state = CurrencyListContract.State(isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = { event -> lastEvent = event })
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_insert))
            .performClick()
        assert(lastEvent is CurrencyListContract.Event.OnInsertDataClick)
    }

    @Test
    fun currencyListContent_load_crypto_button_click_event() {
        var lastEvent: CurrencyListContract.Event? = null
        val state = CurrencyListContract.State(isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = { event -> lastEvent = event })
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_crypto))
            .performClick()
        assert(
            lastEvent ==
                    CurrencyListContract.Event.OnLoadCurrencies(
                        CurrencyType.Crypto,
                        forceFetch = true
                    )
        )
    }

    @Test
    fun currencyListContent_load_fiat_button_click_event() {
        var lastEvent: CurrencyListContract.Event? = null
        val state = CurrencyListContract.State(isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = { event -> lastEvent = event })
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_fiat))
            .performClick()
        assert(
            lastEvent ==
                    CurrencyListContract.Event.OnLoadCurrencies(
                        CurrencyType.Fiat,
                        forceFetch = true
                    )
        )
    }

    @Test
    fun currencyListContent_load_all_button_click_event() {
        var lastEvent: CurrencyListContract.Event? = null
        val state = CurrencyListContract.State(isLoading = false)

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = { event -> lastEvent = event })
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_all))
            .performClick()
        assert(
            lastEvent ==
                    CurrencyListContract.Event.OnLoadCurrencies(
                        CurrencyType.All,
                        forceFetch = false
                    )
        )
    }

    @Test
    fun currencyListContent_state_transition_from_loading_to_empty() {
        val initial = CurrencyListContract.State(isLoading = true)
        val final = CurrencyListContract.State(isLoading = false, currencies = emptyList())
        val state = mutableStateOf(initial)

        composeRule.setContent {
            CurrencyListContent(state = state.value, onEvent = {})
        }

        composeRule.onNode(hasTestTag("loadingIndicator")).assertIsDisplayed()

        composeRule.runOnUiThread {
            state.value = final
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.message_no_results_full))
            .assertIsDisplayed()

        composeRule.onNodeWithTag("infoIcon").assertExists()
    }

    @Test
    fun currencyListContent_state_transition_from_loading_to_data() {
        val initial = CurrencyListContract.State(isLoading = true)
        val final = CurrencyListContract.State(
            isLoading = false,
            currencies = listOf(CurrencyInfo("btc", "Bitcoin", "BTC"))
        )
        val state = mutableStateOf(initial)

        composeRule.setContent {
            CurrencyListContent(state = state.value, onEvent = {})
        }

        composeRule.onNode(hasTestTag("loadingIndicator")).assertIsDisplayed()

        composeRule.runOnUiThread {
            state.value = final
        }

        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
    }

    @Test
    fun currencyListContent_no_op_event_handler() {
        val state = CurrencyListContract.State(
            isLoading = false,
            currencies = listOf(CurrencyInfo("eth", "Ethereum", "ETH"))
        )

        composeRule.setContent {
            CurrencyListContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithText("Ethereum").performClick()
    }
}