package com.gestiontienda.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.gestiontienda.Greeting
import com.gestiontienda.android.navigation.NavigationEvent
import com.gestiontienda.android.ui.components.SampleScreen
import com.gestiontienda.android.util.LocalToastContext
import com.gestiontienda.android.util.ToastUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestionTiendaTheme {
                CompositionLocalProvider(LocalToastContext provides LocalContext.current) {
                    var currentScreen by remember { mutableStateOf("Home") }

                    // Handle screen changes
                    fun navigateToScreen(newScreen: String) {
                        ToastUtil.handleNavigationEvent(
                            this,
                            NavigationEvent.ScreenChange(
                                from = currentScreen,
                                to = newScreen
                            )
                        )
                        currentScreen = newScreen
                    }

                    Surface(color = MaterialTheme.colorScheme.background) {
                        when (currentScreen) {
                            "Home" -> HomeScreen()
                            else -> SampleScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = Greeting().greet(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun GestionTiendaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = colorResource(R.color.md_theme_dark_primary),
            onPrimary = colorResource(R.color.md_theme_dark_onPrimary),
            primaryContainer = colorResource(R.color.md_theme_dark_primaryContainer),
            onPrimaryContainer = colorResource(R.color.md_theme_dark_onPrimaryContainer),
            secondary = colorResource(R.color.md_theme_dark_secondary),
            onSecondary = colorResource(R.color.md_theme_dark_onSecondary),
            secondaryContainer = colorResource(R.color.md_theme_dark_secondaryContainer),
            onSecondaryContainer = colorResource(R.color.md_theme_dark_onSecondaryContainer),
            tertiary = colorResource(R.color.md_theme_dark_tertiary),
            onTertiary = colorResource(R.color.md_theme_dark_onTertiary),
            tertiaryContainer = colorResource(R.color.md_theme_dark_tertiaryContainer),
            onTertiaryContainer = colorResource(R.color.md_theme_dark_onTertiaryContainer),
            error = colorResource(R.color.md_theme_dark_error),
            onError = colorResource(R.color.md_theme_dark_onError),
            errorContainer = colorResource(R.color.md_theme_dark_errorContainer),
            onErrorContainer = colorResource(R.color.md_theme_dark_onErrorContainer),
            background = colorResource(R.color.md_theme_dark_background),
            onBackground = colorResource(R.color.md_theme_dark_onBackground),
            surface = colorResource(R.color.md_theme_dark_surface),
            onSurface = colorResource(R.color.md_theme_dark_onSurface),
            surfaceVariant = colorResource(R.color.md_theme_dark_surfaceVariant),
            onSurfaceVariant = colorResource(R.color.md_theme_dark_onSurfaceVariant),
            inverseSurface = colorResource(R.color.md_theme_dark_inverseSurface),
            inverseOnSurface = colorResource(R.color.md_theme_dark_inverseOnSurface),
            inversePrimary = colorResource(R.color.md_theme_dark_inversePrimary),
            outline = colorResource(R.color.md_theme_dark_outline)
        )

        else -> lightColorScheme(
            primary = colorResource(R.color.md_theme_light_primary),
            onPrimary = colorResource(R.color.md_theme_light_onPrimary),
            primaryContainer = colorResource(R.color.md_theme_light_primaryContainer),
            onPrimaryContainer = colorResource(R.color.md_theme_light_onPrimaryContainer),
            secondary = colorResource(R.color.md_theme_light_secondary),
            onSecondary = colorResource(R.color.md_theme_light_onSecondary),
            secondaryContainer = colorResource(R.color.md_theme_light_secondaryContainer),
            onSecondaryContainer = colorResource(R.color.md_theme_light_onSecondaryContainer),
            tertiary = colorResource(R.color.md_theme_light_tertiary),
            onTertiary = colorResource(R.color.md_theme_light_onTertiary),
            tertiaryContainer = colorResource(R.color.md_theme_light_tertiaryContainer),
            onTertiaryContainer = colorResource(R.color.md_theme_light_onTertiaryContainer),
            error = colorResource(R.color.md_theme_light_error),
            onError = colorResource(R.color.md_theme_light_onError),
            errorContainer = colorResource(R.color.md_theme_light_errorContainer),
            onErrorContainer = colorResource(R.color.md_theme_light_onErrorContainer),
            background = colorResource(R.color.md_theme_light_background),
            onBackground = colorResource(R.color.md_theme_light_onBackground),
            surface = colorResource(R.color.md_theme_light_surface),
            onSurface = colorResource(R.color.md_theme_light_onSurface),
            surfaceVariant = colorResource(R.color.md_theme_light_surfaceVariant),
            onSurfaceVariant = colorResource(R.color.md_theme_light_onSurfaceVariant),
            inverseSurface = colorResource(R.color.md_theme_light_inverseSurface),
            inverseOnSurface = colorResource(R.color.md_theme_light_inverseOnSurface),
            inversePrimary = colorResource(R.color.md_theme_light_inversePrimary),
            outline = colorResource(R.color.md_theme_light_outline)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 