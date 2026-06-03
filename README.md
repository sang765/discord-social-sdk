# Discord Social SDK for Android

A lightweight Kotlin SDK for integrating Discord social features into Android apps.
Lighter and simpler than Discord's native C++ SDK — pure Kotlin with minimal dependencies.

## Features

- **REST API** — user, guild, channel, message, voice, relationships
- **Real-time Gateway** — WebSocket connection with automatic reconnection
- **OAuth2** — full authorization code flow for Android
- **Lightweight** — only 4 runtime dependencies (OkHttp, kotlinx-serialization, kotlinx-coroutines)
- **Kotlin-first** — coroutines, Flow, sealed classes, type-safe builders

## Installation

Add JitPack to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.sang765:discord-social-sdk:v1.0.0")
}
```

## Quick Start

```kotlin
// Bot token mode
val client = DiscordClient.Builder()
    .setToken("YOUR_BOT_TOKEN")
    .build()

// OAuth2 mode (user tokens)
val client = DiscordClient.Builder()
    .setClientId("YOUR_CLIENT_ID")
    .setClientSecret("YOUR_CLIENT_SECRET")
    .build()
```

## Usage

### REST API

```kotlin
// Get current user
val user = client.userApi.getCurrentUser()
user.onSuccess { println("Hello, ${it.displayName}!") }

// Send a message
val msg = client.channelApi.createMessage(
    channelId = "123456789".toSnowflake(),
    request = MessageCreateRequest(content = "Hello from SDK!")
)

// Get guild members
val members = client.guildApi.listGuildMembers("guild_id".toSnowflake())
```

### Gateway (Real-time Events)

```kotlin
client.connectGateway()
client.gatewayEvents.collect { event ->
    when (event) {
        is GatewayEvent.MessageCreate -> println("New message: ${event.message.content}")
        is GatewayEvent.PresenceUpdate -> println("${event.user.username} is $event.status")
        is GatewayEvent.TypingStart -> println("User typing...")
        else -> {}
    }
}
```

### OAuth2 Login

```kotlin
// 1. Open authorization page
val oauth2 = client.oauth2
val url = oauth2.getAuthorizationUrl("myapp://callback")
startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

// 2. Handle redirect in your Activity
override fun onNewIntent(intent: Intent) {
    val uri = intent.data ?: return
    val result = oauth2.parseRedirectUri(uri) ?: return
    lifecycleScope.launch {
        oauth2.exchangeCode(result.code, "myapp://callback")
            .onSuccess { token -> /* save token */ }
    }
}
```

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| OkHttp | 4.12.x | HTTP + WebSocket |
| kotlinx-serialization | 1.6.x | JSON parsing |
| kotlinx-coroutines | 1.8.x | Async operations |

## Distribution

Available via [JitPack](https://jitpack.io/#sang765/discord-social-sdk).

Repository: [github.com/sang765/discord-social-sdk](https://github.com/sang765/discord-social-sdk)

License: MIT
