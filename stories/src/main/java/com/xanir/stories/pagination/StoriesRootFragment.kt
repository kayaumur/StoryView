package com.xanir.stories.pagination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.xanir.stories.databinding.FragmentStoriesRootBinding
import com.xanir.stories.transformer.CubeOutTransformer


/**
 * Created by Umur Kaya on 20-Sep-20.
 */
class StoriesRootFragment : Fragment() {

    lateinit var fragmentStoriesBinding: FragmentStoriesRootBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentStoriesBinding = FragmentStoriesRootBinding.inflate(inflater,container,false)
        return fragmentStoriesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentStoriesBinding.storiesPager.adapter = StoriesPagerAdapter(this)
        fragmentStoriesBinding.storiesPager.setPageTransformer(CubeOutTransformer())
    }
}