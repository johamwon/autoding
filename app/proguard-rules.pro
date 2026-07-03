# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.* class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class **_Impl
-keep class com.aotuding.ding.data.db.** { *; }

# Keep all services and receivers (important for manifest declared components)
-keep class com.aotuding.ding.service.** { *; }
-keep class com.aotuding.ding.receiver.** { *; }

# Keep Application
-keep class com.aotuding.ding.AotuDingApplication { *; }

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding { *; }

# Prevent obfuscation of model classes used in Room
-keep class com.aotuding.ding.data.db.TaskEntity { *; }
-keep class com.aotuding.ding.core.model.** { *; }