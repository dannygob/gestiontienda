package com.gestiontienda.android.navigation

sealed class NavigationEvent {
    data class ScreenChange(val from: String, val to: String) : NavigationEvent()
    data class Action(
        val description: String,
        val type: ActionType = ActionType.CLICK,
    ) : NavigationEvent()
}

enum class ActionType {
    CLICK,
    LONG_PRESS,
    SWIPE,
    INPUT,
    SELECTION,
    CONFIRMATION,
    CANCELLATION,
    ERROR
} 