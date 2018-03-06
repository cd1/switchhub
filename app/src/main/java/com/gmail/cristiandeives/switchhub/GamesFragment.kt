package com.gmail.cristiandeives.switchhub

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_games.*

@MainThread
internal class GamesFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private lateinit var viewModel: MainViewModel
    private lateinit var gameLayoutManager: GridLayoutManager
    private val gameAdapter = GameAdapter()
    private val gameScrollListener = GameScrollListener()
    private val failedSnackbarCb = FailedSnackbarCallback()
    private var shouldScrollToTop = false
    private var isSnackbarShowing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_games, container, false)
        setHasOptionsMenu(true)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        val columnsCount = resources.getInteger(R.integer.game_view_columns)
        Log.d(TAG, "game grid will use $columnsCount columns")
        gameLayoutManager = GridLayoutManager(context, columnsCount)

        games_view.apply {
            setHasFixedSize(true)
            layoutManager = gameLayoutManager
            addOnScrollListener(gameScrollListener)
            adapter = gameAdapter
        }

        swipe_refresh.apply {
            setColorSchemeResources(R.color.joyconNeonRed, R.color.joyconNeonBlue)
            setOnRefreshListener(this@GamesFragment)
        }

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)

        activity?.let { parentActivity ->
            viewModel = ViewModelProviders.of(parentActivity)[MainViewModel::class.java]

            viewModel.games?.let { gameAdapter.games = it }

            var initialLoadingState: LoadingState? = viewModel.loadingState.value

            viewModel.loadingState.observe(this@GamesFragment, Observer { state ->
                Log.v(TAG, "> loadingState#onChanged(t=$state)")

                when (state) {
                    LoadingState.LOADED, LoadingState.LOADED_ALL -> {
                        viewModel.games?.let { games ->
                            if (shouldScrollToTop) {
                                gameLayoutManager.scrollToPosition(0)
                                shouldScrollToTop = false
                            }
                            if (!games.isEmpty()) {
                                gameAdapter.games = games
                                swipe_refresh.visibility = View.VISIBLE
                                no_games_layout.visibility = View.GONE
                            } else {
                                swipe_refresh.visibility = View.GONE
                                no_games_layout.visibility = View.VISIBLE
                            }
                        } ?: Log.wtf(TAG, "cannot update games because it is null; why did that happen when state = $state???")
                    }

                    LoadingState.FAILED -> {
                        if (initialLoadingState == LoadingState.FAILED) {
                            // don't display the snack because the error didn't happen now
                            return@Observer
                        }

                        Snackbar.make(coordinator_layout, R.string.loading_games_failed_message, Snackbar.LENGTH_LONG).apply {
                            setAction(R.string.try_again, this@GamesFragment)
                            addCallback(failedSnackbarCb)
                        }.show()
                        isSnackbarShowing = true
                    }

                    else -> Log.w(TAG, "unexpected state: $state")
                }

                swipe_refresh.isRefreshing = (state == LoadingState.LOADING)
                initialLoadingState = null

                Log.v(TAG, "< loadingState#onChanged(t=$state)")
            })
        } ?: Log.w(TAG, "Fragment doesn't have a parent Activity; cannot get ViewModel")

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.v(TAG, "> onCreateOptionsMenu(menu=$menu, inflater=$inflater)")

        inflater.inflate(R.menu.games_fragment, menu)

        Log.v(TAG, "< onCreateOptionsMenu(menu=$menu, inflater=$inflater): $menu")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onOptionsItemSelected(item=$item)")

        var itemConsumed = true

        when (item.itemId) {
            R.id.refresh -> {
                Log.i(TAG, "user requested to refresh game data via menu")
                viewModel.loadInitialGames()
                shouldScrollToTop = true
            }
            else -> {
                Log.wtf(TAG, "unexpected menu item: ${resources.getResourceEntryName(item.itemId)}")
                itemConsumed = false
            }
        }

        Log.v(TAG, "< onOptionsItemSelected(item=$item): $itemConsumed")
        return itemConsumed
    }

    override fun onRefresh() {
        Log.v(TAG, "> onRefresh()")

        Log.i(TAG, "user requested to refresh game data by swiping down from the top")
        viewModel.loadInitialGames()
        shouldScrollToTop = true

        Log.v(TAG, "< onRefresh()")
    }

    override fun onClick(v: View) {
        Log.v(TAG, "> onClick(v=$v)")

        viewModel.loadMoreGames()

        Log.v(TAG, "< onClick(v=$v)")
    }

    inner class GameScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val isUserScrollingDown = dy > 0
            val isViewJustLoaded = (dx == 0 && dy == 0)
            val isLastGameShown =
                (gameLayoutManager.findLastVisibleItemPosition() + 1) == gameLayoutManager.itemCount

            val isStateLoadedOrFailed = viewModel.loadingState.value.let { state ->
                state == LoadingState.LOADED || state == LoadingState.FAILED
            }
            if (!isSnackbarShowing && isStateLoadedOrFailed && (isUserScrollingDown || isViewJustLoaded) && isLastGameShown) {
                Log.i(TAG, "user [implicitly] requested to refresh game data by reaching the bottom")
                viewModel.loadMoreGames()
            }
        }
    }

    inner class FailedSnackbarCallback : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        private val logTag = FailedSnackbarCallback::class.java.simpleName

        override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
            Log.v(logTag, "> onDismissed(transientBottomBar: $transientBottomBar, event=$event)")
            isSnackbarShowing = false
            Log.v(logTag, "< onDismissed(transientBottomBar: $transientBottomBar, event=$event)")
        }
    }

    companion object {
        private val TAG = GamesFragment::class.java.simpleName
    }
}