package com.gmail.cristiandeives.switchhub

import android.content.Intent
import android.support.annotation.MainThread
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.gmail.cristiandeives.switchhub.persistence.LocalGame
import com.gmail.cristiandeives.switchhub.persistence.Repository
import com.squareup.picasso.Picasso

@MainThread
internal class GameAdapter : RecyclerView.Adapter<GameViewHolder>(), View.OnClickListener {
    private var recyclerView: RecyclerView? = null

    var nintendoGames = emptyList<NintendoGame>()
        set(value) {
            val nonHiddenGames = value.filterNot { v -> localGames.find { v.id == it.id }?.userList == LocalGame.UserList.HIDDEN }
            if (nonHiddenGames === field) {
                return
            }

            field = nonHiddenGames

            notifyDataSetChanged()
        }

    var localGames = emptyList<LocalGame>()
        set(value) {
            if (value === field) {
                return
            }

            field = value

            val nonHiddenGames = nintendoGames.filterNot { game -> value.find { game.id == it.id }?.userList == LocalGame.UserList.HIDDEN }
            if (nonHiddenGames !== nintendoGames) {
                nintendoGames = nonHiddenGames
            }

            notifyDataSetChanged()
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
            setOnClickListener(this@GameAdapter)
        }

        view.findViewById<ImageButton>(R.id.wishlist_button).setOnClickListener(this)

        return GameViewHolder(view)
    }

    override fun getItemCount() = nintendoGames.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        recyclerView?.let { rv ->
            val context = rv.context
            val nGame = nintendoGames[position]
            val lGame = localGames.find { it.id == nGame.id }

            val imageDrawable = ContextCompat.getDrawable(context, lGame?.userList.toDrawableId())
            holder.wishlist.setImageDrawable(imageDrawable)

            holder.title.text = nGame.title
            holder.price.text = nGame.price?.let { NintendoGame.US_CURRENCY_FORMAT.format(it) } ?: context.getString(R.string.price_unavailable)

            Picasso.with(context)
                .load(nGame.frontBoxArtUrl)
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_red)
                .fit()
                .centerCrop()
                .into(holder.frontBoxArt)
        } ?: Log.w(TAG, "unable to perform binding operation when attached RecyclerView == null")
    }

    override fun onClick(view: View) {
        Log.v(TAG, "> onClick(view=$view)")

        when (view.id) {
            R.id.wishlist_button -> toggleWishList(view)
            else -> startGameDetailsActivity(view)
        }

        Log.v(TAG, "< onClick(view=$view)")
    }

    private fun toggleWishList(view: View) {
        recyclerView?.let { rv ->
            val context = rv.context

            rv.getChildAdapterPosition(view.parent.parent as View).takeIf { it >= 0 }?.let { position ->
                val id = nintendoGames[position].id
                val lGame = localGames.find { it.id == id }?.apply {
                    userList = if (userList == LocalGame.UserList.WISH) {
                        LocalGame.UserList.NONE
                    } else {
                        LocalGame.UserList.WISH
                    }
                } ?: LocalGame(nintendoGames[position].id, LocalGame.UserList.WISH)

                val repo = Repository.getInstance(context)
                repo.saveLocalGame(lGame, onSuccess = {
                    rv.findViewHolderForAdapterPosition(position)?.let { holder ->
                        val imageDrawable = ContextCompat.getDrawable(context, lGame.userList.toDrawableId())
                        (holder as GameViewHolder).wishlist.setImageDrawable(imageDrawable)
                    } ?: Log.d(TAG, "could not find ViewHolder for position $position; ignoring wishlist icon change")
                })
            } ?: Log.d(TAG, "could not find clicked NintendoGame on RecyclerView's adapter; ignoring click")
        } ?: Log.w(TAG, "unable to perform clicking operation when attached RecyclerView is null")
    }

    private fun startGameDetailsActivity(view: View) {
        recyclerView?.let { rv ->
            val context = rv.context

            rv.getChildAdapterPosition(view).takeIf { it >= 0 }?.let { position ->
                val intent = Intent(context, GameDetailsActivity::class.java).apply {
                    putExtra(GameDetailsFragment.EXTRA_GAME, nintendoGames[position])
                }
                context.startActivity(intent)
            } ?: Log.d(TAG, "could not find clicked NintendoGame on RecyclerView's adapter; ignoring click")
        } ?: Log.w(TAG, "unable to perform clicking operation when attached RecyclerView is null")
    }

    private fun LocalGame.UserList?.toDrawableId() = if (this == LocalGame.UserList.WISH) {
        R.drawable.ic_wishlist_on
    } else {
        R.drawable.ic_wishlist_off
    }

    companion object {
        private val TAG = GameAdapter::class.java.simpleName
    }
}
