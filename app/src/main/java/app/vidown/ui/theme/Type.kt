package app.vidown.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import app.vidown.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val InterFont = GoogleFont("Inter")

private val InterFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Bold),
)

private val Default = Typography()

val VidownTypography = Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = InterFamily),
    displayMedium = Default.displayMedium.copy(fontFamily = InterFamily),
    displaySmall = Default.displaySmall.copy(fontFamily = InterFamily),
    headlineLarge = Default.headlineLarge.copy(fontFamily = InterFamily),
    headlineMedium = Default.headlineMedium.copy(fontFamily = InterFamily),
    headlineSmall = Default.headlineSmall.copy(fontFamily = InterFamily),
    titleLarge = Default.titleLarge.copy(fontFamily = InterFamily),
    titleMedium = Default.titleMedium.copy(fontFamily = InterFamily),
    titleSmall = Default.titleSmall.copy(fontFamily = InterFamily),
    bodyLarge = Default.bodyLarge.copy(fontFamily = InterFamily),
    bodyMedium = Default.bodyMedium.copy(fontFamily = InterFamily),
    bodySmall = Default.bodySmall.copy(fontFamily = InterFamily),
    labelLarge = Default.labelLarge.copy(fontFamily = InterFamily),
    labelMedium = Default.labelMedium.copy(fontFamily = InterFamily),
    labelSmall = Default.labelSmall.copy(fontFamily = InterFamily),
)
