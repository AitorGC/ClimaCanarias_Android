package com.example.repository

import com.example.data.BeachDao
import com.example.data.BeachEntity
import com.example.data.BeachPartial
import kotlinx.coroutines.flow.Flow

class BeachRepository(private val beachDao: BeachDao) {

    fun getPartialBeaches(): Flow<List<BeachPartial>> {
        return beachDao.getPartialBeaches()
    }

    suspend fun getBeachDetailsById(id: String): BeachEntity {
        return beachDao.getBeachDetailsById(id)
    }
}
