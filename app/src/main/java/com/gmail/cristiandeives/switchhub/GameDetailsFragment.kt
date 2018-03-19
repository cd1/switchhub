package com.gmail.cristiandeives.switchhub

import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_game_details.*

@MainThread
internal class GameDetailsFragment : Fragment(), View.OnClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_game_details, container, false)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        arguments?.getParcelable<Game>(EXTRA_GAME)?.let { game ->
            Picasso.with(context)
                .load(game.frontBoxArtUrl)
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_red)
                .fit()
                .centerCrop()
                .into(front_box_art)
            front_box_art.setOnClickListener(this)

            title.text = game.title

            game.price?.let { price.text = Game.US_CURRENCY_FORMAT.format(it) }

            // TODO: add translation
            release_date.text = game.releaseDateDisplay ?: game.releaseDate?.let { Game.DATE_FORMAT.format(it) }

            // TODO: add translation
            number_of_players.text = game.numberOfPlayers.capitalize()

            // TODO: add translation
            game.categories.takeIf { it.isNotEmpty() }?.let { categories.text = it.joinToString(", ") }
        } ?: Log.w(TAG, "could not find Game [key=$EXTRA_GAME] inside Fragment.arguments")

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onClick(v: View) {
        Log.v(TAG, "> onClick(v=$view)")

        displayFrontBoxArtInFullScreen()

        Log.v(TAG, "< onClick(v=$view)")
    }

    private fun displayFrontBoxArtInFullScreen() {
        arguments?.getParcelable<Game>(EXTRA_GAME)?.frontBoxArtUrl?.let { url ->
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, FrontBoxArtFragment.newInstance(url), FrontBoxArtFragment.FRAGMENT_TAG)
                ?.addToBackStack(null)
                ?.commit()
                    ?: Log.w(TAG, "unable to perform a Fragment transaction on the parent activity")
        } ?: Log.w(TAG, "could not find Game [key=$EXTRA_GAME] inside Fragment.arguments")
    }

    companion object {
        private val TAG = GameDetailsFragment::class.java.simpleName

        const val EXTRA_GAME = "game"

        internal fun newInstance(game: Game) = GameDetailsFragment().apply {
            arguments = Bundle().apply { putParcelable(EXTRA_GAME, game) }
        }
    }
}