package com.gmail.cristiandeives.switchhub.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = [LocalGame::class], version = 1)
@TypeConverters(LocalGame::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun localGameDao(): LocalGameDao
}