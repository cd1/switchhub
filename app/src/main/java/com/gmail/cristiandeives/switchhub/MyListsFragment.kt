package com.gmail.cristiandeives.switchhub

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_my_lists.*

// TODO: check why the options menu items blink when this fragment is loaded
internal class MyListsFragment : Fragment() {
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
        } ?: Log.w(TAG, "cannot initialize tabs because context == null")

        Log.v(TAG, "< onViewCreated(view=$view, savedInstanceState=$savedInstanceState)")
    }

    companion object {
        private val TAG = MyListsFragment::class.java.simpleName
    }
}