package com.gmail.cristiandeives.switchhub.http

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface NintendoEshopService {
    @GET("/json/content/get/filter/game?system=switch&shop=ncom")
    fun getGames(
        @Query("sort") sort: SortBy,
        @Query("direction") sortDirection: SortDirection,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<GameResponse>

    enum class SortBy {
        Featured,
        Title,
        Price,
        ReleaseDate;

        override fun toString() = when(this) {
            Featured -> "featured"
            Title -> "title"
            Price -> "price"
            ReleaseDate -> "release"
        }
    }

    enum class SortDirection {
        Ascending,
        Descending;

        override fun toString() = when (this) {
            Ascending -> "asc"
            Descending -> "des"
        }
    }
}
