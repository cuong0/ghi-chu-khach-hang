package com.example.data.repository

import com.example.data.db.LeadDao
import com.example.data.model.Lead
import kotlinx.coroutines.flow.Flow

class LeadRepository(private val leadDao: LeadDao) {
    val allLeads: Flow<List<Lead>> = leadDao.getAllLeads()
    val distinctAreas: Flow<List<String>> = leadDao.getDistinctAreas()

    fun getLeadsByArea(area: String): Flow<List<Lead>> {
        return leadDao.getLeadsByArea(area)
    }

    suspend fun insert(lead: Lead): Long {
        return leadDao.insertLead(lead)
    }

    suspend fun delete(lead: Lead) {
        leadDao.deleteLead(lead)
    }
}
