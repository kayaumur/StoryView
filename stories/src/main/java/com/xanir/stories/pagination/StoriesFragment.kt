package com.xanir.stories.pagination

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xanir.stories.databinding.FragmentStoriesBinding
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Umur Kaya on 20-Sep-20.
 */

class StoriesFragment : Fragment() {

    lateinit var fragmentStoriesBinding: FragmentStoriesBinding
    private lateinit var storiesFragmentViewModel: StoriesFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentStoriesBinding = FragmentStoriesBinding.inflate(inflater,container,false)
        return fragmentStoriesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storiesFragmentViewModel = ViewModelProvider(this).get(StoriesFragmentViewModel::class.java)
        //Do not use live data from preventing next fragment's exoplayer start
        //Or revert to ViewPager old for making offset 0 in ViewPager
        fragmentStoriesBinding.storyView.loadStories(storiesFragmentViewModel.prepareStories())
    }

    override fun onStart() {
        super.onStart()
        storiesFragmentViewModel.lastSavedStory?.let {
            fragmentStoriesBinding.storyView.loadStories(it)
        }
    }

    override fun onPause() {
        super.onPause()
        fragmentStoriesBinding.storyView.stories?.let {
            storiesFragmentViewModel.lastSavedStory = it
        }
    }
}