# Hilt specific rules
-keep public class * extends android.app.Service
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.view.View
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ViewComponentBuilder

# Room specific rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep entities for Room
-keepclassmembers class com.yourcompany.pumpmanager.feature.**.**Entity {
    <fields>;
    <methods>;
}

# Keep Compose related classes
-keep class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.ui.platform.**
