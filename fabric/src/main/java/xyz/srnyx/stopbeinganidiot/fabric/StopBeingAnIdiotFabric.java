package xyz.srnyx.stopbeinganidiot.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;


public class StopBeingAnIdiotFabric implements ModInitializer {
    public static final String MOD_ID = "sbai";

    private static final Logger LOGGER = LoggerFactory.getLogger("StopBeingAnIdiot");

    private SbaiState state;
    private boolean justDied;

    @Override
    public void onInitialize() {
        state = SbaiState.load(LOGGER);
        registerCommands();
        registerDeathListener();
        registerJoinListener();
        LOGGER.info("Loaded StopBeingAnIdiot for Fabric");
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            final LiteralCommandNode<ServerCommandSource> root = registerRootCommand(dispatcher);
            dispatcher.register(literal("sbai").redirect(root));
        });
    }

    private LiteralCommandNode<ServerCommandSource> registerRootCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        return dispatcher.register(literal("stopbeinganidiot")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> toggle(context.getSource()))
                .then(literal("on").executes(context -> setEnabled(context.getSource(), true)))
                .then(literal("off").executes(context -> setEnabled(context.getSource(), false)))
            .then(literal("hardcore")
                .executes(context -> hardcoreStatus(context.getSource()))
                .then(literal("on").executes(context -> setHardcore(context.getSource(), true)))
                .then(literal("off").executes(context -> setHardcore(context.getSource(), false)))
                .then(literal("status").executes(context -> hardcoreStatus(context.getSource()))))
                .then(literal("reload").executes(context -> reload(context.getSource())))
                .then(literal("status").executes(context -> status(context.getSource()))));
    }

    private int toggle(ServerCommandSource source) {
        return setEnabled(source, !state.enabled);
    }

    private int setEnabled(ServerCommandSource source, boolean enabled) {
        state.enabled = enabled;
        state.save(LOGGER);
        source.sendFeedback(() -> Text.literal("StopBeingAnIdiot is now " + (enabled ? "enabled" : "disabled") + "."), true);
        return 1;
    }

    private int setHardcore(ServerCommandSource source, boolean enabled) {
        state.hardcoreOfflineDeaths = enabled;
        state.save(LOGGER);
        source.sendFeedback(() -> Text.literal("Offline join deaths are now " + (enabled ? "enabled" : "disabled") + "."), true);
        return 1;
    }

    private int hardcoreStatus(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Offline join deaths are currently " + (state.hardcoreOfflineDeaths ? "enabled" : "disabled") + "."), false);
        return 1;
    }

    private int reload(ServerCommandSource source) {
        state = SbaiState.load(LOGGER);
        source.sendFeedback(() -> Text.literal("Reloaded StopBeingAnIdiot state from config."), false);
        return 1;
    }

    private int status(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("StopBeingAnIdiot is currently " + (state.enabled ? "enabled" : "disabled") + ". Offline join deaths are " + (state.hardcoreOfflineDeaths ? "enabled" : "disabled") + "."), false);
        return 1;
    }

    private void registerJoinListener() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final ServerPlayerEntity player = handler.player;
            final String uuid = player.getUuidAsString();

            // New players are marked at the current wave so they don't get retroactively punished.
            if (!state.playerWave.containsKey(uuid)) {
                state.playerWave.put(uuid, state.deathWave);
                state.save(LOGGER);
                return;
            }

            if (!state.hardcoreOfflineDeaths) return;

            final long playerWave = state.playerWave.getOrDefault(uuid, 0L);
            if (playerWave >= state.deathWave) return;

            state.playerWave.put(uuid, state.deathWave);
            state.save(LOGGER);

            if (player.isCreative() || player.isSpectator()) return;
            if (player.getCommandTags().contains("sbai.bypass")) return;
            player.damage(player.getDamageSources().generic(), Float.MAX_VALUE);
        });
    }

    private void registerDeathListener() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof final ServerPlayerEntity deadPlayer)) return;
            if (!state.enabled || justDied) return;
            if (deadPlayer.getCommandTags().contains("sbai.no_trigger")) return;

            final MinecraftServer server = deadPlayer.getServer();
            if (server == null) return;

            final long currentWave = ++state.deathWave;
            state.playerWave.put(deadPlayer.getUuidAsString(), currentWave);

            justDied = true;
            try {
                for (final ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
                    state.playerWave.put(online.getUuidAsString(), currentWave);
                    if (online.equals(deadPlayer)) continue;
                    if (online.isCreative() || online.isSpectator()) continue;
                    if (online.getCommandTags().contains("sbai.bypass")) continue;
                    online.damage(deadPlayer.getDamageSources().generic(), Float.MAX_VALUE);
                }
            } finally {
                justDied = false;
            }

            state.save(LOGGER);

            final String deadName = deadPlayer.getName().getString();
            server.getPlayerManager().broadcast(Text.literal(deadName + " just made everyone die!"), false);
            for (final ServerPlayerEntity online : server.getPlayerManager().getPlayerList()) {
                online.sendMessage(Text.literal(deadName + " just died!"), true);
            }
        });
    }
}
