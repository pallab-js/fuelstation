package com.pallab.pumpmanager.feature.sales

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface SalesRepository {
    fun getAllSales(): Flow<List<SaleEntity>>
    fun getSalesByShiftId(shiftId: String): Flow<List<SaleEntity>>
    fun getTodaySales(startOfDay: Long): Flow<List<SaleEntity>>
    suspend fun getTotalRevenueForShift(shiftId: String): Double?
    suspend fun getTotalVolumeForShiftByFuel(shiftId: String, fuelType: String): Double?
    suspend fun getRevenueTrendSince(windowStart: Long): List<DayRevenue>
    suspend fun insertSale(sale: SaleEntity)
    suspend fun deleteSale(id: String)
    fun getSalesPaged(): Flow<PagingData<SaleEntity>>
}

@Singleton
class SalesRepositoryImpl @Inject constructor(private val saleDao: SaleDao) : SalesRepository {
    override fun getAllSales() = saleDao.getAllSales()
    override fun getSalesByShiftId(shiftId: String) = saleDao.getSalesByShiftId(shiftId)
    override fun getTodaySales(startOfDay: Long) = saleDao.getTodaySales(startOfDay)
    override suspend fun getTotalRevenueForShift(shiftId: String) = saleDao.getTotalRevenueForShift(shiftId)
    override suspend fun getTotalVolumeForShiftByFuel(shiftId: String, fuelType: String) = saleDao.getTotalVolumeForShiftByFuel(shiftId, fuelType)
    override suspend fun getRevenueTrendSince(windowStart: Long) = saleDao.getRevenueTrendSince(windowStart)
    override suspend fun insertSale(sale: SaleEntity) = saleDao.insertSale(sale)
    override suspend fun deleteSale(id: String) = saleDao.deleteSale(id)
    override fun getSalesPaged(): Flow<PagingData<SaleEntity>> = Pager(
        config = PagingConfig(pageSize = 30, enablePlaceholders = false),
        pagingSourceFactory = { saleDao.getSalesPaged() }
    ).flow
}
