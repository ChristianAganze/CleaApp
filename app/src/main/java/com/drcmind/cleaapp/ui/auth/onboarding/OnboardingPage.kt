package com.drcmind.cleaapp.ui.auth.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.drcmind.cleaapp.R

data class OnboardingPage(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val tagRes: Int,
    @DrawableRes val imageRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        tagRes = R.string.onboarding_tag_tracking,
        titleRes = R.string.onboarding_title_1,
        subtitleRes = R.string.onboarding_subtitle_1,
        descriptionRes = R.string.onboarding_desc_1,
        imageRes = R.drawable.onboarding_cycle_1787731535362
    ),
    OnboardingPage(
        tagRes = R.string.onboarding_tag_health,
        titleRes = R.string.onboarding_title_2,
        subtitleRes = R.string.onboarding_subtitle_2,
        descriptionRes = R.string.onboarding_desc_2,
        imageRes = R.drawable.onboarding_insights_1787731548355
    ),
    OnboardingPage(
        tagRes = R.string.onboarding_tag_privacy,
        titleRes = R.string.onboarding_title_3,
        subtitleRes = R.string.onboarding_subtitle_3,
        descriptionRes = R.string.onboarding_desc_3,
        imageRes = R.drawable.onboarding_privacy_1787731561268
    )
)


