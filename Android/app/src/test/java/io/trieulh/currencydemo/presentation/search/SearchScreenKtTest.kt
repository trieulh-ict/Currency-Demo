package io.trieulh.currencydemo.presentation.search

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.presentation.search.SearchContract.Event
import io.trieulh.currencydemo.presentation.search.SearchContract.State
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class SearchScreenKtTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun placeholderShownWhenQueryEmptyAndNoResults() {
        val state = State(searchQuery = "", isLoading = false, searchResults = emptyList())

        composeRule.setContent {
            SearchContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithText(context.getString(R.string.search_currencies_placeholder))
            .assertIsDisplayed()
        val messageNodes =
            composeRule.onAllNodesWithText(context.getString(R.string.message_no_results))
                .fetchSemanticsNodes()
        assertEquals(0, messageNodes.size)
    }

    @Test
    fun loadingIndicatorVisibleWhileFetching() {
        val state = State(searchQuery = "btc", isLoading = true, searchResults = null)

        composeRule.setContent {
            SearchContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
        val infoNodes =
            composeRule.onAllNodesWithText(context.getString(R.string.message_no_results))
                .fetchSemanticsNodes()
        assertEquals(0, infoNodes.size)
    }

    @Test
    fun rendersResultListWhenAvailable() {
        val results = listOf(
            CurrencyInfo("BTC", "Bitcoin", symbol = "BTC"),
            CurrencyInfo("ETH", "Ethereum", symbol = "ETH")
        )
        val state = State(searchQuery = "b", isLoading = false, searchResults = results)

        composeRule.setContent {
            SearchContent(state = state, onEvent = {})
        }

        results.forEach { composeRule.onNodeWithText(it.name).assertIsDisplayed() }
        val suggestionNodes =
            composeRule.onAllNodesWithText(context.getString(R.string.try_btc_message))
                .fetchSemanticsNodes()
        assertEquals(0, suggestionNodes.size)
    }

    @Test
    fun emptyResultsShowGuidanceMessage() {
        val state = State(searchQuery = "zzz", isLoading = false, searchResults = emptyList())

        composeRule.setContent {
            SearchContent(state = state, onEvent = {})
        }

        composeRule.onNodeWithText(context.getString(R.string.message_no_results))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.try_btc_message))
            .assertIsDisplayed()
    }

    @Test
    fun textInputEmitsOnSearchQueryChange() {
        val events = mutableListOf<Event>()
        val stateHolder = mutableStateOf(
            State(searchQuery = "", isLoading = false, searchResults = emptyList())
        )

        composeRule.setContent {
            SearchContent(
                state = stateHolder.value,
                onEvent = { event ->
                    events += event
                    if (event is Event.OnSearchQueryChange) {
                        stateHolder.value = stateHolder.value.copy(searchQuery = event.query)
                    }
                }
            )
        }

        composeRule.onNodeWithTag("search_query").performTextInput("bitcoin")

        assertTrue(events.any { it is Event.OnSearchQueryChange && it.query == "bitcoin" })
    }

    @Test
    fun clearAndBackButtonsEmitEvents() {
        val events = mutableListOf<Event>()
        val stateHolder = mutableStateOf(
            State(searchQuery = "btc", isLoading = false, searchResults = emptyList())
        )

        composeRule.setContent {
            SearchContent(
                state = stateHolder.value,
                onEvent = { event ->
                    events += event
                    if (event is Event.OnClearSearch) {
                        stateHolder.value = stateHolder.value.copy(searchQuery = "")
                    }
                }
            )
        }

        composeRule.onNodeWithContentDescription(
            context.getString(R.string.clear_search_content_description)
        ).performClick()
        composeRule.onNodeWithContentDescription(
            context.getString(R.string.back_content_description)
        ).performClick()

        assertEquals(
            listOf(Event.OnClearSearch, Event.OnBackClick),
            events
        )
    }
}
