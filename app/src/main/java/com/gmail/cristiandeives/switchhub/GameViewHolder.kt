package com.gmail.cristiandeives.switchhub

import android.support.annotation.MainThread
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

@MainThread
internal class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val frontBoxArt: ImageView = view.findViewById(R.id.front_box_art)
    val title: TextView = view.findViewById(R.id.title)
    val price: TextView = view.findViewById(R.id.price)
}
