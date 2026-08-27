# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line numbers and source file names for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlinx Serialization & Data Transfer Objects (DTOs) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep all DTOs and Domain Models
-keep class com.drcmind.cleaapp.data.remote.dto.** { *; }
-keep class com.drcmind.cleaapp.data.model.** { *; }
-keep class com.drcmind.cleaapp.domain.model.** { *; }
-keep class com.drcmind.cleaapp.data.local.room.entity.** { *; }

-keepclasseswithmembers class com.drcmind.cleaapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.drcmind.cleaapp.**$$serializer { *; }
-keepclassmembers class com.drcmind.cleaapp.** {
    *** Companion;
}

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomOpenHelper
-keep class * extends androidx.room.RoomOpenDelegate

# --- Ktor Client & Engines ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.coroutines.** { *; }

# --- Koin Dependency Injection ---
-dontwarn org.koin.**
-keep class * extends org.koin.core.module.Module
-keep class com.drcmind.cleaapp.di.** { *; }

# --- AndroidX Core & Navigation ---
-dontwarn androidx.navigation.**

