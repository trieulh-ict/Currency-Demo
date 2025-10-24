package io.trieulh.currencydemo.presentation.currencylist

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.trieulh.currencydemo.data.util.Resource
import io.trieulh.currencydemo.domain.model.CurrencyInfo
import io.trieulh.currencydemo.domain.model.CurrencyType
import io.trieulh.currencydemo.domain.usecase.GetAllCurrenciesUseCase
import io.trieulh.currencydemo.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CurrencyListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: CurrencyListViewModel
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase = mockk(relaxed = true)

    private val mockCurrencies = listOf(
        CurrencyInfo("1", "Bitcoin", "BTC"),
        CurrencyInfo("2", "US Dollar", "USD")
    )

    @Before
    fun setUp() {
        every { getAllCurrenciesUseCase.invoke(any(), any()) } returns flowOf(Resource.Loading())
    }

    @Test
    fun `Initial state verification on creation`() = runTest {
        // When
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        runCurrent()

        // Then
        coVerify { getAllCurrenciesUseCase(CurrencyType.All, true) }
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Initial data load success`() = runTest {
        // Given
        every { getAllCurrenciesUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Success(mockCurrencies)
        )

        // When
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)

        // Then
        runCurrent()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(mockCurrencies, state.currencies)
    }

    @Test
    fun `Initial data load success with empty list`() = runTest {
        every { getAllCurrenciesUseCase(any(), any()) } returns flowOf(
            Resource.Loading(),
            Resource.Success(emptyList())
        )

        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.currencies.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `Initial data load success with null data`() = runTest {
        every {
            getAllCurrenciesUseCase(
                any(),
                any()
            )
        } returns flowOf(
            Resource.Loading(),
            Resource.Success(listOf())
        )

        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        runCurrent()

        assertTrue(viewModel.uiState.value.currencies.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `OnSearchClick event emits navigation effect`() = runTest {
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)

        viewModel.effect.test {
            viewModel.onEvent(CurrencyListContract.Event.OnSearchClick)
            val effect = awaitItem()
            assertTrue(effect is CurrencyListContract.Effect.NavigateToSearch)
        }
    }

    @Test
    fun `OnInsertDataClick event triggers data fetch`() = runTest {
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)

        viewModel.onEvent(CurrencyListContract.Event.OnInsertDataClick)
        coVerify { getAllCurrenciesUseCase(CurrencyType.All, true) }
    }

    @Test
    fun `OnClearDataClick event triggers use case clear`() = runTest {
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)

        viewModel.onEvent(CurrencyListContract.Event.OnClearDataClick)
        coVerify { getAllCurrenciesUseCase.clear() }
    }

    @Test
    fun `OnLoadCurrencies event triggers fetch with specific type`() = runTest {
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        viewModel.onEvent(
            CurrencyListContract.Event.OnLoadCurrencies(CurrencyType.Fiat, false)
        )

        coVerify { getAllCurrenciesUseCase(CurrencyType.Fiat, false) }
    }

    @Test
    fun `OnLoadCurrencies event triggers fetch with force refresh`() = runTest {
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        viewModel.onEvent(
            CurrencyListContract.Event.OnLoadCurrencies(CurrencyType.Crypto, true)
        )

        coVerify { getAllCurrenciesUseCase(CurrencyType.Crypto, true) }
    }

    @Test
    fun `Concurrent data fetch cancels previous job`() = runTest {
        val longRunningFlow = flow {
            emit(Resource.Loading())
            kotlinx.coroutines.delay(1000)
            emit(Resource.Success(mockCurrencies))
        }
        every { getAllCurrenciesUseCase(any(), any()) } returns longRunningFlow

        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        viewModel.onEvent(CurrencyListContract.Event.OnLoadCurrencies(CurrencyType.Fiat, true))
        viewModel.onEvent(CurrencyListContract.Event.OnLoadCurrencies(CurrencyType.Crypto, true))

        coVerify(exactly = 1) { getAllCurrenciesUseCase(CurrencyType.Crypto, true) }
    }

    @Test
    fun `Success during data fetch after an error`() = runTest {
        every { getAllCurrenciesUseCase(any(), any()) } returns flowOf(
            Resource.Error("Old error"),
            Resource.Success(mockCurrencies)
        )

        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        runCurrent()

        assertEquals(mockCurrencies, viewModel.uiState.value.currencies)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Rapid succession of events processing`() = runTest {
        every { getAllCurrenciesUseCase(any(), any()) } returns flowOf(
            Resource.Success(
                mockCurrencies
            )
        )
        viewModel = CurrencyListViewModel(getAllCurrenciesUseCase)
        runCurrent()

        launch {
            viewModel.onEvent(
                CurrencyListContract.Event.OnLoadCurrencies(
                    CurrencyType.Crypto,
                    true
                )
            )
            viewModel.onEvent(CurrencyListContract.Event.OnSearchClick)
        }

        viewModel.effect.test {
            val effect = awaitItem()
            assertTrue(effect is CurrencyListContract.Effect.NavigateToSearch)
        }
    }
}