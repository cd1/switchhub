package com.gmail.cristiandeives.switchhub

import android.content.Intent
import android.support.annotation.MainThread
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.gmail.cristiandeives.switchhub.persistence.Game
import com.gmail.cristiandeives.switchhub.persistence.Repository
import com.squareup.picasso.Picasso

@MainThread
internal open class GamesAdapter : RecyclerView.Adapter<GameViewHolder>(), View.OnClickListener {
    private var recyclerView: RecyclerView? = null

    var games = emptyList<Game>()
        set(value) {
            val gamesToBeDisplayed = value.filter(::shouldGameBeDisplayed)

            // TODO: move this out of the main thread
            DiffUtil
                .calculateDiff(GamesDiffUtilCallback(field, gamesToBeDisplayed))
                .dispatchUpdatesTo(this)

            field = gamesToBeDisplayed
        }

    override fun onAttachedToRecyclerView(attachedRecyclerView: RecyclerView) {
        Log.v(TAG, "> onAttachedToRecyclerView(attachedRecyclerView=$attachedRecyclerView)")

        recyclerView = attachedRecyclerView

        Log.v(TAG, "< onAttachedToRecyclerView(attachedRecyclerView=$attachedRecyclerView)")
    }

    override fun onDetachedFromRecyclerView(detachedRecyclerView: RecyclerView) {
        Log.v(TAG, "> onDetachedFromRecyclerView(detachedRecyclerView=$detachedRecyclerView)")

        recyclerView = null

        Log.v(TAG, "< onDetachedFromRecyclerView(detachedRecyclerView=$detachedRecyclerView)")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.game_viewholder, parent, false).apply {
            setOnClickListener(this@GamesAdapter)
        }

        view.findViewById<ImageButton>(R.id.wishlist_button).setOnClickListener(this)

        return GameViewHolder(view)
    }

    override fun getItemCount() = games.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int, payloads: List<Any>) {
        recyclerView?.let { rv ->
            val context = rv.context
            val game = games[position]
            val changes = payloads.flatMap { it as List<String> }.toSet()

            if (changes.isEmpty() || PAYLOAD_USERLIST_CHANGED in changes) {
                val imageDrawable = ContextCompat.getDrawable(context, game.userList.toDrawableId())
                holder.wishlist.setImageDrawable(imageDrawable)
            }

            if (changes.isEmpty() || PAYLOAD_TITLE_CHANGED in changes) {
                holder.title.text = game.title
            }

            if (changes.isEmpty() || PAYLOAD_PRICE_CHANGED in changes) {
                holder.price.text = game.price?.let { Game.US_CURRENCY_FORMAT.format(it) } ?: context.getString(R.string.price_unavailable)
            }

            if (changes.isEmpty() || PAYLOAD_FRONTBOX_ART_CHANGED in changes) {
                Picasso.with(context)
                    .load(game.frontBoxArtUrl)
                    .placeholder(R.drawable.ic_image_black)
                    .error(R.drawable.ic_broken_image_red)
                    .fit()
                    .centerCrop()
                    .into(holder.frontBoxArt)
            }
        } ?: Log.w(TAG, "unable to perform binding operation when attached RecyclerView == null")
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) = onBindViewHolder(holder, position, emptyList())

    override fun onClick(view: View) {
        Log.v(TAG, "> onClick(view=$view)")

        when (view.id) {
            R.id.wishlist_button -> toggleWishList(view)
            else -> startGameDetailsActivity(view)
        }

        Log.v(TAG, "< onClick(view=$view)")
    }

    open fun shouldGameBeDisplayed(game: Game) = game.userList != Game.UserList.HIDDEN

    private fun toggleWishList(view: View) {
        recyclerView?.let { rv ->
            val context = rv.context

            rv.getChildAdapterPosition(view.parent.parent as View).takeIf { it >= 0 }?.let { position ->
                val game = games[position]
                val newUserList = if (game.userList == Game.UserList.WISH) {
                    Game.UserList.NONE
                } else {
                    Game.UserList.WISH
                }

                val repo = Repository.getInstance(context)
                repo.updateGameUserList(game.id, newUserList)
            } ?: Log.d(TAG, "could not find clicked game on RecyclerView's adapter; ignoring click")
        } ?: Log.w(TAG, "unable to perform clicking operation when attached RecyclerView is null")
    }

    private fun startGameDetailsActivity(view: View) {
        recyclerView?.let { rv ->
            val context = rv.context

            rv.getChildAdapterPosition(view).takeIf { it >= 0 }?.let { position ->
                val intent = Intent(context, GameDetailsActivity::class.java).apply {
                    putExtra(GameDetailsFragment.EXTRA_GAME_ID, games[position].id)
                }
                context.startActivity(intent)
            } ?: Log.d(TAG, "could not find clicked game on RecyclerView's adapter; ignoring click")
        } ?: Log.w(TAG, "unable to perform clicking operation when attached RecyclerView is null")
    }

    private fun Game.UserList?.toDrawableId() = if (this == Game.UserList.WISH) {
        R.drawable.ic_wishlist_on
    } else {
        R.drawable.ic_wishlist_off
    }

    companion object {
        private val TAG = GamesAdapter::class.java.simpleName

        const val PAYLOAD_TITLE_CHANGED = "title"
        const val PAYLOAD_PRICE_CHANGED = "price"
        const val PAYLOAD_FRONTBOX_ART_CHANGED = "frontbox_art"
        const val PAYLOAD_USERLIST_CHANGED = "userlist"
    }
}
