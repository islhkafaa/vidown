# Add project specific ProGuard rules here.
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

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# youtubedl-android and ffmpeg-android rules
-keep class com.yausername.youtubedl_android.** { *; }
-keep interface com.yausername.youtubedl_android.** { *; }
-keep class com.yausername.ffmpeg.** { *; }
-keep interface com.yausername.ffmpeg.** { *; }

# JNI rules
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve annotations and inner classes for kotlinx.serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep data models used by kotlinx.serialization to prevent ExceptionInInitializerError
-keep class app.vidown.domain.models.** { *; }
-keepnames @kotlinx.serialization.Serializable class *
-keep class * { @kotlinx.serialization.SerialName <fields>; }

# Ignore missing classes from Jackson and Apache Commons Compress
-dontwarn java.beans.**
-dontwarn org.tukaani.xz.**

# Keep Apache Commons Compress classes (used by youtubedl-android to extract Python)
-keep class org.apache.commons.compress.** { *; }
