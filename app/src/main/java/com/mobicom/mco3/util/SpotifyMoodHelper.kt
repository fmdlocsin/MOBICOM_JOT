package com.mobicom.mco3.util

object SpotifyMoodHelper {

    private val moodPlaylists = mapOf(
        "Happy" to listOf(
            "https://open.spotify.com/playlist/05joCdh9j2iuVdZJlq6kTW",
            "https://open.spotify.com/playlist/6zSFAdKnh8Wh1JL3lo0kLv",
            "https://open.spotify.com/playlist/4kceNXHVOx0KF9Dm0N88is"
        ),
        "Sad" to listOf(
            "https://open.spotify.com/playlist/1OnplsjPqmqYRRzniz3gr9",
            "https://open.spotify.com/playlist/4D2PTCrwmFHyoZTmfoS85W",
            "https://open.spotify.com/playlist/1z6dpy3Twy9iOapEiJTEnc"
        ),
        "Angry" to listOf(
            "https://open.spotify.com/playlist/1z2797ssTFQX8iPgQkrrMC",
            "https://open.spotify.com/playlist/2voVtnSJo3HtHakP5GwguH",
            "https://open.spotify.com/playlist/6g8iIbGXVF3EPbCPiIsqzs"
        ),
        "Anxious" to listOf(
            "https://open.spotify.com/playlist/6H1Ko0YewQSX8qGityoEiT",
            "https://open.spotify.com/playlist/663aQVlsLjtCQRdUUlZVLm",
            "https://open.spotify.com/playlist/3VjzFQmfGYVWQ58R5054bA"
        ),
        "Relaxed" to listOf(
            "https://open.spotify.com/playlist/1REyNbQcJZeTNWelZQPEV9",
            "https://open.spotify.com/playlist/7f9K2gpJsg73o411xgoqMp",
            "https://open.spotify.com/playlist/4OM4EzM5FaajaxkRmCvKZ4"
        )
    )

    fun getRandomPlaylistUrl(mood: String): String? {
        return moodPlaylists[mood]?.random()
    }
}
