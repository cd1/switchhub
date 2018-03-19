package com.gmail.cristiandeives.switchhub

import android.net.Uri
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_front_box_art.*

@MainThread
internal class FrontBoxArtFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "> onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState)")

        val view = inflater.inflate(R.layout.fragment_front_box_art, container, false)

        Log.v(TAG, "< onCreateView(inflater=$inflater, container=$container, savedInstanceState=$savedInstanceState): $view")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getParcelable<Uri>(EXTRA_FRONT_BOX_ART_URL)?.let { url ->
            Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_red)
                .fit()
                .centerInside()
                .into(front_box_art)
        } ?: Log.w(TAG, "could not find front box art URL [key=$EXTRA_FRONT_BOX_ART_URL] inside Fragment.arguments")
    }

    companion object {
        private val TAG = FrontBoxArtFragment::class.java.simpleName

        internal const val EXTRA_FRONT_BOX_ART_URL = "front_box_art_url"
        internal val FRAGMENT_TAG = FrontBoxArtFragment::class.java.simpleName

        internal fun newInstance(frontBoxArtUrl: Uri) = FrontBoxArtFragment().apply {
            arguments = Bundle().apply { putParcelable(EXTRA_FRONT_BOX_ART_URL, frontBoxArtUrl) }
        }
    }
}