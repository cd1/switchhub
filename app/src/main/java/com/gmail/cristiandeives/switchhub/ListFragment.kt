package com.gmail.cristiandeives.switchhub

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_list.*

// TODO: this class is very similar to GamesFragment...
internal abstract class ListFragment : Fragment() {
    // use the "dynamic" class name so the subclasses can print their names when logging
    private val logTag = javaClass.simpleName
    private lateinit var gameLayoutManager: GridLayoutManager
    private lateinit var viewModel: GamesViewModel
    private var shouldScrollToTop = false

    abstract val gamesAdapter: GamesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(logTag, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_list, container, false)

        Log.v(logTag, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(logTag, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        val columnsCount = resources.getInteger(R.integer.game_view_columns)
        Log.d(logTag, "game grid will use $columnsCount columns")
        gameLayoutManager = GridLayoutManager(context, columnsCount)

        games_view.apply {
            setHasFixedSize(true)
            layoutManager = gameLayoutManager
            adapter = gamesAdapter
        }

        no_games_suggestion.text = noGamesSuggestion()

        setHasOptionsMenu(true)

        Log.v(logTag, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v(logTag, "> onActivityCreated(savedInstanceState=$savedInstanceState)")
        super.onActivityCreated(savedInstanceState)

        activity?.let { parentActivity ->
            viewModel = ViewModelProviders.of(parentActivity)[GamesViewModel::class.java].apply {
                games.observe(this@ListFragment, Observer { gs ->
                    Log.v(logTag, "> games#onChanged(t[]=${gs?.size})")

                    gs?.let { gamesAdapter.games = it }

                    if (gamesAdapter.games.isNotEmpty()) {
                        no_games_layout.visibility = View.GONE
                        games_view.visibility = View.VISIBLE
                    } else {
                        games_view.visibility = View.GONE
                        no_games_layout.visibility = View.VISIBLE
                    }

                    Log.v(logTag, "< games#onChanged(t[]=${gs?.size})")
                })
            }
        }

        Log.v(logTag, "< onActivityCreated(savedInstanceState=$savedInstanceState)")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.v(logTag, "> onCreateOptionsMenu(menu=$menu, inflater=$inflater)")

        inflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.refresh).isVisible = false

        Log.v(logTag, "< onCreateOptionsMenu(menu=$menu, inflater=$inflater)")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(logTag, "> onOptionsItemSelected(item=$item)")

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
                Log.i(logTag, "user requested to refresh game data via menu")
                viewModel.loadGames()
                shouldScrollToTop = true
            }
            else -> {
                throw IllegalArgumentException("unexpected clicked options item: $item")
            }
        }

        val itemConsumed = true
        Log.v(logTag, "< onOptionsItemSelected(item=$item): $itemConsumed")
        return itemConsumed
    }

    open fun noGamesSuggestion() = ""

    private fun changeSortCriteria(sortCriteria: SortCriteria) {
        viewModel.sortCriteria = sortCriteria
        shouldScrollToTop = true
    }
}
