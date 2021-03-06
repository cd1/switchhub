package com.gmail.cristiandeives.switchhub

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_loading_games.*

@MainThread
internal class LoadingGamesFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: GamesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_loading_games, container, false)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        try_again_button.setOnClickListener(this)

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)

        activity?.let { parentActivity ->
            viewModel = ViewModelProviders.of(parentActivity)[GamesViewModel::class.java].apply {
                games.observe(this@LoadingGamesFragment, Observer { gs ->
                    Log.v(TAG, "> games#onChanged(t[]=${gs?.size})")

                    if (loadingState.value == LoadingState.NOT_LOADED &&
                            (gs?.isEmpty() != false || gs.all { it.title.isEmpty() })) {
                        loadGames()
                    }

                    Log.v(TAG, "< games#onChanged(t[]=${gs?.size})")
                })

                loadingState.observe(this@LoadingGamesFragment, Observer { state ->
                    Log.v(TAG, "> loadingState#onChanged(t=$state)")

                    if (state == LoadingState.FAILED) {
                        loading_message_view.visibility = View.GONE
                        failed_to_load_view.visibility = View.VISIBLE
                    }


                    Log.v(TAG, "< loadingState#onChanged(t=$state)")
                })
            }
        } ?: Log.w(TAG, "Fragment doesn't have a parent Activity; cannot get ViewModel")

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onClick(v: View) {
        Log.v(TAG, "> onClick(v=$v)")

        when (v.id) {
            R.id.try_again_button -> {
                failed_to_load_view.visibility = View.GONE
                loading_message_view.visibility = View.VISIBLE
                viewModel.loadGames()
            }

            else -> throw IllegalArgumentException("unexpected clicked view: $v")
        }

        Log.v(TAG, "< onClick(v=$v)")
    }

    companion object {
        private val TAG = LoadingGamesFragment::class.java.simpleName
    }
}
