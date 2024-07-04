
package com.example.cheesechase

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://chasedeux.vercel.app/"

    var retrofit: Retrofit?= null

    fun getInstance():Retrofit?{
        val builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
        retrofit = builder.build()
        return retrofit
    }
}
