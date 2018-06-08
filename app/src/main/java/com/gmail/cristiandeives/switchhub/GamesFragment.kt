package com.gmail.cristiandeives.switchhub

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
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
    private lateinit var viewModel: GamesViewModel
    private lateinit var gameLayoutManager: GridLayoutManager
    private val gameAdapter = GamesAdapter()
    private var cachedState: LoadingState? = null
    private var shouldScrollToTop = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_games, container, false)

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
            adapter = gameAdapter
        }

        swipe_refresh.apply {
            setColorSchemeResources(R.color.joyconNeonRed, R.color.joyconNeonBlue)
            setOnRefreshListener(this@GamesFragment)
        }

        setHasOptionsMenu(true)

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(TAG, "> onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)

        activity?.let { parentActivity ->
            viewModel = ViewModelProviders.of(parentActivity)[GamesViewModel::class.java].apply {
                var initialLoadingState: LoadingState? = loadingState.value

                games.observe(this@GamesFragment, Observer { games ->
                    Log.v(TAG, "> games#onChanged(t[]=${games?.size})")

                    if (games?.isEmpty() == false) {
                        no_games_layout.visibility = View.GONE
                        swipe_refresh.visibility = View.VISIBLE

                        if (shouldScrollToTop) {
                            gameLayoutManager.scrollToPosition(0)
                            shouldScrollToTop = false
                        }

                        gameAdapter.games = games
                    } else if (cachedState == LoadingState.LOADED) {
                        // when cachedState == LOADED, we can be sure that the empty games are
                        // because they have actually been loaded and they're empty (as opposed to
                        // being the first observation of the LiveData, even without real data).
                        swipe_refresh.visibility = View.GONE
                        no_games_layout.visibility = View.VISIBLE
                    }

                    Log.v(TAG, "< games#onChanged(t[]=${games?.size})")
                })

                loadingState.observe(this@GamesFragment, Observer { state ->
                    Log.v(TAG, "> loadingState#onChanged(t=$state)")

                    cachedState = state

                    if (state == LoadingState.FAILED) {
                        if (initialLoadingState == LoadingState.FAILED) {
                            // don't display the snack because the error didn't happen now
                            return@Observer
                        }

                        Snackbar.make(coordinator_layout, R.string.loading_games_failed_message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.try_again, this@GamesFragment)
                            .show()
                    }

                    swipe_refresh.isRefreshing = (state == LoadingState.LOADING)
                    initialLoadingState = null

                    Log.v(TAG, "< loadingState#onChanged(t=$state)")
                })
            }
        } ?: Log.w(TAG, "Fragment doesn't have a parent Activity; cannot get ViewModel")

        Log.v(TAG, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onStart() {
        Log.v(TAG, "> onStart()")
        super.onStart()

        // check if some games should be displayed/hidden due to other settings being changed
        // (e.g. a shared preference) while the Fragment was out
        viewModel.games.value?.let { viewModelGames ->
            val oldCount = gameAdapter.games.size
            gameAdapter.games = viewModelGames
            val newCount = gameAdapter.games.size

            if (oldCount != newCount) {
                gameLayoutManager.scrollToPosition(0)
            }
        }

        Log.v(TAG, "< onStart()")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.v(TAG, "> onCreateOptionsMenu(menu=$menu, inflater=$inflater)")

        inflater.inflate(R.menu.main, menu)

        Log.v(TAG, "< onCreateOptionsMenu(menu=$menu, inflater=$inflater)")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "> onOptionsItemSelected(item=$item)")

        when (item.itemId) {
            R.id.sort -> {
                val subMenuId = when (viewModel.sortCriteria) {
                    SortCriteria.FEATURED -> R.id.sort_by_featured
                    SortCriteria.TITLE -> R.id.sort_by_title
                    SortCriteria.RELEASE_DATE -> R.id.sort_by_release_date
                    SortCriteria.LOWEST_PRICE -> R.id.sort_by_lowest_price
                    SortCriteria.HIGHEST_PRICE -> R.id.sort_by_highest_price
                }

                item.subMenu.findItem(subMenuId).isChecked = true
            }
            R.id.sort_by_featured -> changeSortCriteria(SortCriteria.FEATURED)
            R.id.sort_by_title -> changeSortCriteria(SortCriteria.TITLE)
            R.id.sort_by_release_date -> changeSortCriteria(SortCriteria.RELEASE_DATE)
            R.id.sort_by_lowest_price -> changeSortCriteria(SortCriteria.LOWEST_PRICE)
            R.id.sort_by_highest_price -> changeSortCriteria(SortCriteria.HIGHEST_PRICE)
            R.id.refresh -> {
                Log.i(TAG, "user requested to refresh game data via menu")
                viewModel.loadGames()
                shouldScrollToTop = true
            }
            R.id.settings -> {
                val intent = Intent(context, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> {
                throw IllegalArgumentException("unexpected clicked options item: $item")
            }
        }

        val itemConsumed = true
        Log.v(TAG, "< onOptionsItemSelected(item=$item): $itemConsumed")
        return itemConsumed
    }

    override fun onRefresh() {
        Log.v(TAG, "> onRefresh()")

        Log.i(TAG, "user requested to refresh game data by swiping down from the top")
        viewModel.loadGames()
        shouldScrollToTop = true

        Log.v(TAG, "< onRefresh()")
    }

    override fun onClick(v: View) {
        Log.v(TAG, "> onClick(v=$v)")

        viewModel.loadGames()

        Log.v(TAG, "< onClick(v=$v)")
    }

    private fun changeSortCriteria(sortCriteria: SortCriteria) {
        viewModel.sortCriteria = sortCriteria
        shouldScrollToTop = true
    }

    companion object {
        private val TAG = GamesFragment::class.java.simpleName
    }
}
