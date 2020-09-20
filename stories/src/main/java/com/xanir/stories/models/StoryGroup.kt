package com.xanir.stories.models


/**
 * Created by Umur Kaya on 19-Sep-20.
 */
data class StoryGroup(
    var isSeen: Boolean = false,
    var story: ArrayList<Story> = arrayListOf(),
    var userId: Int = 0,
    var userName: String = "",
    var userProfilePicture: String = ""
)