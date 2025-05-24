package com.gestiontienda.android.util

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.gestiontienda.android.navigation.NavigationEvent
import com.gestiontienda.android.navigation.ActionType

/**
 * Utility class for showing toasts with consistent styling
 */
object ToastUtil {
    private var toast: Toast? = null

    /**
     * Shows a short duration toast message
     */
    fun showShort(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_SHORT)
    }

    /**
     * Shows a long duration toast message
     */
    fun showLong(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_LONG)
    }

    /**
     * Handles navigation events and shows appropriate toasts
     */
    fun handleNavigationEvent(context: Context, event: NavigationEvent) {
        when (event) {
            is NavigationEvent.ScreenChange -> {
                showToast(
                    context,
                    "Navigating to ${event.to}",
                    Toast.LENGTH_SHORT
                )
            }

            is NavigationEvent.Action -> {
                val message = when (event.type) {
                    ActionType.CLICK -> "Clicked: ${event.description}"
                    ActionType.LONG_PRESS -> "Long pressed: ${event.description}"
                    ActionType.SWIPE -> "Swiped: ${event.description}"
                    ActionType.INPUT -> "Input: ${event.description}"
                    ActionType.SELECTION -> "Selected: ${event.description}"
                    ActionType.CONFIRMATION -> "Confirmed: ${event.description}"
                    ActionType.CANCELLATION -> "Cancelled: ${event.description}"
                    ActionType.ERROR -> "Error: ${event.description}"
                }
                showToast(context, message, Toast.LENGTH_SHORT)
            }
        }
    }

    private fun showToast(context: Context, message: String, duration: Int) {
        // Cancel any existing toast
        toast?.cancel()

        // Create and show new toast
        toast = Toast.makeText(context, message, duration).apply {
            show()
        }
    }
}

// Composable extension functions for easy toast access in Compose
@Composable
fun ShowToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val context = LocalToastContext.current
    ToastUtil.showToast(context, message, duration)
}

@Composable
fun HandleNavigationEvent(event: NavigationEvent) {
    val context = LocalToastContext.current
    ToastUtil.handleNavigationEvent(context, event)
}

// CompositionLocal for accessing Context in Composables
val LocalToastContext = staticCompositionLocalOf<Context> { error("No ToastContext provided") } 