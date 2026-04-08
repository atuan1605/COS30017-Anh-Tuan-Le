# Add project specific ProGuard rules here.

# Keep Supabase / Kotlinx serialization models
-keep class com.example.customproject.data.remote.** { *; }
-keepclassmembers class * { @kotlinx.serialization.SerialName *; }

# Keep Retrofit DTOs (Gson)
-keep class com.example.customproject.data.remote.tmdb.dto.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile