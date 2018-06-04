package com.gmail.cristiandeives.switchhub

import android.content.Context
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_my_lists.*

// TODO: check why the options menu items blink when this fragment is loaded
internal class MyListsFragment : Fragment(), ViewPager.OnPageChangeListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_my_lists, container, false)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.v(TAG, "> onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")

        context?.let { ctx ->
            val adapter = MyListsPagerAdapter(ctx, childFragmentManager)
            view_pager.adapter = adapter
            tab_layout.setupWithViewPager(view_pager)

            val homeScreenExtra = arguments?.getString(EXTRA_HOME_SCREEN)
            Log.d(TAG, "$EXTRA_HOME_SCREEN = $homeScreenExtra")
            homeScreenExtra?.let { extra ->
                val currentItem = when (extra) {
                    EXTRA_VALUE_HOME_SCREEN_WISH_LIST -> 0
                    EXTRA_VALUE_HOME_SCREEN_MY_GAMES -> 1
                    EXTRA_VALUE_HOME_SCREEN_HIDDEN_GAMES -> 2
                    else -> throw IllegalArgumentException("unexpected home screen extra: $extra")
                }
                view_pager.currentItem = currentItem
            }

            view_pager.addOnPageChangeListener(this)
        } ?: Log.w(TAG, "cannot initialize tabs because context == null")

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        Log.v(TAG, "> onPageSelected(position=$position)")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context?.getSystemService(Context.SHORTCUT_SERVICE) as? ShortcutManager

            shortcutManager?.let { service ->
                val shortcutId = when (position) {
                    0 -> EXTRA_VALUE_HOME_SCREEN_WISH_LIST
                    1 -> EXTRA_VALUE_HOME_SCREEN_MY_GAMES
                    2 -> EXTRA_VALUE_HOME_SCREEN_HIDDEN_GAMES
                    else -> throw IllegalArgumentException("unexpected page position: $position")
                }
                service.reportShortcutUsed(shortcutId)
            }
        }

        Log.v(TAG, "< onPageSelected(position=$position)")
    }

    companion object {
        const val EXTRA_HOME_SCREEN = "com.gmail.cristiandeives.switchhub.HOME_SCREEN"
        const val EXTRA_VALUE_HOME_SCREEN_WISH_LIST = "wish_list"
        const val EXTRA_VALUE_HOME_SCREEN_MY_GAMES = "my_games"
        const val EXTRA_VALUE_HOME_SCREEN_HIDDEN_GAMES = "hidden_games"

        private val TAG = MyListsFragment::class.java.simpleName
    }
}