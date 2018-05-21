package com.gmail.cristiandeives.switchhub

import android.support.v7.util.DiffUtil
import com.gmail.cristiandeives.switchhub.persistence.Game

internal class GamesDiffUtilCallback(private val oldData: List<Game>, private val newData: List<Game>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldData.size

    override fun getNewListSize() = newData.size

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int) = oldData[oldPosition].id == newData[newPosition].id

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        val old = oldData[oldPosition]
        val new = newData[newPosition]

        return old.id == new.id &&
                old.title == new.title &&
                old.price == new.price &&
                old.frontBoxArtUrl == new.frontBoxArtUrl &&
                old.userList == new.userList
    }

    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any {
        val old = oldData[oldPosition]
        val new = newData[newPosition]
        val changes = mutableListOf<String>()

        if (old.title != new.title) {
            changes += GamesAdapter.PAYLOAD_TITLE_CHANGED
        }

        if (old.price != new.price) {
            changes += GamesAdapter.PAYLOAD_PRICE_CHANGED
        }

        if (old.frontBoxArtUrl != new.frontBoxArtUrl) {
            changes += GamesAdapter.PAYLOAD_FRONTBOX_ART_CHANGED
        }

        if (old.userList != new.userList) {
            changes += GamesAdapter.PAYLOAD_USERLIST_CHANGED
        }

        return changes
    }
}