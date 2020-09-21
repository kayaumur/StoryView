package com.xanir.stories.pagination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.viewpager2.widget.ViewPager2
import com.xanir.stories.databinding.FragmentStoriesRootBinding
import com.xanir.stories.extensions.findCurrentFragment
import com.xanir.stories.extensions.findFragmentAtPosition
import com.xanir.stories.transformer.CubeOutTransformer
import kotlin.math.absoluteValue


/**
 * Created by Umur Kaya on 20-Sep-20.
 */
class StoriesRootFragment : Fragment() {

    lateinit var fragmentStoriesBinding: FragmentStoriesRootBinding
    private var lastPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentStoriesBinding = FragmentStoriesRootBinding.inflate(inflater,container,false)
        return fragmentStoriesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentStoriesBinding.storiesPager.adapter = StoriesPagerAdapter(this)
        fragmentStoriesBinding.storiesPager.setPageTransformer(CubeOutTransformer())
        fragmentStoriesBinding.storiesPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                if((lastPosition - position).absoluteValue == 1){
                    //If not found,then onViewCreated will work,else this will trigger resume/pause
                    fragmentStoriesBinding.storiesPager.findFragmentAtPosition(childFragmentManager,lastPosition)?.let {
                        (it as StoriesFragment).onPause()
                    }
                    fragmentStoriesBinding.storiesPager.findCurrentFragment(childFragmentManager)?.let {
                        (it as StoriesFragment).onStart()
                    }
                }
                lastPosition = position
            }
        })
    }
}