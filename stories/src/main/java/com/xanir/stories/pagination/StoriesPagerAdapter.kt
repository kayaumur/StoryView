package com.xanir.stories.pagination

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.xanir.stories.models.Story
import com.xanir.stories.models.StoryGroup


/**
 * Created by Umur Kaya on 20-Sep-20.
 */
class StoriesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): StoriesFragment {
        return StoriesFragment().apply { arguments?.putInt("position",position) }
    }
}