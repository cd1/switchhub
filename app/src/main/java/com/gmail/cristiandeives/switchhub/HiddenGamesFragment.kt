package com.gmail.cristiandeives.switchhub

internal class HiddenGamesFragment : ListFragment() {
    override val gamesAdapter = HiddenGamesAdapter()

    override fun noGamesSuggestion() = getString(R.string.hidden_games_no_games_suggestion)
}