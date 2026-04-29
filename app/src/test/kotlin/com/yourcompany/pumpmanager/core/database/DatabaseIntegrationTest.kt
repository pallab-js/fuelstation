package com.yourcompany.pumpmanager.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yourcompany.pumpmanager.feature.sales.SaleDao
import com.yourcompany.pumpmanager.feature.sales.SaleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class DatabaseIntegrationTest {

    private lateinit var saleDao: SaleDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
        saleDao = db.saleDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeSaleAndReadInList() = runBlocking {
        val sale = SaleEntity(
            id = "test_sale_1",
            shiftId = "shift_1",
            fuelType = "PETROL",
            volumeLiters = 10.0,
            pricePerLiter = 100.0,
            totalAmount = 1000.0,
            paymentMode = "CASH",
            timestamp = System.currentTimeMillis()
        )
        saleDao.insertSale(sale)
        val allSales = saleDao.getAllSales().first()
        assertEquals(allSales[0].id, "test_sale_1")
        assertEquals(allSales[0].totalAmount, 1000.0, 0.0)
    }

    @Test
    @Throws(Exception::class)
    fun getSaleById() = runBlocking {
        val sale = SaleEntity(
            id = "test_sale_2",
            shiftId = "shift_1",
            fuelType = "DIESEL",
            volumeLiters = 20.0,
            pricePerLiter = 90.0,
            totalAmount = 1800.0,
            paymentMode = "UPI",
            timestamp = System.currentTimeMillis()
        )
        saleDao.insertSale(sale)
        val fetchedSale = saleDao.getSaleById("test_sale_2")
        assertEquals(fetchedSale?.id, "test_sale_2")
        assertEquals(fetchedSale?.paymentMode, "UPI")
    }
}
