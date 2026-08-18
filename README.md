# ChatColor

Twitch-style colored nicknames for Paper chat. Every player gets a random color on their first join, the nickname is colored in chat, and the color is saved in a local SQLite database.

## Features

- Automatic random color on first join (Twitch-style palette), persisted to SQLite
- `<Name> message` chat format with the nickname colored and the message text plain
- Colored name in the tab list via the player's display name
- `/color` command to view, randomize or set a custom color
- Full hex color support (`/color #FF5733`)
- Join/quit messages are suppressed
- Colors survive restarts and plugin reloads (SQLite + in-memory cache)

## Requirements

- Paper 1.21+ (or a compatible fork such as Leaf)
- Java 21

## Installation

1. Drop `ChatColor-1.0.0.jar` into the `plugins/` folder of your server.
2. Restart the server (or load it at runtime with PlugManX).
3. Done. Players get their color on their first join.

If you use a plugin with its own chat system (e.g. CMI), disable it so it does not override the chat renderer:

```yaml
# plugins/CMI/Settings/Chat.yml
Chat:
  ModifyChatFormat:
    Enabled: false
    ClickHoverMessages: false
```

## Commands

| Command | Description |
| --- | --- |
| `/color` | Show your current color and usage |
| `/color random` | Get a random palette color (always different from the current one) |
| `/color <name>` | Set a palette color, e.g. `/color red` |
| `/color #RRGGBB` | Set a custom hex color, e.g. `/color #FF5733` |
| `/color list` | List the palette with hex values |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `chatcolor.color` | true | Allows changing your nickname color |

## Building

```
build.bat
```

The build runs in an ASCII-path copy (`C:\cfbuild`) because the project lives under a non-ASCII path on Windows, which breaks Gradle's worker daemon. Requires a JDK 21 at `C:\cfbuild\jdk` and Gradle 8.11 at `C:\cfbuild\gradle`. The resulting jar is copied back as `ChatColor-1.0.0.jar`.

## Storage

Colors are stored in `plugins/ChatColor/chatcolor.db` (SQLite), table `nick_colors`. Reads are served from an in-memory cache.