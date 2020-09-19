package com.xanir.instastories

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xanir.instastories.databinding.ActivityMainBinding
import com.xanir.stories.models.Story
import com.xanir.stories.models.StoryGroup

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        storyGroup.userId = 1
        storyGroup.userName = "Test"
        storyGroup.userProfilePicture = "https://media-exp1.licdn.com/dms/image/C5603AQF3AMeNui664g/profile-displayphoto-shrink_200_200/0?e=1605744000&v=beta&t=UuibqxVAwW6iYhbJ3_ncESK24r-nbERMdJaxqfIpZFI"
        binding.container.loadStories(storyGroup)
    }
}