# Stop Being An Idiot!

This plugin makes it so that if one player dies, all other players die as well.

Great plugin for content creation (YouTube, TikTok, Twitch, etc...) or just for a bit of fun with friends.

**🐛 Bugs / 💡 Suggestions:** Please [open an issue](https://github.com/srnyx/stop-being-an-idiot/issues/new/choose) to report a bug or suggest an idea

**🆘 Support:** Please [join the Discord](https://srnyx.com/discord) to get support

## Download

**✅ Stable:** You can download the latest **stable** version at [Modrinth](https://modrinth.com/plugin/sbai), [Hangar](https://hangar.papermc.io/srnyx/StopBeingAnIdiot), [Spigot](https://spigotmc.org/resources/107313), [Bukkit](https://dev.bukkit.org/projects/sbai), or [GitHub](https://github.com/srnyx/stop-being-an-idiot/releases)

**🚧 Snapshot:** You can download the latest **snapshot** version at [actions/workflows/build.yml](https://github.com/srnyx/stop-being-an-idiot/actions/workflows/build.yml)

## Fabric (1.21.1)

This repository now also includes a Fabric module for Minecraft `1.21.1`.

Build it with:

```shell
./gradlew :fabric:build
```

The built jar will be under `fabric/build/libs`.

### Fabric usage notes

- Commands: `/stopbeinganidiot` and `/sbai` (operator level 2 required)
- SBAI starts disabled on first run, enable with `/sbai on`
- Optional offline-join deaths (hardcore mode): `/sbai hardcore on|off|status`
- Add tag `sbai.no_trigger` to a player to stop their deaths from triggering a server-wide death
- Add tag `sbai.bypass` to a player to skip killing them when SBAI triggers

# Wiki

For all information about the plugin (commands, permissions, etc...) please see the wiki at [github.com/srnyx/stop-being-an-idiot/wiki](https://github.com/srnyx/stop-being-an-idiot/wiki)
