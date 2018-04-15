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
import com.gmail.cristiandeives.switchhub.persistence.LocalGame
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_game_details.*

@MainThread
internal class GameDetailsFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: GameDetailsViewModel
    private var cachedLocalGame: LocalGame? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_game_details, container, false)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        arguments?.getParcelable<NintendoGame>(EXTRA_GAME)?.let { game ->
            Picasso.with(context)
                .load(game.frontBoxArtUrl)
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_red)
                .fit()
                .centerCrop()
                .into(front_box_art)
            front_box_art.setOnClickListener(this)

            title.text = game.title

            game.price?.let { price.text = NintendoGame.US_CURRENCY_FORMAT.format(it) }

            // TODO: add translation
            release_date.text = game.releaseDateDisplay ?: game.releaseDate?.let { NintendoGame.DATE_FORMAT.format(it) }

            // TODO: add translation
            number_of_players.text = game.numberOfPlayers.capitalize()

            // TODO: add translation
            game.categories.takeIf { it.isNotEmpty() }?.let { categories.text = it.joinToString(", ") }
        } ?: Log.w(TAG, "could not find NintendoGame [key=$EXTRA_GAME] inside Fragment.arguments")

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
            gameId = arguments?.getParcelable<NintendoGame>(EXTRA_GAME)?.id

            localGame?.observe(this@GameDetailsFragment, Observer { game ->
                Log.v(TAG, "> localGame#onChanged(t=$game)")

                cachedLocalGame = game
                list.text = game?.userList.toTextViewString()

                Log.v(TAG, "< localGame#onChanged(t=$game)")
            })
        }

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        Log.v(TAG, "> onCreateContextMenu(menu=$menu, v=$v, menuInfo=$menuInfo)")

        activity?.menuInflater?.inflate(R.menu.game_lists, menu)

        val menuItemId = cachedLocalGame?.userList.toMenuItemId()
        menu.apply {
            setHeaderTitle(R.string.choose_list)
            findItem(menuItemId).isChecked = true
        }

        Log.v(TAG, "< onCreateContextMenu(menu=$menu, v=$v, menuInfo=$menuInfo)")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onContextItemSelected(item=$item)")

        var itemConsumed = true

        val newUserList = when (item.itemId) {
            R.id.wish_list_item -> LocalGame.UserList.WISH
            R.id.no_list_item -> LocalGame.UserList.NONE
            else -> {
                itemConsumed = super.onContextItemSelected(item)
                LocalGame.UserList.NONE
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
        arguments?.getParcelable<NintendoGame>(EXTRA_GAME)?.frontBoxArtUrl?.let { url ->
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, FrontBoxArtFragment.newInstance(url), FrontBoxArtFragment.FRAGMENT_TAG)
                ?.addToBackStack(null)
                ?.commit()
                    ?: Log.w(TAG, "unable to perform a Fragment transaction on the parent activity")
        } ?: Log.w(TAG, "could not find NintendoGame [key=$EXTRA_GAME] inside Fragment.arguments")
    }

    private fun LocalGame.UserList?.toTextViewString(): CharSequence {
        val listStrId = when (this) {
            LocalGame.UserList.WISH -> R.string.wish_list
            else -> R.string.no_list
        }
        val listStr = getString(listStrId)

        return SpannableString(listStr).apply {
            setSpan(UnderlineSpan(), 0, listStr.length, 0)
        }
    }

    private fun LocalGame.UserList?.toMenuItemId() = when (this) {
        LocalGame.UserList.WISH -> R.id.wish_list_item
        else -> R.id.no_list_item
    }

    companion object {
        private val TAG = GameDetailsFragment::class.java.simpleName

        const val EXTRA_GAME = "game"

        fun newInstance(nintendoGame: NintendoGame) = GameDetailsFragment().apply {
            arguments = Bundle().apply { putParcelable(EXTRA_GAME, nintendoGame) }
        }
    }
}