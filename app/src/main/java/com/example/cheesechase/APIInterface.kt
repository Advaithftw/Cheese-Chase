package com.example.cheesechase


import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {

    @GET("/obstacleLimit")
    fun getObstacleLimit(): Call<obstacleLimit>

    @GET("/image")
    fun getImage(
        @Query("character") character: String
    ): Call<ResponseBody>



}