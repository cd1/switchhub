package com.gmail.cristiandeives.switchhub

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.support.annotation.MainThread
import android.util.Log
import com.gmail.cristiandeives.switchhub.persistence.Game
import com.gmail.cristiandeives.switchhub.persistence.Repository

@MainThread
internal class GameDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository.getInstance(app)

    var game: LiveData<Game>? = null
        private set

    var gameId: String? = null
        set(value) {
            field = value

            value?.let { id ->
                game = repo.getGame(id)
            } ?: Log.w(TAG, "cannot load game data with ID == null")
        }

    fun setGameUserList(userList: Game.UserList) {
        gameId?.let { id ->
            repo.updateGameUserList(id, userList)
        } ?: Log.d(TAG, "cannot update game user list with ID == null; ignoring update")
    }

    companion object {
        private val TAG = GameDetailsViewModel::class.java.simpleName
    }
}