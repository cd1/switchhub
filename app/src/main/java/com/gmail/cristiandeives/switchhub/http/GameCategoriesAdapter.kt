package com.gmail.cristiandeives.switchhub.http

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader

/**
 * Converts the field "categories" from the Nintendo API to a list of string. The API returns a list
 * of string when the game has multiple categories but it returns a single string (not a list with
 * only one element, as expected) when the game has one category.
 */
internal class GameCategoriesAdapter {
    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<List<String>>) = when (reader.peek()) {
        JsonReader.Token.STRING -> listOf(reader.nextString())
        else -> delegate.fromJson(reader) ?: emptyList()
    }
}