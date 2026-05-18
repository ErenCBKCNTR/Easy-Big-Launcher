# Timber
-dontwarn timber.log.**
-keep class timber.log.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase$Callback { *; }

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Keep entity classes
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
