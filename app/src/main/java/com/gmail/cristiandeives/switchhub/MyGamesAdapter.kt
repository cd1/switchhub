package com.gmail.cristiandeives.switchhub

import com.gmail.cristiandeives.switchhub.persistence.Game

internal class MyGamesAdapter : GamesAdapter() {
    override fun shouldGameBeDisplayed(game: Game) = game.userList == Game.UserList.OWNED
}