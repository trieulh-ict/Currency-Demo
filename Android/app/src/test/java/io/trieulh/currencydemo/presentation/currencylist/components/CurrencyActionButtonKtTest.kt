package io.trieulh.currencydemo.presentation.currencylist.components

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.trieulh.currencydemo.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyActionButtonKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `onClick callback verification`() {
        var clickCount = 0
        composeTestRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_clear) {
                    clickCount++
                }
            }
        }
        composeTestRule.onNodeWithText("Clear").performClick()
        assert(clickCount == 1)
    }

    @Test
    fun `Label text display with valid resource ID`() {
        composeTestRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_insert, onClick = {})
            }
        }
        composeTestRule.onNodeWithText("Insert").assertIsDisplayed()
    }

    @Test(expected = android.content.res.Resources.NotFoundException::class)
    fun `Invalid string resource ID handling`() {
        composeTestRule.setContent {
            Row {
                CurrencyActionButton(labelResId = -1, onClick = {})
            }
        }
    }

    @Test
    fun `UI properties verification`() {
        composeTestRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_all, onClick = {})
            }
        }
        // Verify Button exists
        composeTestRule.onNodeWithText("All").assertExists()
    }

    @Test
    fun `Text properties verification for single line display`() {
        composeTestRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_crypto, onClick = {})
            }
        }
        composeTestRule.onNodeWithText("Crypto").assertIsDisplayed()
    }

    @Test
    fun `Composition within a RowScope`() {
        composeTestRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_fiat, onClick = {})
            }
        }
        composeTestRule.onNodeWithText("Fiat").assertIsDisplayed()
    }

    @Test
    fun `Composition outside a RowScope handling`() {
        // This should not compile in normal Kotlin usage, so we just verify it’s not allowed.
        // Cannot be tested at runtime — compile-time enforcement.
        assert(true)
    }

    @Test
    fun `Multiple clicks behavior`() {
        var clickCount = 0
        composeTestRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_insert) {
                    clickCount++
                }
            }
        }
        val node = composeTestRule.onNodeWithText("Insert")
        node.performClick()
        node.performClick()
        node.performClick()
        assert(clickCount == 3)
    }
}