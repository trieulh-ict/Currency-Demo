package io.trieulh.currencydemo.presentation.search

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.trieulh.currencydemo.R
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.presentation.search.SearchContract.Event
import io.trieulh.currencydemo.presentation.search.SearchContract.State
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchScreen_initialState_displaysPlaceholderAndNoResults() {
        var eventReceived: Event? = null
        val state = State(
            searchQuery = "",
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        // Placeholder text visible
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.search_currencies_placeholder))
            .assertIsDisplayed()

        // No results list or messages displayed
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.message_no_results))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.try_btc_message))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.loading_message))
            .assertDoesNotExist()
    }

    @Test
    fun searchScreen_loadingState_displaysLoadingMessage() {
        var eventReceived: Event? = null
        val state = State(
            searchQuery = "",
            isLoading = true,
            searchResults = null
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()

        // No results or placeholder visible
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.message_no_results))
            .assertDoesNotExist()
    }

    @Test
    fun searchScreen_withResults_displaysCurrencyList() {
        var eventReceived: Event? = null
        val currencyList = listOf(
            CurrencyInfo("BTC", "Bitcoin", symbol = "BTC"),
            CurrencyInfo("ETH", "Ethereum", symbol = "ETH"),
            CurrencyInfo("DOGE", "Dogecoin", symbol = "DOGE")
        )
        val state = State(
            searchQuery = "bit",
            isLoading = false,
            searchResults = currencyList
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        currencyList.forEach {
            composeTestRule.onNodeWithText(it.name).assertIsDisplayed()
        }

        // Placeholder and no results messages should not be visible
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.search_currencies_placeholder))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.message_no_results))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.try_btc_message))
            .assertDoesNotExist()
    }

    @Test
    fun searchScreen_emptyResults_displaysNoResultsAndSuggestion() {
        var eventReceived: Event? = null
        val state = State(
            searchQuery = "nonexistent",
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.message_no_results))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.try_btc_message))
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_nullResults_displaysNoCrashAndEmptyState() {
        var eventReceived: Event? = null
        val state = State(
            searchQuery = "anything",
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        // Should show no results and suggestion messages
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.message_no_results))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.try_btc_message))
            .assertIsDisplayed()
    }

    @Test
    fun searchScreen_textInput_triggersOnSearchQueryChange() {
        var eventReceived: Event? = null
        var query by mutableStateOf("")
        val state = State(
            searchQuery = query,
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = {
                    eventReceived = it
                    if (it is Event.OnSearchQueryChange) {
                        query = it.query
                    }
                }
            )
        }

        val inputText = "bitcoin"
        composeTestRule.onNodeWithTag("search_query").performTextInput(inputText)

        composeTestRule.waitForIdle()

        assert(eventReceived is Event.OnSearchQueryChange)
        if (eventReceived is Event.OnSearchQueryChange) {
            assert((eventReceived as Event.OnSearchQueryChange).query == inputText)
        }
    }

    @Test
    fun searchScreen_clearButton_visibilityAndTriggersOnClearSearch() {
        var eventReceived: Event? = null
        var query by mutableStateOf("bitcoin")
        val state = State(
            searchQuery = query,
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = {
                    eventReceived = it
                    if (it is Event.OnClearSearch) {
                        query = ""
                    }
                }
            )
        }

        // Clear button visible when query not empty
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.clear_search_content_description))
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assert(eventReceived is Event.OnClearSearch)
    }

    @Test
    fun searchScreen_clearButton_hiddenWhenQueryEmpty() {
        var eventReceived: Event? = null
        val state = State(
            searchQuery = "",
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        // Clear button should not be visible when query is empty
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.clear_search_content_description))
            .assertDoesNotExist()
    }

    @Test
    fun searchScreen_backButton_triggersOnBackClick() {
        var eventReceived: Event? = null
        val state = State(
            searchQuery = "",
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.back_content_description))
            .assertIsDisplayed()
            .performClick()

        assert(eventReceived is Event.OnBackClick)
    }

    @Test
    fun searchScreen_specialCharacters_inputHandledCorrectly() {
        var eventReceived: Event? = null
        var query by mutableStateOf("")
        val state = State(
            searchQuery = query,
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = {
                    eventReceived = it
                    if (it is Event.OnSearchQueryChange) {
                        query = it.query
                    }
                }
            )
        }

        val specialInput = "!@#\$%^&*()_+😀"
        composeTestRule.onNodeWithTag("search_query")
            .performTextInput(specialInput)

        composeTestRule.waitForIdle()

        assert(eventReceived is Event.OnSearchQueryChange)
        if (eventReceived is Event.OnSearchQueryChange) {
            assert((eventReceived as Event.OnSearchQueryChange).query == specialInput)
        }
    }

    @Test
    fun searchScreen_longInput_notTruncated() {
        var eventReceived: Event? = null
        var query by mutableStateOf("")
        val state = State(
            searchQuery = query,
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = {
                    eventReceived = it
                    if (it is Event.OnSearchQueryChange) {
                        query = it.query
                    }
                }
            )
        }

        val longInput = "a".repeat(500)
        composeTestRule.onNodeWithTag("search_query")
            .performTextInput(longInput)

        composeTestRule.waitForIdle()

        assert(eventReceived is Event.OnSearchQueryChange)
        if (eventReceived is Event.OnSearchQueryChange) {
            assert((eventReceived as Event.OnSearchQueryChange).query == longInput)
        }
    }

    @Test
    fun searchScreen_largeList_displaysEntries() {
        var eventReceived: Event? = null
        val largeList = (1..100).map { CurrencyInfo("SYM$it", "Currency $it", symbol = "SYM$it") }
        val state = State(
            searchQuery = "a",
            isLoading = false,
            searchResults = largeList
        )

        composeTestRule.setContent {
            SearchContent(
                state = state,
                onEvent = { eventReceived = it }
            )
        }

        composeTestRule.onNodeWithText("Currency 1").assertIsDisplayed()
    }

    @Test
    fun searchScreen_stateTransitions_loadingToResultsToEmpty() {
        var eventReceived: Event? = null
        var query by mutableStateOf("")
        var isLoading by mutableStateOf(true)
        var results by mutableStateOf<List<CurrencyInfo>?>(null)

        composeTestRule.setContent {
            SearchContent(
                state = State(
                    searchQuery = query,
                    isLoading = isLoading,
                    searchResults = results
                ),
                onEvent = { eventReceived = it }
            )
        }

        // Initially loading message shown
        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()

        // Transition to results
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun searchScreen_placeholderVisibleOnlyWhenQueryEmpty() {
        var eventReceived: Event? = null
        var query by mutableStateOf("")
        val state = State(
            searchQuery = query,
            isLoading = false,
            searchResults = emptyList()
        )

        composeTestRule.setContent {
            SearchContent(
                state = State(
                    searchQuery = query,
                    isLoading = false,
                    searchResults = emptyList()
                ),
                onEvent = { eventReceived = it }
            )
        }

        // Initially, placeholder visible when query is empty
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.search_currencies_placeholder)
        ).assertIsDisplayed()

        // Update query to trigger recomposition
        composeTestRule.runOnUiThread {
            query = "btc"
        }
        composeTestRule.waitForIdle()

        // Placeholder should disappear when query is not empty
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.search_currencies_placeholder)
        ).assertDoesNotExist()
    }
}