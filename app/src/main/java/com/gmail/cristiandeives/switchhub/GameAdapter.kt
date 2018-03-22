package com.gmail.cristiandeives.switchhub

import android.content.Intent
import android.support.annotation.MainThread
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso

@MainThread
internal class GameAdapter : RecyclerView.Adapter<GameViewHolder>(), View.OnClickListener {
    private lateinit var recyclerView: RecyclerView

    var games = emptyList<Game>()
        set(value) {
            if (value === field) {
                return
            }

            notifyDataSetChanged()
            field = value
        }

    override fun onAttachedToRecyclerView(attachedRecyclerView: RecyclerView) {
        recyclerView = attachedRecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.game_viewholder, parent, false).apply {
            setOnClickListener(this@GameAdapter)
        }

        return GameViewHolder(view)
    }

    override fun getItemCount() = games.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val context = recyclerView.context
        val g = games[position]

        holder.title.text = g.title
        holder.price.text = g.price?.let { Game.US_CURRENCY_FORMAT.format(it) } ?: context.getString(R.string.price_unavailable)

        Picasso.with(context)
            .load(g.frontBoxArtUrl)
            .placeholder(R.drawable.ic_image_black)
            .error(R.drawable.ic_broken_image_red)
            .fit()
            .centerCrop()
            .into(holder.frontBoxArt)
    }

    override fun onClick(view: View) {
        val context = recyclerView.context
        recyclerView.getChildAdapterPosition(view).takeIf { it >= 0 }?.let { position ->
            val intent = Intent(context, GameDetailsActivity::class.java).apply {
                putExtra(GameDetailsFragment.EXTRA_GAME, games[position])
            }
            context.startActivity(intent)
        } ?: Log.d(TAG, "could not find clicked Game on RecyclerView's adapter; ignoring click")
    }

    companion object {
        private val TAG = GameAdapter::class.java.simpleName
    }
}
