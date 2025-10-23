package io.trieulh.currencydemo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.trieulh.currencydemo.data.local.entity.CurrencyInfoEntity
import io.trieulh.currencydemo.BuildConfig
import net.sqlcipher.database.SupportFactory

@Database(entities = [CurrencyInfoEntity::class], version = 2)
abstract class CurrencyDatabase : RoomDatabase() {

    abstract fun currencyDao(): CurrencyInfoDao

    companion object {
        @Volatile
        private var INSTANCE: CurrencyDatabase? = null

        fun getInstance(context: Context, passphrase: ByteArray): CurrencyDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    val factory = SupportFactory(passphrase)
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CurrencyDatabase::class.java, "currency.db"
                    )
                        .openHelperFactory(factory)
                        .fallbackToDestructiveMigration(false)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        fun generatePassphrase(context: Context): ByteArray {
            return BuildConfig.SQLCIPHER_PASSPHRASE.toByteArray()
        }
    }
}
