package com.example.fillerapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform