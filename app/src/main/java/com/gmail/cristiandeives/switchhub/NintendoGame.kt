package com.gmail.cristiandeives.switchhub

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

internal data class NintendoGame(
    val id: String,
    val nsuid: String? = null,
    val title: String,
    val releaseDate: Date?,
    val releaseDateDisplay: String?,
    val price: BigDecimal?,
    val frontBoxArtUrl: Uri,
    val videoLink: Uri?,
    val numberOfPlayers: String,
    val categories: List<String>,
    val buyItNow: Boolean
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
        parcel.readBoolean()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.run {
        writeString(id)
        writeString(nsuid)
        writeString(title)
        writeDate(releaseDate)
        writeString(releaseDateDisplay)
        writeBigDecimal(price)
        writeUri(frontBoxArtUrl)
        writeUri(videoLink)
        writeString(numberOfPlayers)
        writeList(categories)
        writeBoolean(buyItNow)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<NintendoGame> {
            override fun createFromParcel(source: Parcel) = NintendoGame(source)

            override fun newArray(size: Int) = arrayOfNulls<NintendoGame>(size)
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
    }
}
