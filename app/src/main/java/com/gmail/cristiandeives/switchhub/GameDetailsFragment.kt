package com.gmail.cristiandeives.switchhub

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.gmail.cristiandeives.switchhub.persistence.Game
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_game_details.*

@MainThread
internal class GameDetailsFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: GameDetailsViewModel
    private var cachedGame: Game? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_game_details, container, false)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        list.apply {
            setOnCreateContextMenuListener(this@GameDetailsFragment)
            setOnClickListener(this@GameDetailsFragment)
        }

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this)[GameDetailsViewModel::class.java].apply {
            gameId = arguments?.getString(EXTRA_GAME_ID)

            game?.observe(this@GameDetailsFragment, Observer { game ->
                Log.v(TAG, "> game#onChanged(t=$game)")

                game?.let { g ->
                    Picasso.with(context)
                        .load(g.frontBoxArtUrl)
                        .placeholder(R.drawable.ic_image_black)
                        .error(R.drawable.ic_broken_image_red)
                        .fit()
                        .centerCrop()
                        .into(front_box_art)
                    front_box_art.setOnClickListener(this@GameDetailsFragment)

                    title.text = g.title

                    g.price?.let { price.text = Game.US_CURRENCY_FORMAT.format(it) }

                    // TODO: add translation
                    release_date.text = g.releaseDateDisplay.takeIf { it.isNotEmpty() }
                            ?: g.releaseDate?.let { Game.DATE_FORMAT.format(it) }

                    // TODO: add translation
                    number_of_players.text = g.numberOfPlayers.capitalize()

                    // TODO: add translation
                    g.categories.takeIf { it.isNotEmpty() }
                        ?.let { categories.text = it.joinToString(", ") }

                    hidden_game_warning.visibility = if (g.userList == Game.UserList.HIDDEN) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    list.text = g.userList.toTextViewString()
                } ?: Log.w(TAG, "cannot find game data to be displayed")

                cachedGame = game

                Log.v(TAG, "< game#onChanged(t=$game)")
            })
        }

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        Log.v(TAG, "> onCreateContextMenu(menu=$menu, v=$v, menuInfo=$menuInfo)")

        activity?.menuInflater?.inflate(R.menu.game_lists, menu)

        menu.apply {
            setHeaderTitle(R.string.choose_list)

            cachedGame?.let { g ->
                val menuItemId = g.userList.toMenuItemId()
                findItem(menuItemId).isChecked = true
            } ?: Log.w(TAG, "unable to check current sorting criteria because there's no game data")
        }

        Log.v(TAG, "< onCreateContextMenu(menu=$menu, v=$v, menuInfo=$menuInfo)")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onContextItemSelected(item=$item)")

        var itemConsumed = true

        val newUserList = when (item.itemId) {
            R.id.wish_list_item -> Game.UserList.WISH
            R.id.my_games_list_item -> Game.UserList.OWNED
            R.id.hidden_games_list_item -> Game.UserList.HIDDEN
            R.id.no_list_item -> Game.UserList.NONE
            else -> {
                itemConsumed = super.onContextItemSelected(item)
                Game.UserList.NONE
            }
        }

        viewModel.setGameUserList(newUserList)
        list.text = newUserList.toTextViewString()

        Log.v(TAG, "< onContextItemSelected(item=$item): $itemConsumed")
        return itemConsumed
    }

    override fun onClick(v: View) {
        Log.v(TAG, "> onClick(v=$view)")

        when (v.id) {
            R.id.front_box_art -> displayFrontBoxArtInFullScreen()
            R.id.list -> list.showContextMenu()
            else -> Log.w(TAG, "unexpected clicked view: ${resources.getResourceEntryName(v.id)}")
        }

        Log.v(TAG, "< onClick(v=$view)")
    }

    private fun displayFrontBoxArtInFullScreen() {
        cachedGame?.let { g ->
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, FrontBoxArtFragment.newInstance(g.frontBoxArtUrl), FrontBoxArtFragment.FRAGMENT_TAG)
                ?.addToBackStack(null)
                ?.commit()
                    ?: Log.w(TAG, "unable to perform a Fragment transaction on the parent activity")
        } ?: Log.w(TAG, "unable to display front box art because there's no game data")
    }

    private fun Game.UserList?.toTextViewString(): CharSequence {
        val listStrId = when (this) {
            Game.UserList.WISH -> R.string.wish_list
            Game.UserList.OWNED -> R.string.my_games_list
            Game.UserList.HIDDEN -> R.string.hidden_games_list
            else -> R.string.no_list
        }
        val listStr = getString(listStrId)

        return SpannableString(listStr).apply {
            setSpan(UnderlineSpan(), 0, listStr.length, 0)
        }
    }

    private fun Game.UserList?.toMenuItemId() = when (this) {
        Game.UserList.WISH -> R.id.wish_list_item
        Game.UserList.OWNED -> R.id.my_games_list_item
        Game.UserList.HIDDEN -> R.id.hidden_games_list_item
        else -> R.id.no_list_item
    }

    companion object {
        private val TAG = GameDetailsFragment::class.java.simpleName

        const val EXTRA_GAME_ID = "game_id"

        fun newInstance(gameId: String) = GameDetailsFragment().apply {
            arguments = Bundle().apply { putString(EXTRA_GAME_ID, gameId) }
        }
    }
}