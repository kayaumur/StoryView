package com.xanir.stories.pagination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xanir.stories.databinding.FragmentStoriesBinding
import com.xanir.stories.models.Story
import com.xanir.stories.models.StoryGroup


/**
 * Created by Umur Kaya on 20-Sep-20.
 */
class StoriesFragment : Fragment() {

    lateinit var fragmentStoriesBinding: FragmentStoriesBinding

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
        val storyGroup = StoryGroup()
        val story = Story()
        story.storyUrl = "https://i.pinimg.com/564x/50/df/34/50df34b9e93f30269853b96b09c37e3b.jpg"
        story.isPicture = true
        story.isSeen = false
        story.isVideo = false
        storyGroup.story.add(story)
        storyGroup.story.add(story)
        storyGroup.story.add(story)
        storyGroup.story.add(story)
        storyGroup.story.add(story)
        storyGroup.story.add(story)
        storyGroup.story.add(story)
        val story2 = Story()
        story2.storyUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        story2.isPicture = false
        story2.isSeen = false
        story2.isVideo = true
        storyGroup.story.add(0,story2)
        storyGroup.story.add(0,story2)
        storyGroup.userId = 1
        storyGroup.userName = "Test"
        storyGroup.userProfilePicture = "https://media-exp1.licdn.com/dms/image/C5603AQF3AMeNui664g/profile-displayphoto-shrink_200_200/0?e=1605744000&v=beta&t=UuibqxVAwW6iYhbJ3_ncESK24r-nbERMdJaxqfIpZFI"
        fragmentStoriesBinding.storyView.loadStories(storyGroup)
    }
}