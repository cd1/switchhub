package com.gmail.cristiandeives.switchhub

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.support.annotation.MainThread
import android.util.Log
import com.gmail.cristiandeives.switchhub.persistence.LocalGame
import com.gmail.cristiandeives.switchhub.persistence.Repository

@MainThread
internal class GameDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository.getInstance(app)

    var localGame: LiveData<LocalGame>? = null
        private set

    var gameId: String? = null
        set(value) {
            field = value

            value?.let { id ->
                localGame = repo.getLocalGame(id)

                repo.localGameExists(id, onSuccess = { exists ->
                    if (!exists) {
                        val game = LocalGame(id)
                        repo.saveLocalGame(game)
                    }
                })

            } ?: Log.w(TAG, "cannot load game data with ID == null")
        }

    fun setGameUserList(userList: LocalGame.UserList) {
        gameId?.let { id ->
            repo.updateLocalGameUserList(id, userList)
        } ?: Log.d(TAG, "cannot update game user list with ID == null; ignoring update")
    }

    companion object {
        private val TAG = GameDetailsViewModel::class.java.simpleName
    }
}