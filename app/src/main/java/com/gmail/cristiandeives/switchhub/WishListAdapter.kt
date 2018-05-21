package com.gmail.cristiandeives.switchhub

import com.gmail.cristiandeives.switchhub.persistence.Game

internal class WishListAdapter : GamesAdapter() {
    override fun shouldGameBeDisplayed(game: Game) = game.userList == Game.UserList.WISH
}