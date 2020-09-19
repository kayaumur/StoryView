package com.xanir.stories.models


/**
 * Created by Umur Kaya on 19-Sep-20.
 */
data class Story(
    var storyUrl: String = "",
    var isSeen: Boolean = false,
    var isVideo: Boolean = false,
    var isPicture: Boolean = false
)