package com.example.gestiontienda

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
