package com.gmail.cristiandeives.switchhub

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log

internal class MyListsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        Log.v(TAG, "> getCount()")

        val count = 3

        Log.v(TAG, "< getCount(): $count")
        return count
    }

    override fun getItem(position: Int): Fragment {
        Log.v(TAG, "> getItem(position=$position)")

        val frag = when(position) {
            0 -> WishListFragment()
            1 -> MyGamesFragment()
            2 -> HiddenGamesFragment()
            else -> throw IllegalArgumentException("unexpected position: $position")
        }

        Log.v(TAG, "< getItem(position=$position): $frag")
        return frag
    }

    override fun getPageTitle(position: Int): CharSequence? {
        Log.v(TAG, "> getPageTitle(position=$position)")

        val titleId = when(position) {
            0 -> R.string.wish_list
            1 -> R.string.my_games_list
            2 -> R.string.hidden_games_list
            else -> throw IllegalArgumentException("unexpected position: $position")
        }
        val title = context.getString(titleId)

        Log.v(TAG, "< getPageTitle(position=$position): $title")
        return title
    }

    companion object {
        private val TAG = MyListsPagerAdapter::class.java.simpleName
    }
}