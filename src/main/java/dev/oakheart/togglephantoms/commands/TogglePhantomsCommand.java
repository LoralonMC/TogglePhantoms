package dev.oakheart.togglephantoms.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.oakheart.togglephantoms.TogglePhantoms;
import dev.oakheart.togglephantoms.message.MessageManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class TogglePhantomsCommand {

    private final TogglePhantoms plugin;

    public TogglePhantomsCommand(TogglePhantoms plugin) {
        this.plugin = plugin;
    }

    private MessageManager messages() {
        return plugin.getMessageManager();
    }

    public void register() {
        LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(buildCommand(), "Toggle phantom spawning for yourself");
        });
    }

    private LiteralCommandNode<CommandSourceStack> buildCommand() {
        return Commands.literal("togglephantoms")
                .requires(src -> src.getSender() instanceof Player
                        && src.getSender().hasPermission("togglephantoms.use"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    boolean nowDisabled = plugin.togglePhantoms(player.getUniqueId());
                    if (nowDisabled) {
                        messages().sendPhantomsDisabled(player);
                    } else {
                        messages().sendPhantomsEnabled(player);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(src -> src.getSender().hasPermission("togglephantoms.reload"))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            try {
                                if (plugin.reloadPlugin()) {
                                    messages().sendReloadSuccess(sender);
                                } else {
                                    messages().sendReloadFailed(sender);
                                }
                            } catch (Exception e) {
                                messages().sendReloadFailed(sender);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("status")
                        .requires(src -> src.getSender() instanceof Player
                                && src.getSender().hasPermission("togglephantoms.use"))
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getSender();
                            if (plugin.arePhantomsDisabled(player.getUniqueId())) {
                                messages().sendStatusDisabled(player);
                            } else {
                                messages().sendStatusEnabled(player);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("admin")
                        .requires(src -> src.getSender().hasPermission("togglephantoms.admin"))
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .then(Commands.literal("on")
                                        .executes(ctx -> executeAdmin(ctx, "on")))
                                .then(Commands.literal("off")
                                        .executes(ctx -> executeAdmin(ctx, "off")))
                                .then(Commands.literal("toggle")
                                        .executes(ctx -> executeAdmin(ctx, "toggle")))
                                .then(Commands.literal("status")
                                        .executes(ctx -> executeAdmin(ctx, "status")))))
                .build();
    }

    private int executeAdmin(CommandContext<CommandSourceStack> ctx, String action) {
        CommandSender sender = ctx.getSource().getSender();
        PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);

        List<Player> players;
        try {
            players = resolver.resolve(ctx.getSource());
        } catch (Exception e) {
            messages().sendPlayerNotFound(sender, "unknown");
            return Command.SINGLE_SUCCESS;
        }

        if (players.isEmpty()) {
            messages().sendPlayerNotFound(sender, "unknown");
            return Command.SINGLE_SUCCESS;
        }

        Player target = players.getFirst();
        UUID targetUUID = target.getUniqueId();
        String targetName = target.getName();

        switch (action) {
            case "on" -> {
                plugin.setPhantomsDisabled(targetUUID, false);
                messages().sendAdminPhantomsEnabled(sender, targetName);
                messages().sendAdminNotifyEnabled(target);
            }
            case "off" -> {
                plugin.setPhantomsDisabled(targetUUID, true);
                messages().sendAdminPhantomsDisabled(sender, targetName);
                messages().sendAdminNotifyDisabled(target);
            }
            case "toggle" -> {
                boolean nowDisabled = plugin.togglePhantoms(targetUUID);
                if (nowDisabled) {
                    messages().sendAdminPhantomsDisabled(sender, targetName);
                    messages().sendAdminNotifyDisabled(target);
                } else {
                    messages().sendAdminPhantomsEnabled(sender, targetName);
                    messages().sendAdminNotifyEnabled(target);
                }
            }
            case "status" -> {
                if (plugin.arePhantomsDisabled(targetUUID)) {
                    messages().sendAdminStatusDisabled(sender, targetName);
                } else {
                    messages().sendAdminStatusEnabled(sender, targetName);
                }
            }
            default -> { }
        }

        return Command.SINGLE_SUCCESS;
    }
}
