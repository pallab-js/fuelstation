package com.pallab.pumpmanager.core.di

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.room.Room
import com.pallab.pumpmanager.core.database.AppDatabase
import com.pallab.pumpmanager.core.database.Migrations
import com.pallab.pumpmanager.feature.auth.UserDao
import com.pallab.pumpmanager.feature.inventory.FuelTypeDao
import com.pallab.pumpmanager.feature.inventory.RefillLogDao
import com.pallab.pumpmanager.feature.inventory.TankDao
import com.pallab.pumpmanager.feature.sales.SaleDao
import com.pallab.pumpmanager.feature.shift.ShiftDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.security.KeyStore
import java.util.Base64
import javax.crypto.KeyGenerator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val passphrase = getDatabasePassphrase(context)
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pump_manager_db"
        ).openHelperFactory(factory)
            .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3, Migrations.MIGRATION_3_4).build()
    }

    @Provides @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides @Singleton
    fun provideSaleDao(database: AppDatabase): SaleDao = database.saleDao()

    @Provides @Singleton
    fun provideShiftDao(database: AppDatabase): ShiftDao = database.shiftDao()

    @Provides @Singleton
    fun provideTankDao(database: AppDatabase): TankDao = database.tankDao()

    @Provides @Singleton
    fun provideFuelTypeDao(database: AppDatabase): FuelTypeDao = database.fuelTypeDao()

    @Provides @Singleton
    fun provideRefillLogDao(database: AppDatabase): RefillLogDao = database.refillLogDao()

    private fun getDatabasePassphrase(context: Context): ByteArray {
        return try {
            val alias = "pump_manager_db_key"
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (!keyStore.containsAlias(alias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
                keyGenerator.generateKey()
            }
            val secretKey = (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
            Base64.getEncoder().encode(secretKey.encoded)
        } catch (e: Exception) {
            android.util.Log.w("DatabaseModule", "AndroidKeyStore unavailable, using fallback", e)
            "pump_manager_fallback_key".toByteArray(Charsets.UTF_8)
        }
    }
}
