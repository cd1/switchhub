package com.gmail.cristiandeives.switchhub.http

import android.net.Uri
import android.util.Log
import com.gmail.cristiandeives.switchhub.NintendoGame
import java.text.DateFormat
import java.text.ParseException
import java.util.Locale

internal data class GameResponse(val filter: ResponseFilter, val games: ResponseGames)

internal data class ResponseGames(val offset: Int, val limit: Int, private val game: List<GamesGame>?) {
    fun toGameData() = game?.mapIndexed { index, g ->
        NintendoGame(
            id = g.id,
            nsuid = g.nsuid,
            featuredIndex = offset + index,
            title = g.title,
            releaseDate = try {
                US_DATE_FORMAT.parse(g.release_date)
            } catch (e: ParseException) {
                Log.w(TAG, "invalid release date format", e)
                null
            },
            releaseDateDisplay = g.release_date_display,
            price = g.eshop_price?.toBigDecimal(),
            frontBoxArtUrl = Uri.parse(g.front_box_art),
            videoLink = g.video_link?.let { Uri.parse("https://secure-cf-c.ooyala.com/$it/DOcJ-FxaFrRg4gtDEwOmk2OjBrO6qGv_") },
            numberOfPlayers = g.number_of_players,
            categories = g.categories.category,
            buyItNow = g.buyitnow.toBoolean()
        )
    } ?: emptyList()

    companion object {
        private val TAG = ResponseGames::class.java.simpleName
        private val US_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US)
    }
}

internal data class ResponseFilter(
    val availabilities: FilterAvailabilities,
    val categories: FilterCategories,
    val demo: Total,
    val dlc: Total,
    val esrbs: FilterEsrbs,
    val franchises: FilterFranchises,
    val free_start: Total,
    val how_to_play: FilterHowToPlay,
    val number_of_players: FilterNumberOfPlayers,
    val prices: FilterPrices,
    val publishers: FilterPublishers,
    val sale: Total,
    val shop: FilterShop,
    val systems: FilterSystems,
    val vcs: FilterVcs,
    val total: Int
)

internal data class Total(val total: Int)

internal data class FilterAvailabilities(val availability: List<NameTotal>, val total: Int)

internal data class NameTotal(val name: String, val total: Int)

internal data class FilterCategories(val category: List<NameTotal>, val total: Int)

internal data class FilterEsrbs(val esrbs: List<NameTotal>, val total: Int)

internal data class FilterFranchises(val franchises: List<NameTotal>, val total: Int)

internal data class FilterHowToPlay(val how_to_play: List<NameTotal>, val total: Int)

internal data class FilterNumberOfPlayers(val number_of_players: List<NameTotal>, val total: Int)

internal data class FilterPrices(val price: List<NameTotal>, val total: Int)

internal data class FilterPublishers(val publishers: List<NameTotal>, val total: Int)

internal data class FilterShop(val shop: List<NameTotal>, val total: Int)

internal data class FilterSystems(val system: List<NameTotal>, val total: Int)

internal data class FilterVcs(val vcs: List<NameTotal>, val total: Int)

internal data class GamesGame(
    val buyitnow: String,
    val slug: String,
    val release_date: String,
    val digitaldownload: String,
    val free_to_start: String,
    val title: String,
    val system: String,
    val id: String,
    val number_of_players: String,
    val release_date_display: String,
    val front_box_art: String,
    val game_code: String,
    val buyonline: String,
    val video_link: String?,
    val eshop_price: Double?,
    val ca_price: String?,
    val nsuid: String?,
    val categories: GameCategories
)

data class GameCategories(val category: List<String>)