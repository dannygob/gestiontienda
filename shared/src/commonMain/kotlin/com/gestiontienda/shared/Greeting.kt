package com.gestiontienda.shared

expect fun getPlatform(): Platform

class Greeting {
    fun greet(): String {
        return "Hello from ${getPlatform().name}!"
    }
} 
