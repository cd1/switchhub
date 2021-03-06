package com.gmail.cristiandeives.switchhub.persistence

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.gmail.cristiandeives.switchhub.persistence.AppDatabase.Companion.fromUserList
import com.gmail.cristiandeives.switchhub.persistence.AppDatabase.Companion.toUserList
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@Entity
internal data class Game(
    /* Data from Nintendo; we don't control it and don't want to backup it up */
    @PrimaryKey
    val id: String,
    val nsuid: String,
    val title: String,
    val releaseDate: Date?,
    val releaseDateDisplay: String,
    val price: BigDecimal?,
    val frontBoxArtUrl: Uri,
    val videoLink: Uri,
    val numberOfPlayers: String,
    val categories: List<String>,
    val buyItNow: Boolean,

    /* Data derived from Nintendo's; we don't want to back it up */
    val featuredIndex: Int,

    /* Personal data; generated by the user and we want to back it up */
    val userList: UserList = UserList.NONE
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDate(),
        parcel.readString(),
        parcel.readBigDecimal(),
        parcel.readUri(),
        parcel.readUri(),
        parcel.readString(),
        parcel.readArrayList(ClassLoader.getSystemClassLoader()) as List<String>,
        parcel.readBoolean(),

        parcel.readInt(),
        parcel.readUserList()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.run {
        writeString(id)
        writeString(nsuid)
        writeInt(featuredIndex)
        writeString(title)
        writeDate(releaseDate)
        writeString(releaseDateDisplay)
        writeBigDecimal(price)
        writeUri(frontBoxArtUrl)
        writeUri(videoLink)
        writeString(numberOfPlayers)
        writeList(categories)
        writeBoolean(buyItNow)

        writeString(id)
        writeUserList(userList)
    }

    override fun describeContents() = 0

    internal enum class UserList(val code: Int) {
        NONE(0),
        WISH(1),
        OWNED(2),
        HIDDEN(3),
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Game> {
            override fun createFromParcel(source: Parcel) = Game(source)

            override fun newArray(size: Int) = arrayOfNulls<Game>(size)
        }

        val US_CURRENCY_FORMAT: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        val DATE_FORMAT: DateFormat = DateFormat.getDateInstance()

        private fun Parcel.writeBigDecimal(value: BigDecimal?) = writeString(value?.toString())

        private fun Parcel.readBigDecimal() = readString()?.toBigDecimal()

        private fun Parcel.writeBoolean(value: Boolean) = writeInt(if (value) 1 else 0)

        private fun Parcel.readBoolean() = (readInt() != 0)

        private fun Parcel.writeDate(value: Date?) = writeLong(value?.time ?: -1)

        private fun Parcel.readDate() = readLong().let { if (it >= 0) Date(it) else null }

        private fun Parcel.writeUri(value: Uri?) = writeString(value.toString())

        private fun Parcel.readUri() = Uri.parse(readString())

        private fun Parcel.writeUserList(value: UserList) = writeInt(fromUserList(value))

        private fun Parcel.readUserList() = toUserList(readInt())
    }
}