package com.pallab.pumpmanager.feature.sales

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE timestamp >= :startOfDay")
    fun getTodaySales(startOfDay: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getSalesPaged(): PagingSource<Int, SaleEntity>

    @Query("SELECT * FROM sales WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getSalesByShiftId(shiftId: String): Flow<List<SaleEntity>>

    @Query("SELECT SUM(total_amount) FROM sales WHERE shift_id = :shiftId")
    suspend fun getTotalRevenueForShift(shiftId: String): Double?

    @Query("SELECT SUM(volume_liters) FROM sales WHERE shift_id = :shiftId AND fuel_type = :fuelType")
    suspend fun getTotalVolumeForShiftByFuel(shiftId: String, fuelType: String): Double?

    @Query("""
        SELECT strftime('%d/%m', datetime(timestamp / 1000, 'unixepoch', 'localtime')) AS day,
               SUM(total_amount) AS revenue
        FROM sales
        WHERE timestamp >= :windowStart
        GROUP BY day
        ORDER BY timestamp ASC
    """)
    suspend fun getRevenueTrendSince(windowStart: Long): List<DayRevenue>

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: String)
}
