package io.trieulh.currencydemo.data.util

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkBoundResourceTest {

    @Test
    fun `returns success from database when fetch not needed`() = runBlocking {
        val dbFlow = MutableStateFlow("cached")
        var fetchCalled = false

        val resource = object : NetworkBoundResource<String, String>() {
            override fun loadFromDb(): Flow<String> = dbFlow
            override fun shouldFetch(data: String?): Boolean = false
            override suspend fun fetchFromNetwork(): String {
                fetchCalled = true
                return "network"
            }
            override suspend fun saveNetworkResult(item: String) {
                dbFlow.value = item
            }
        }

        resource.asFlow().test {
            assertEquals(Resource.Success("cached"), awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(fetchCalled)
    }

    @Test
    fun `emits loading then success when network fetch succeeds`() = runBlocking {
        val dbFlow = MutableStateFlow("cached")
        var fetchedTimes = 0
        var lastSaved: String? = null

        val resource = object : NetworkBoundResource<String, String>() {
            override fun loadFromDb(): Flow<String> = dbFlow
            override fun shouldFetch(data: String?): Boolean = true
            override suspend fun fetchFromNetwork(): String {
                fetchedTimes += 1
                return "network"
            }
            override suspend fun saveNetworkResult(item: String) {
                lastSaved = item
                dbFlow.value = "updated"
            }
        }

        resource.asFlow().test {
            assertEquals(Resource.Loading("cached"), awaitItem())
            assertEquals(Resource.Success("updated"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, fetchedTimes)
        assertEquals("network", lastSaved)
    }

    @Test
    fun `emits loading then error when network fetch fails`() = runBlocking {
        val dbFlow = MutableStateFlow("cached")
        var failureCaptured: Throwable? = null
        var saveCalled = false

        val resource = object : NetworkBoundResource<String, String>() {
            override fun loadFromDb(): Flow<String> = dbFlow
            override fun shouldFetch(data: String?): Boolean = true
            override suspend fun fetchFromNetwork(): String {
                throw IllegalStateException("boom")
            }
            override suspend fun saveNetworkResult(item: String) {
                saveCalled = true
            }
            override fun onFetchFailed(throwable: Throwable) {
                failureCaptured = throwable
            }
        }

        resource.asFlow().test {
            assertEquals(Resource.Loading("cached"), awaitItem())
            assertEquals(Resource.Error("boom", "cached"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(saveCalled)
        assertTrue(failureCaptured is IllegalStateException)
        assertEquals("boom", failureCaptured?.message)
    }
}
