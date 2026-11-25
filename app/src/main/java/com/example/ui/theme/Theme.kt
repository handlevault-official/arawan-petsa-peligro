package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

data class SurvivalPalette(
  val safeColor: Color,
  val safeContainer: Color,
  val onSafeContainer: Color,
  val tightColor: Color,
  val tightContainer: Color,
  val onTightContainer: Color,
  val criticalColor: Color,
  val criticalContainer: Color,
  val onCriticalContainer: Color,
  val kofiBg: Color,
  val kofiBorder: Color,
  val kofiAccent: Color,
)

private val LightSurvivalPalette = SurvivalPalette(
  safeColor = EmeraldPrimaryLight,
  safeContainer = EmeraldContainerLight,
  onSafeContainer = EmeraldOnContainerLight,
  tightColor = AmberSecondaryLight,
  tightContainer = AmberContainerLight,
  onTightContainer = AmberOnContainerLight,
  criticalColor = PeligroRedLight,
  criticalContainer = PeligroContainerLight,
  onCriticalContainer = PeligroOnContainerLight,
  kofiBg = KofiWarmBgLight,
  kofiBorder = KofiWarmBorderLight,
  kofiAccent = KofiAccentLight,
)

private val DarkSurvivalPalette = SurvivalPalette(
  safeColor = EmeraldPrimaryDark,
  safeContainer = EmeraldContainerDark,
  onSafeContainer = EmeraldOnContainerDark,
  tightColor = AmberSecondaryDark,
  tightContainer = AmberContainerDark,
  onTightContainer = AmberOnContainerDark,
  criticalColor = PeligroRedDark,
  criticalContainer = PeligroContainerDark,
  onCriticalContainer = PeligroOnContainerDark,
  kofiBg = KofiWarmBgDark,
  kofiBorder = KofiWarmBorderDark,
  kofiAccent = KofiAccentDark,
)

val LocalSurvivalPalette = staticCompositionLocalOf { LightSurvivalPalette }

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldPrimaryDark,
  onPrimary = EmeraldOnPrimaryDark,
  primaryContainer = EmeraldContainerDark,
  onPrimaryContainer = EmeraldOnContainerDark,
  secondary = AmberSecondaryDark,
  onSecondary = AmberOnSecondaryDark,
  secondaryContainer = AmberContainerDark,
  onSecondaryContainer = AmberOnContainerDark,
  error = PeligroRedDark,
  errorContainer = PeligroContainerDark,
  onErrorContainer = PeligroOnContainerDark,
  background = DarkBackground,
  onBackground = DarkOnSurface,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkOnSurfaceVariant,
  outline = DarkOutline,
  outlineVariant = DarkOutline,
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimaryLight,
  onPrimary = EmeraldOnPrimaryLight,
  primaryContainer = EmeraldContainerLight,
  onPrimaryContainer = EmeraldOnContainerLight,
  secondary = AmberSecondaryLight,
  onSecondary = AmberOnSecondaryLight,
  secondaryContainer = AmberContainerLight,
  onSecondaryContainer = AmberOnContainerLight,
  error = PeligroRedLight,
  errorContainer = PeligroContainerLight,
  onErrorContainer = PeligroOnContainerLight,
  background = LightBackground,
  onBackground = LightOnSurface,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightOnSurfaceVariant,
  outline = LightOutline,
  outlineVariant = LightOutline,
)

private val AppShapes = Shapes(
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun ArawanBudgetTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val survivalPalette = if (darkTheme) DarkSurvivalPalette else LightSurvivalPalette

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
    }
  }

  CompositionLocalProvider(LocalSurvivalPalette provides survivalPalette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      shapes = AppShapes,
      content = content,
    )
  }
}
