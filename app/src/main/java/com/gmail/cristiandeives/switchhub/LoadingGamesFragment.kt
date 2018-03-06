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
class LoadingGamesFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_loading_games, container, false)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try_again_button.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onActivityCreated(savedInstanceState=$savedInstanceState)")

        super.onActivityCreated(savedInstanceState)

        activity?.let { parentActivity ->
            viewModel = ViewModelProviders.of(parentActivity)[MainViewModel::class.java]

            viewModel.loadingState.observe(this, Observer { state ->
                when (state) {
                    LoadingState.NOT_LOADED, LoadingState.LOADING -> viewModel.loadInitialGames()

                    LoadingState.FAILED -> {
                        loading_message_view.visibility = View.GONE
                        failed_to_load_view.visibility = View.VISIBLE
                    }

                    else -> Log.w(TAG, "unexpected state: $state")
                }
            })
        } ?: Log.w(TAG, "Fragment doesn't have a parent Activity; cannot get ViewModel")

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onClick(v: View) {
        Log.v(TAG, "> onClick(v=$v)")

        failed_to_load_view.visibility = View.GONE
        loading_message_view.visibility = View.VISIBLE
        viewModel.loadInitialGames()

        Log.v(TAG, "< onClick(v=$v)")
    }

    companion object {
        private val TAG = LoadingGamesFragment::class.java.simpleName
    }
}
