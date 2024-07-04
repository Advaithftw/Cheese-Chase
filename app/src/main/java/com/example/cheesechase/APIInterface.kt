package com.example.cheesechase


import retrofit2.Call
import retrofit2.http.GET

interface APIInterface {

    @GET("/obstacleLimit")
    fun getObstacleLimit(): Call<obstacleLimit>



}