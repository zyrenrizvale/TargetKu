# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Retrofit interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep data models for Gson serialization
-keep class com.rizki.targetku.data.models.** { *; }
-keep class com.rizki.targetku.data.api.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Keep all ViewModels - CRITICAL: prevents R8 from obfuscating ViewModel classes
# which would break TargetKuViewModelFactory's isAssignableFrom checks
-keep class com.rizki.targetku.viewmodel.** { *; }

# Keep all Screens and Composable functions
-keep class com.rizki.targetku.ui.** { *; }

# Keep Application class and MainActivity
-keep class com.rizki.targetku.** { *; }

# Keep AndroidX Lifecycle ViewModel
-keep class androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.AndroidViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# Keep Compose Navigation
-keep class androidx.navigation.** { *; }

# Gson - needed for SharedPreferences deserialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
