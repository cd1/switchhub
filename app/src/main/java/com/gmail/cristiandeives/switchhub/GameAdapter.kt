package com.gmail.cristiandeives.switchhub

import android.support.annotation.MainThread
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

@MainThread
internal class GameAdapter : RecyclerView.Adapter<GameViewHolder>() {
    var games = emptyList<Game>()
        set(value) {
            if (value === field) {
                return
            }

            if (value.size < field.size) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeInserted(field.size, value.size - field.size)
            }

            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.game_viewholder, parent, false)

        return GameViewHolder(view)
    }

    override fun getItemCount() = games.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val context = holder.frontBoxArt.context
        val g = games[position]

        holder.title.text = g.title
        holder.price.text = g.price?.let { US_CURRENCY_FORMAT.format(it) } ?: context.getString(R.string.price_unavailable)

        Picasso.with(context)
            .load(g.frontBoxArtUrl)
            .placeholder(R.drawable.ic_image_black)
            .error(R.drawable.ic_broken_image_red)
            .centerCrop()
            .resize(720, 720) // why 500???
            .into(holder.frontBoxArt)
    }

    companion object {
        private val US_CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US)
    }
}
