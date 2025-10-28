package io.trieulh.currencydemo.presentation.currencylist.components

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.trieulh.currencydemo.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class CurrencyActionButtonKtTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun labelFromResourcesIsRendered() {
        composeRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_insert, onClick = {})
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.button_insert)).assertIsDisplayed()
    }

    @Test
    fun clickCallbackIsInvoked() {
        var clickCount = 0

        composeRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_clear) {
                    clickCount++
                }
            }
        }

        val node = composeRule.onNodeWithText(context.getString(R.string.button_clear))
        repeat(2) { node.performClick() }

        assertEquals(2, clickCount)
    }

    @Test
    fun multipleButtonsCanCoexistInRowScope() {
        composeRule.setContent {
            Row {
                CurrencyActionButton(labelResId = R.string.button_all, onClick = {})
                CurrencyActionButton(labelResId = R.string.button_crypto, onClick = {})
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.button_all)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.button_crypto)).assertIsDisplayed()
    }
}
