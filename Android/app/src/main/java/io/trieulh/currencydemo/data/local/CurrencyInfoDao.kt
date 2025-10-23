
package io.trieulh.currencydemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.trieulh.currencydemo.data.local.entity.CurrencyInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyInfoEntity>)

    @Query("SELECT * FROM currencies")
    fun getCurrencies(): Flow<List<CurrencyInfoEntity>>

    @Query("DELETE FROM currencies")
    suspend fun clearCurrencies()
}
