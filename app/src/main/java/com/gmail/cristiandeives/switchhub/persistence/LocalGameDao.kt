package com.gmail.cristiandeives.switchhub.persistence

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread

@Dao
internal interface LocalGameDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(localGame: LocalGame)

    @WorkerThread
    @Query("SELECT EXISTS(SELECT * FROM localGame WHERE id = :id)")
    fun exists(id: String): Boolean

    @UiThread
    @Query("SELECT * FROM localGame WHERE id = :id")
    fun select(id: String): LiveData<LocalGame>

    @UiThread
    @Query("SELECT * FROM localGame")
    fun selectAll(): LiveData<List<LocalGame>>

    @WorkerThread
    @Query("UPDATE localGame SET userList = :userList WHERE id = :id")
    fun updateUserList(id: String, userList: LocalGame.UserList)
}