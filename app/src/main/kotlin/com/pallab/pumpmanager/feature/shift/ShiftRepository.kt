package com.pallab.pumpmanager.feature.shift

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ShiftRepository {
    fun getActiveShift(): Flow<ShiftEntity?>
    fun getAllShifts(): Flow<List<ShiftEntity>>
    suspend fun getShiftById(id: String): ShiftEntity?
    suspend fun insertShift(shift: ShiftEntity)
    suspend fun updateShift(shift: ShiftEntity)
    fun getShiftsPaged(): Flow<PagingData<ShiftEntity>>
}

@Singleton
class ShiftRepositoryImpl @Inject constructor(private val shiftDao: ShiftDao) : ShiftRepository {
    override fun getActiveShift() = shiftDao.getActiveShift()
    override fun getAllShifts() = shiftDao.getAllShifts()
    override suspend fun getShiftById(id: String) = shiftDao.getShiftById(id)
    override suspend fun insertShift(shift: ShiftEntity) = shiftDao.insertShift(shift)
    override suspend fun updateShift(shift: ShiftEntity) = shiftDao.updateShift(shift)
    override fun getShiftsPaged(): Flow<PagingData<ShiftEntity>> = Pager(
        config = PagingConfig(pageSize = 30, enablePlaceholders = false),
        pagingSourceFactory = { shiftDao.getShiftsPaged() }
    ).flow
}
