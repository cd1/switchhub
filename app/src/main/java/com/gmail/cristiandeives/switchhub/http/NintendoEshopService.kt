package com.gmail.cristiandeives.switchhub.http

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface NintendoEshopService {
    @GET("/json/content/get/filter/game?system=switch&shop=ncom&sort=featured")
    fun getGames(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<GameResponse>
}
