package com.gmail.cristiandeives.switchhub.persistence

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.net.Uri
import android.util.Log
import com.squareup.moshi.Moshi
import java.math.BigDecimal
import java.util.Date

/**
 * Database version history:
 *
 * 1: Initial version. Contains LocalGame.
 * 2: Create Game, with data from the Internet + LocalGame
 */
@Database(entities = [Game::class], version = 2)
@TypeConverters(AppDatabase::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    object MigrationV1V2 : Migration(1, 2) {
        private val TAG = MigrationV1V2::class.java.simpleName

        override fun migrate(database: SupportSQLiteDatabase) {
            Log.v(TAG, "> migrate(database=$database)")

            database.beginTransaction()
            try {
                database.execSQL("CREATE TABLE `Game` (`id` TEXT NOT NULL, `nsuid` TEXT NOT NULL, `title` TEXT NOT NULL, `releaseDate` INTEGER, `releaseDateDisplay` TEXT NOT NULL, `price` REAL, `frontBoxArtUrl` TEXT NOT NULL, `videoLink` TEXT NOT NULL, `numberOfPlayers` TEXT NOT NULL, `categories` TEXT NOT NULL, `buyItNow` INTEGER NOT NULL, `featuredIndex` INTEGER NOT NULL, `userList` INTEGER NOT NULL, PRIMARY KEY(`id`))")

                val cursor = database.query("SELECT id, userList FROM LocalGame")
                Log.d(TAG, "migrating ${cursor.count} rows from LocalGame to Game")

                cursor.use { c ->
                    if (c.moveToFirst()) {
                        do {
                            val id = cursor.getString(0)
                            val userList = cursor.getInt(1)
                            val params = arrayOf(id, "", "", null, "", null, Uri.EMPTY, Uri.EMPTY,
                                "", emptyList<String>(), false, 0, userList)

                            database.execSQL("INSERT INTO Game (id, nsuid, title, releaseDate, releaseDateDisplay, price, frontBoxArtUrl, videoLink, numberOfPlayers, categories, buyItNow, featuredIndex, userList) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", params)
                        } while (c.moveToNext())
                    }
                }

                database.execSQL("DROP TABLE LocalGame")

                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }

            Log.v(TAG, "< migrate(database=$database)")
        }
    }

    companion object {
        private val MOSHI = Moshi.Builder().build()
        private val MOSHI_STRING_LIST_ADAPTER = MOSHI.adapter(Array<String>::class.java)
        private val TAG = AppDatabase::class.java.simpleName

        @JvmStatic
        @TypeConverter
        fun fromBigDecimal(bigDecimal: BigDecimal?) = bigDecimal?.toDouble()

        @JvmStatic
        @TypeConverter
        fun toBigDecimal(d: Double?) = d?.let { BigDecimal(it) }

        @JvmStatic
        @TypeConverter
        fun fromDate(date: Date) = date.time

        @JvmStatic
        @TypeConverter
        fun toDate(time: Long) = Date(time)

        @JvmStatic
        @TypeConverter
        fun fromStringList(list: List<String>): String = MOSHI_STRING_LIST_ADAPTER.toJson(list.toTypedArray())

        @JvmStatic
        @TypeConverter
        fun toStringList(str: String) = MOSHI_STRING_LIST_ADAPTER.fromJson(str)?.toList() ?: emptyList()

        @JvmStatic
        @TypeConverter
        fun fromUri(uri: Uri?) = uri?.toString()

        @JvmStatic
        @TypeConverter
        fun toUri(str: String?): Uri? = str?.let { Uri.parse(it) }

        @JvmStatic
        @TypeConverter
        fun fromUserList(userList: Game.UserList) = userList.code

        @JvmStatic
        @TypeConverter
        fun toUserList(code: Int) = Game.UserList.values().find { it.code == code }
                ?: Game.UserList.NONE.also {
                    Log.w(TAG, "toUserList: invalid list code: $code; using ${Game.UserList.NONE}")
                }
    }
}