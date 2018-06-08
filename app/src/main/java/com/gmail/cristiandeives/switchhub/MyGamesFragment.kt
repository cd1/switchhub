package com.gmail.cristiandeives.switchhub

internal class MyGamesFragment : ListFragment() {
    override val gamesAdapter = MyGamesAdapter()

    override fun noGamesSuggestion() = getString(R.string.my_games_no_games_suggestion)
}