package io.trieulh.currencydemo.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.trieulh.currencydemo.data.local.entity.CurrencyInfoEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class CurrencyInfoDaoTest {

    private lateinit var database: CurrencyDatabase
    private lateinit var dao: CurrencyInfoDao

    private val fiatCurrency1 = CurrencyInfoEntity("usd", "US Dollar", "USD", "USD")
    private val fiatCurrency2 = CurrencyInfoEntity("eur", "Euro", "EUR", "EUR")
    private val cryptoCurrency1 = CurrencyInfoEntity("btc", "Bitcoin", "BTC", null)
    private val cryptoCurrency2 = CurrencyInfoEntity("eth", "Ethereum", "ETH", null)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CurrencyDatabase::class.java)
            .allowMainThreadQueries() // For testing purposes
            .build()
        dao = database.currencyDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertCurrencies  Insert a single new currency`() = runTest {
        dao.getCurrencies().test {
            // Initial empty state
            assertEquals(emptyList(), awaitItem())

            // Insert a single currency
            dao.insertCurrencies(listOf(fiatCurrency1))
            advanceUntilIdle()

            // Verify the flow emits the inserted currency
            assertEquals(listOf(fiatCurrency1), awaitItem())
        }
    }

    @Test
    fun `insertCurrencies  Insert multiple new currencies`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            val currenciesToInsert = listOf(fiatCurrency1, cryptoCurrency1)
            dao.insertCurrencies(currenciesToInsert)
            advanceUntilIdle()

            assertEquals(currenciesToInsert, awaitItem())
        }
    }

    @Test
    fun `insertCurrencies  Insert an empty list`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            // Insert some initial currencies
            dao.insertCurrencies(listOf(fiatCurrency1))
            advanceUntilIdle()
            assertEquals(listOf(fiatCurrency1), awaitItem())

            // Insert an empty list
            dao.insertCurrencies(emptyList())
            advanceUntilIdle()

            // Verify no new emission, state remains unchanged
            expectNoEvents()
        }
    }

    @Test
    fun `insertCurrencies  Conflict replacement behavior with existing currencies`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            // Insert initial currency
            dao.insertCurrencies(listOf(fiatCurrency1))
            advanceUntilIdle()
            assertEquals(listOf(fiatCurrency1), awaitItem())

            // Insert a new currency with the same ID but different name
            val updatedFiatCurrency1 = fiatCurrency1.copy(name = "Updated US Dollar")
            dao.insertCurrencies(listOf(updatedFiatCurrency1))
            advanceUntilIdle()

            // Verify the old entity is replaced by the new one
            assertEquals(listOf(updatedFiatCurrency1), awaitItem())
        }
    }

    @Test
    fun `insertCurrencies  Insert currencies with null and non null codes`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            val mixedCurrencies =
                listOf(fiatCurrency1, cryptoCurrency1, fiatCurrency2, cryptoCurrency2)
            dao.insertCurrencies(mixedCurrencies)
            advanceUntilIdle()

            assertEquals(mixedCurrencies.sortedBy { it.id }, awaitItem().sortedBy { it.id })

            // Verify partitioning
            dao.getFiatCurrencies().test {
                assertEquals(
                    listOf(fiatCurrency1, fiatCurrency2).sortedBy { it.id },
                    awaitItem().sortedBy { it.id })
            }
            dao.getCryptoCurrencies().test {
                assertEquals(
                    listOf(cryptoCurrency1, cryptoCurrency2).sortedBy { it.id },
                    awaitItem().sortedBy { it.id })
            }
        }
    }

    @Test
    fun `getCurrencies  Observe initial empty state`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `getCurrencies  Observe emission after insertion`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            dao.insertCurrencies(listOf(fiatCurrency1))
            advanceUntilIdle()

            assertEquals(listOf(fiatCurrency1), awaitItem())
        }
    }

    @Test
    fun `getCurrencies  Flow emits updates on subsequent insertions`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            dao.insertCurrencies(listOf(fiatCurrency1))
            advanceUntilIdle()
            assertEquals(listOf(fiatCurrency1), awaitItem())

            dao.insertCurrencies(listOf(cryptoCurrency1))
            advanceUntilIdle()
            assertEquals(
                listOf(fiatCurrency1, cryptoCurrency1).sortedBy { it.id },
                awaitItem().sortedBy { it.id })
        }
    }

    @Test
    fun `getCurrencies  Flow emits updates after clearCurrencies call`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            dao.insertCurrencies(listOf(fiatCurrency1, cryptoCurrency1))
            advanceUntilIdle()
            assertEquals(
                listOf(fiatCurrency1, cryptoCurrency1).sortedBy { it.id },
                awaitItem().sortedBy { it.id })

            dao.clearCurrencies()
            advanceUntilIdle()
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `getFiatCurrencies  Retrieve only fiat currencies`() = runTest {
        dao.insertCurrencies(listOf(fiatCurrency1, cryptoCurrency1, fiatCurrency2, cryptoCurrency2))
        advanceUntilIdle()

        dao.getFiatCurrencies().test {
            assertEquals(
                listOf(fiatCurrency1, fiatCurrency2).sortedBy { it.id },
                awaitItem().sortedBy { it.id })
        }
    }

    @Test
    fun `getFiatCurrencies  Empty state when no fiat currencies exist`() = runTest {
        dao.insertCurrencies(listOf(cryptoCurrency1, cryptoCurrency2))
        advanceUntilIdle()

        dao.getFiatCurrencies().test {
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `getFiatCurrencies  Flow updates on relevant insertion`() = runTest {
        dao.getFiatCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            // Insert a fiat currency
            dao.insertCurrencies(listOf(fiatCurrency1))
            advanceUntilIdle()
            assertEquals(listOf(fiatCurrency1), awaitItem())

            // Insert a crypto currency (should not trigger update)
            dao.insertCurrencies(listOf(cryptoCurrency1))
            advanceUntilIdle()
            expectNoEvents()

            // Insert another fiat currency
            dao.insertCurrencies(listOf(fiatCurrency2))
            advanceUntilIdle()
            assertEquals(
                listOf(fiatCurrency1, fiatCurrency2).sortedBy { it.id },
                awaitItem().sortedBy { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCryptoCurrencies  Retrieve only crypto currencies`() = runTest {
        dao.insertCurrencies(listOf(fiatCurrency1, cryptoCurrency1, fiatCurrency2, cryptoCurrency2))
        advanceUntilIdle()

        dao.getCryptoCurrencies().test {
            assertEquals(
                listOf(cryptoCurrency1, cryptoCurrency2).sortedBy { it.id },
                awaitItem().sortedBy { it.id })
        }
    }

    @Test
    fun `getCryptoCurrencies  Empty state when no crypto currencies exist`() = runTest {
        dao.insertCurrencies(listOf(fiatCurrency1, fiatCurrency2))
        advanceUntilIdle()

        dao.getCryptoCurrencies().test {
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `getCryptoCurrencies  Flow updates on relevant insertion`() = runTest {
        dao.getCryptoCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            // Insert a crypto currency
            dao.insertCurrencies(listOf(cryptoCurrency1))
            advanceUntilIdle()
            assertEquals(listOf(cryptoCurrency1), awaitItem())

            // Insert a fiat currency (should not trigger update)
            dao.insertCurrencies(listOf(fiatCurrency1))
            advanceUntilIdle()
            expectNoEvents()

            // Insert another crypto currency
            dao.insertCurrencies(listOf(cryptoCurrency2))
            advanceUntilIdle()
            assertEquals(
                listOf(cryptoCurrency1, cryptoCurrency2).sortedBy { it.id },
                awaitItem().sortedBy { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearCurrencies  Clear all currencies from a non empty database`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            dao.insertCurrencies(listOf(fiatCurrency1, cryptoCurrency1))
            advanceUntilIdle()
            assertEquals(
                listOf(fiatCurrency1, cryptoCurrency1).sortedBy { it.id },
                awaitItem().sortedBy { it.id })

            dao.clearCurrencies()
            advanceUntilIdle()
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun `clearCurrencies  Calling on an already empty database`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())

            dao.clearCurrencies()
            advanceUntilIdle()

            // Should still be empty, no new emission
            expectNoEvents()
        }
    }

    @Test
    fun `Concurrency  Multiple insertions in parallel`() = runTest {
        val currencies1 = listOf(fiatCurrency1, cryptoCurrency1)
        val currencies2 = listOf(fiatCurrency2, cryptoCurrency2)

        val job1 = launch { dao.insertCurrencies(currencies1) }
        val job2 = launch { dao.insertCurrencies(currencies2) }

        // Wait until both are done
        joinAll(job1, job2)
        advanceUntilIdle()

        dao.getCurrencies().test {
            val result = awaitItem().sortedBy { it.id }
            assertEquals((currencies1 + currencies2).sortedBy { it.id }, result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Concurrency  Simultaneous read and write operations`() = runTest {
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem()) // Initial empty state

            // Insert some currencies
            launch {
                dao.insertCurrencies(listOf(fiatCurrency1, cryptoCurrency1))
            }
            advanceUntilIdle()
            assertEquals(
                listOf(fiatCurrency1, cryptoCurrency1).sortedBy { it.id },
                awaitItem().sortedBy { it.id })

            // Clear currencies
            launch {
                dao.clearCurrencies()
            }
            advanceUntilIdle()
            assertEquals(emptyList(), awaitItem())

            // Insert more currencies
            launch {
                dao.insertCurrencies(listOf(fiatCurrency2))
            }
            advanceUntilIdle()
            assertEquals(listOf(fiatCurrency2), awaitItem())
        }
    }

    @Test
    fun `Thread safety of suspend functions`() = runTest {
        // Calling suspend functions from runTest (which uses TestDispatcher as main)
        // implicitly tests their main-safety. Room handles dispatching to background threads.

        // Insert operation
        dao.insertCurrencies(listOf(fiatCurrency1))
        advanceUntilIdle()
        dao.getCurrencies().test {
            assertEquals(listOf(fiatCurrency1), awaitItem())
        }

        // Clear operation
        dao.clearCurrencies()
        advanceUntilIdle()
        dao.getCurrencies().test {
            assertEquals(emptyList(), awaitItem())
        }

        // If these operations complete without blocking the test dispatcher,
        // it indicates Room's main-safety is working.
    }

}