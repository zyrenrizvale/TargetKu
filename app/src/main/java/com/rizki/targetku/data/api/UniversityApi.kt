package com.rizki.targetku.data.api

import com.rizki.targetku.data.models.University
import retrofit2.http.GET
import retrofit2.http.Query

interface UniversityApiService {
    @GET("search")
    suspend fun searchUniversities(
        @Query("name") name: String
    ): List<University>
}
