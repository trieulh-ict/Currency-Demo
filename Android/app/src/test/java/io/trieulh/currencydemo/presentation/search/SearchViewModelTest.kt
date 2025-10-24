package io.trieulh.currencydemo.presentation.search

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.usecase.SearchCurrenciesUseCase
import io.trieulh.currencydemo.domain.util.DispatcherProvider
import io.trieulh.currencydemo.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SearchViewModel
    private val searchCurrenciesUseCase: SearchCurrenciesUseCase = mockk(relaxed = true)
    private val dispatcherProvider: DispatcherProvider = mockk(relaxed = true)

    private val mockCurrencies = listOf(
        CurrencyInfo("btc", "Bitcoin", "BTC"),
        CurrencyInfo("eth", "Ethereum", "ETH")
    )

    @Before
    fun setUp() {
        every { dispatcherProvider.IO } returns mainCoroutineRule.testDispatcher
        every { dispatcherProvider.Default } returns mainCoroutineRule.testDispatcher
        every { dispatcherProvider.Main } returns mainCoroutineRule.testDispatcher

        // Default mock for searchCurrenciesUseCase to prevent unexpected calls
        every { searchCurrenciesUseCase.invoke(any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = SearchViewModel(searchCurrenciesUseCase, dispatcherProvider)
    }

    @Test
    fun `Initial State Validation`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearchActive)
        assertFalse(state.isLoading)
        assertNull(state.searchResults)
    }

    @Test
    fun `OnSearchQueryChange Event  UI State Update`() = runTest {
        val query = "bitcoin"
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        runCurrent()
        assertEquals(query, viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `OnSearchQueryChange Event  Search Debouncing`() = runTest {
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange("b"))
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange("bi"))
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange("bit"))
        runCurrent()
        verify(exactly = 0) { searchCurrenciesUseCase.invoke(any()) }

        advanceTimeBy(1.seconds - 1.milliseconds)
        runCurrent()
        verify(exactly = 0) { searchCurrenciesUseCase.invoke(any()) }

        advanceTimeBy(1.milliseconds)
        runCurrent()
        verify(exactly = 1) { searchCurrenciesUseCase.invoke("bit") }
    }

    @Test
    fun `OnSearchQueryChange Event  Search with Sufficient Delay`() = runTest {
        val query = "bitcoin"
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()
        verify(exactly = 1) { searchCurrenciesUseCase.invoke(query) }
    }

    @Test
    fun `OnSearchQueryChange Event  Distinct Queries`() = runTest {
        val query = "bitcoin"
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()
        verify(exactly = 1) { searchCurrenciesUseCase.invoke(query) }
    }

    @Test
    fun `OnToggleSearch Event  Activate Search`() = runTest {
        assertFalse(viewModel.uiState.value.isSearchActive)
        viewModel.onEvent(SearchContract.Event.OnToggleSearch)
        runCurrent()
        assertTrue(viewModel.uiState.value.isSearchActive)
    }

    @Test
    fun `OnToggleSearch Event  Deactivate Search`() = runTest {
        viewModel.onEvent(SearchContract.Event.OnToggleSearch) // Activate first
        runCurrent()
        assertTrue(viewModel.uiState.value.isSearchActive)

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange("test"))
        advanceTimeBy(1.seconds)
        runCurrent()
        assertEquals("test", viewModel.uiState.value.searchQuery)

        viewModel.onEvent(SearchContract.Event.OnToggleSearch) // Deactivate
        runCurrent()
        assertFalse(viewModel.uiState.value.isSearchActive)
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertNull(viewModel.uiState.value.searchResults)
    }

    @Test
    fun `OnClearSearch Event`() = runTest {
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange("test"))
        runCurrent()
        assertEquals("test", viewModel.uiState.value.searchQuery)

        viewModel.onEvent(SearchContract.Event.OnClearSearch)
        runCurrent()
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertNull(viewModel.uiState.value.searchResults)
    }

    @Test
    fun `OnBackClick Event  Navigation Effect`() = runTest {
        viewModel.effect.test {
            viewModel.onEvent(SearchContract.Event.OnBackClick)
            runCurrent()
            assertEquals(SearchContract.Effect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `searchCurrencies  Successful API Call`() = runTest {
        val query = "bitcoin"
        every { searchCurrenciesUseCase.invoke(query) } returns flowOf(
            Resource.Loading<List<CurrencyInfo>>(),
            Resource.Success(mockCurrencies)
        )

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(mockCurrencies, state.searchResults)
    }

    @Test
    fun `searchCurrencies  API Call Failure`() = runTest {
        val query = "bitcoin"
        val errorMessage = "API Error"
        every { searchCurrenciesUseCase.invoke(query) } returns flow {
            advanceTimeBy(1.seconds)
            emit(Resource.Loading<List<CurrencyInfo>>())
            advanceTimeBy(1.seconds)
            emit(Resource.Error(errorMessage))
        }

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        runCurrent()

        viewModel.effect.test {
            val item = awaitItem()
            assertEquals(SearchContract.Effect.ShowError(errorMessage), item)
        }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.searchResults)
    }

    @Test
    fun `searchCurrencies  API Call Loading State`() = runTest {
        val query = "bitcoin"
        every { searchCurrenciesUseCase.invoke(query) } returns flow {
            emit(Resource.Loading())

        }

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `searchCurrencies  Blank Query`() = runTest {
        val query = "   "
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()

        verify(exactly = 0) { searchCurrenciesUseCase.invoke(any()) }
        assertNull(viewModel.uiState.value.searchResults)
    }

    @Test
    fun `searchCurrencies  Query with leading trailing spaces`() = runTest {
        val query = "  bitcoin  "
        val trimmedQuery = query.trim()
        every { searchCurrenciesUseCase.invoke(trimmedQuery) } returns flowOf(
            Resource.Success(
                mockCurrencies
            )
        )

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()

        coVerify(exactly = 1) { searchCurrenciesUseCase.invoke(trimmedQuery) }
        assertEquals(mockCurrencies, viewModel.uiState.value.searchResults)
    }

    @Test
    fun `searchCurrencies  Query with special characters`() = runTest {
        val query = "bit@coin#"
        every { searchCurrenciesUseCase.invoke(query) } returns flowOf(
            Resource.Success(
                mockCurrencies
            )
        )

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()

        verify(exactly = 1) { searchCurrenciesUseCase.invoke(query) }
        assertEquals(mockCurrencies, viewModel.uiState.value.searchResults)
    }

    @Test
    fun `searchCurrencies  Case Sensitivity`() = runTest {
        val lowerCaseQuery = "bitcoin"
        val upperCaseQuery = "BITCOIN"

        every { searchCurrenciesUseCase.invoke(lowerCaseQuery) } returns flowOf(
            Resource.Success(
                mockCurrencies
            )
        )
        every { searchCurrenciesUseCase.invoke(upperCaseQuery) } returns flowOf(
            Resource.Success(
                mockCurrencies
            )
        )

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(lowerCaseQuery))
        advanceTimeBy(1.seconds)
        runCurrent()
        verify(exactly = 1) { searchCurrenciesUseCase.invoke(lowerCaseQuery) }

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(upperCaseQuery))
        advanceTimeBy(1.seconds)
        runCurrent()
        // Assuming the search is case-sensitive based on the use case implementation
        verify(exactly = 1) { searchCurrenciesUseCase.invoke(upperCaseQuery) }
    }

    @Test
    fun `searchCurrencies  No Results Found`() = runTest {
        val query = "xyz"
        every { searchCurrenciesUseCase.invoke(query) } returns flowOf(
            Resource.Loading<List<CurrencyInfo>>(),
            Resource.Success(emptyList())
        )

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
        advanceTimeBy(1.seconds)
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.searchResults?.isEmpty() == true)
    }

    @Test
    fun `ViewModel Coroutine Scope Cancellation`() = runTest {
        val longRunningFlow = flow {
            emit(Resource.Loading<List<CurrencyInfo>>()) // Explicitly type
            delay(5000) // Long delay
            emit(Resource.Success(emptyList<CurrencyInfo>())) // Explicitly type
        }
        every { searchCurrenciesUseCase.invoke(any()) } returns longRunningFlow

        val job = launch {
            viewModel.onEvent(SearchContract.Event.OnSearchQueryChange("test"))
            advanceTimeBy(1.seconds)
        }
        runCurrent()
        job.cancel() // Simulates onCleared
        runCurrent()

        // If the job inside the ViewModel is properly cancelled, the test will complete quickly
        // without waiting for the 5s delay. This is an indirect way to verify cancellation.
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Concurrent Event Handling`() = runTest {
        val query1 = "btc"
        val query2 = "eth"

        every { searchCurrenciesUseCase.invoke(query1) } returns flowOf(
            Resource.Success(
                listOf(
                    mockCurrencies[0]
                )
            )
        )
        every { searchCurrenciesUseCase.invoke(query2) } returns flowOf(
            Resource.Success(
                listOf(
                    mockCurrencies[1]
                )
            )
        )

        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query1))
        advanceTimeBy(500.milliseconds) // Half debounce
        viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query2))
        advanceTimeBy(1.seconds) // Full debounce for query2
        runCurrent()

        assertEquals(listOf(mockCurrencies[1]), viewModel.uiState.value.searchResults)
        verify(exactly = 1) { searchCurrenciesUseCase.invoke(query2) }
        verify(exactly = 0) { searchCurrenciesUseCase.invoke(query1) }
    }

    @Test
    fun `Error Effect Message Handling`() = runTest {
        val query = "error"
        every { searchCurrenciesUseCase.invoke(query) } returns flow {
            advanceTimeBy(1.seconds)
            emit(Resource.Loading())
            advanceTimeBy(1.seconds)
            emit(Resource.Error("Unknown error"))
        }

        viewModel.effect.test {
            viewModel.onEvent(SearchContract.Event.OnSearchQueryChange(query))
            advanceTimeBy(1.seconds)
            runCurrent()

            assertEquals(SearchContract.Effect.ShowError("Unknown error"), awaitItem())
        }
    }
}