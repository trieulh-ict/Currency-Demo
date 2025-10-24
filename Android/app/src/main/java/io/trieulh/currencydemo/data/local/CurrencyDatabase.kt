package io.trieulh.currencydemo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.trieulh.currencydemo.data.local.entity.CurrencyInfoEntity

@Database(entities = [CurrencyInfoEntity::class], version = 2)
abstract class CurrencyDatabase : RoomDatabase() {

    abstract fun currencyDao(): CurrencyInfoDao
}
