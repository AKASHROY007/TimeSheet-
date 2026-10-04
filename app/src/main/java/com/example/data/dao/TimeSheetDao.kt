package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdvanceEntity
import com.example.data.model.DayRecordEntity
import com.example.data.model.TimeSheetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSheetDao {

    // Sheet operations
    @Query("SELECT * FROM time_sheets WHERE month = :month ORDER BY createdAt ASC")
    fun getSheetsForMonth(month: String): Flow<List<TimeSheetEntity>>

    @Query("SELECT * FROM time_sheets WHERE id = :sheetId LIMIT 1")
    suspend fun getSheetById(sheetId: String): TimeSheetEntity?

    @Query("SELECT * FROM time_sheets ORDER BY month ASC, createdAt ASC")
    fun getAllSheets(): Flow<List<TimeSheetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSheet(sheet: TimeSheetEntity)

    @Query("DELETE FROM time_sheets WHERE id = :sheetId")
    suspend fun deleteSheet(sheetId: String)

    // Day record operations
    @Query("SELECT * FROM day_records WHERE sheetId = :sheetId")
    fun getRecordsForSheet(sheetId: String): Flow<List<DayRecordEntity>>

    @Query("SELECT * FROM day_records WHERE sheetId = :sheetId")
    suspend fun getRecordsListForSheet(sheetId: String): List<DayRecordEntity>

    @Query("SELECT * FROM day_records WHERE sheetId = :sheetId AND date = :date LIMIT 1")
    suspend fun getRecord(sheetId: String, date: String): DayRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: DayRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<DayRecordEntity>)

    @Query("DELETE FROM day_records WHERE sheetId = :sheetId")
    suspend fun deleteRecordsForSheet(sheetId: String)

    // Advances operations
    @Query("SELECT * FROM advances WHERE sheetId = :sheetId ORDER BY date ASC, id ASC")
    fun getAdvancesForSheet(sheetId: String): Flow<List<AdvanceEntity>>

    @Query("SELECT * FROM advances WHERE sheetId = :sheetId ORDER BY date ASC, id ASC")
    suspend fun getAdvancesListForSheet(sheetId: String): List<AdvanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: AdvanceEntity)

    @Query("DELETE FROM advances WHERE id = :id")
    suspend fun deleteAdvance(id: Long)

    @Query("DELETE FROM advances WHERE sheetId = :sheetId")
    suspend fun deleteAdvancesForSheet(sheetId: String)

    // Lifetime calculation queries
    @Query("SELECT * FROM day_records WHERE status = 'present'")
    fun getAllPresentRecords(): Flow<List<DayRecordEntity>>

    // Reset database
    @Query("DELETE FROM time_sheets")
    suspend fun clearAllSheets()

    @Query("DELETE FROM day_records")
    suspend fun clearAllRecords()

    @Query("DELETE FROM advances")
    suspend fun clearAllAdvances()
}
