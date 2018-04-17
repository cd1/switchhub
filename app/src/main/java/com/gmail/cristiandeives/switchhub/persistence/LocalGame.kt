package com.gmail.cristiandeives.switchhub.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter
import android.os.Parcel
import android.os.Parcelable
import android.util.Log

@Entity
internal data class LocalGame(
    @PrimaryKey
    var id: String,
    var userList: UserList = UserList.NONE
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        toUserList(source.readInt())
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.run {
        writeString(id)
        writeInt(fromUserList(userList))
    }

    override fun describeContents() = 0

    internal enum class UserList(val code: Int) {
        NONE(0),
        WISH(1),
        OWNED(2),
        HIDDEN(3),
    }

    companion object {
        private val TAG = LocalGame::class.java.simpleName

        @JvmField
        val CREATOR = object : Parcelable.Creator<LocalGame> {
            override fun createFromParcel(source: Parcel) = LocalGame(source)

            override fun newArray(size: Int) = arrayOfNulls<LocalGame>(size)
        }

        @JvmStatic
        @TypeConverter
        fun toUserList(code: Int) = UserList.values().find { it.code == code }
                ?: UserList.NONE.also {
                    Log.w(TAG, "toUserList: invalid list code: $code; using ${UserList.NONE}")
                }

        @JvmStatic
        @TypeConverter
        fun fromUserList(userList: UserList) = userList.code
    }
}