package com.gmail.cristiandeives.switchhub

internal class WishListFragment : ListFragment() {
    override val gamesAdapter = WishListAdapter()

    override fun noGamesSuggestion() = getString(R.string.wish_list_no_games_suggestion)
}