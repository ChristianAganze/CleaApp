package com.drcmind.cleaapp.ui.auth.onboarding

import androidx.annotation.DrawableRes
import com.drcmind.cleaapp.R

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val tag: String,
    @DrawableRes val imageRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        tag = "SUIVI & BIEN-ÊTRE",
        title = "Comprenez votre corps en toute clarté",
        subtitle = "Prévisions personnalisées & sérénité",
        description = "Suivez chaque phase de votre cycle avec des analyses précises et des rappels intuitifs pour vivre chaque jour en harmonie.",
        imageRes = R.drawable.onboarding_cycle_1787731535362
    ),
    OnboardingPage(
        tag = "SANTÉ & ANALYTIQUE",
        title = "Des conseils adaptés à votre rythme",
        subtitle = "Écoute bienveillante de vos symptômes",
        description = "Notez vos humeurs, sensations et flux. Obtenez des insights médicaux clairs pour mieux appréhender vos besoins.",
        imageRes = R.drawable.onboarding_insights_1787731548355
    ),
    OnboardingPage(
        tag = "INTIMITÉ & SÉCURITÉ",
        title = "Vos données intimes 100% protégées",
        subtitle = "Chiffrement et confidentialité totale",
        description = "Votre santé vous appartient. Vos données sont cryptées de bout en bout et conservées en toute sécurité.",
        imageRes = R.drawable.onboarding_privacy_1787731561268
    )
)

