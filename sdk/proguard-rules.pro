-keep class com.discord.sdk.model.** { *; }
-keep class com.discord.sdk.api.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-dontwarn okhttp3.**
-dontwarn okio.**
