package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Lead
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY recontactDate ASC")
    fun getAllLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE area = :area ORDER BY recontactDate ASC")
    fun getLeadsByArea(area: String): Flow<List<Lead>>

    @Query("SELECT DISTINCT area FROM leads ORDER BY area ASC")
    fun getDistinctAreas(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead): Long

    @Delete
    suspend fun deleteLead(lead: Lead)
}
