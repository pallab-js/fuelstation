package com.pallab.pumpmanager.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pallab.pumpmanager.feature.sales.SaleDao
import com.pallab.pumpmanager.feature.sales.SaleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestPumpManagerApp::class)
class DatabaseIntegrationTest {

    private lateinit var saleDao: SaleDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        saleDao = db.saleDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `insert sale and read from list`() = runBlocking {
        val sale = SaleEntity(
            id = "test-1",
            shiftId = "shift-1",
            fuelType = "Petrol",
            volumeLiters = 10.0,
            pricePerLiter = 100.0,
            totalAmount = 1000.0,
            paymentMode = "CASH",
            timestamp = System.currentTimeMillis()
        )
        saleDao.insertSale(sale)
        val all = saleDao.getAllSales().first()
        assertEquals(1, all.size)
        assertEquals("test-1", all[0].id)
        assertEquals(1000.0, all[0].totalAmount, 0.0)
    }

    @Test
    fun `get sale by id returns correct record`() = runBlocking {
        val sale = SaleEntity(
            id = "test-2",
            shiftId = "shift-1",
            fuelType = "Diesel",
            volumeLiters = 20.0,
            pricePerLiter = 90.0,
            totalAmount = 1800.0,
            paymentMode = "UPI",
            timestamp = System.currentTimeMillis()
        )
        saleDao.insertSale(sale)
        val fetched = saleDao.getSaleById("test-2")
        assertEquals("test-2", fetched?.id)
        assertEquals("UPI", fetched?.paymentMode)
    }

    @Test
    fun `update shift persists closing meter and status`() = runBlocking {
        val shift = com.pallab.pumpmanager.feature.shift.ShiftEntity(
            id = "shift-test-1",
            attendantId = "user-1",
            startTime = 1000L,
            endTime = null,
            openingMeterReading = 1000.0,
            closingMeterReading = null,
            status = "active"
        )
        db.shiftDao().insertShift(shift)
        db.shiftDao().updateShift(shift.copy(
            endTime = 2000L,
            closingMeterReading = 1500.0,
            status = "closed"
        ))
        val updated = db.shiftDao().getShiftById("shift-test-1")
        assertNotNull(updated)
        assertEquals("closed", updated?.status)
        assertEquals(1500.0, updated!!.closingMeterReading!!, 0.0)
        assertEquals(2000L, updated?.endTime)
    }

    @Test
    fun `insert duplicate id replaces existing`() = runBlocking {
        val sale = SaleEntity("dup", "s1", "Petrol", 5.0, 100.0, 500.0, "CASH", 1000L)
        val updated = sale.copy(totalAmount = 999.0)
        saleDao.insertSale(sale)
        saleDao.insertSale(updated)
        val all = saleDao.getAllSales().first()
        assertEquals(1, all.size)
        assertEquals(999.0, all[0].totalAmount, 0.0)
    }
}
