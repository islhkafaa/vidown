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

private val OutfitFont = GoogleFont("Outfit")

private val OutfitFamily = FontFamily(
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Bold),
)

private val InterFont = GoogleFont("Inter")

private val InterFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold),
)

private val Default = Typography()

val VidownTypography = Typography(
    displayLarge = Default.displayLarge.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = Default.displayMedium.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = Default.displaySmall.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineLarge = Default.headlineLarge.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = Default.headlineMedium.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = Default.headlineSmall.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = Default.titleLarge.copy(fontFamily = OutfitFamily, fontWeight = FontWeight.Medium),
    titleMedium = Default.titleMedium.copy(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = Default.titleSmall.copy(fontFamily = InterFamily, fontWeight = FontWeight.Medium),
    bodyLarge = Default.bodyLarge.copy(fontFamily = InterFamily),
    bodyMedium = Default.bodyMedium.copy(fontFamily = InterFamily),
    bodySmall = Default.bodySmall.copy(fontFamily = InterFamily),
    labelLarge = Default.labelLarge.copy(fontFamily = InterFamily, fontWeight = FontWeight.Medium),
    labelMedium = Default.labelMedium.copy(fontFamily = InterFamily),
    labelSmall = Default.labelSmall.copy(fontFamily = InterFamily),
)
