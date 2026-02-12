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
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
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

    private void send(CommandSender sender, Optional<Component> message) {
        message.ifPresent(sender::sendMessage);
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
                // Base command - toggle for self
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if (!(sender instanceof Player player)) {
                        send(sender, messages().playerOnly());
                        return Command.SINGLE_SUCCESS;
                    }

                    if (!player.hasPermission("togglephantoms.use")) {
                        send(player, messages().noPermission());
                        return Command.SINGLE_SUCCESS;
                    }

                    boolean nowDisabled = plugin.togglePhantoms(player.getUniqueId());
                    if (nowDisabled) {
                        send(player, messages().phantomsDisabled());
                    } else {
                        send(player, messages().phantomsEnabled());
                    }
                    return Command.SINGLE_SUCCESS;
                })
                // Reload subcommand
                .then(Commands.literal("reload")
                        .requires(src -> src.getSender().hasPermission("togglephantoms.reload"))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (plugin.reloadPlugin()) {
                                send(sender, messages().reloadSuccess());
                            } else {
                                send(sender, messages().reloadFailed());
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                // Status subcommand
                .then(Commands.literal("status")
                        .requires(src -> src.getSender().hasPermission("togglephantoms.use"))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            if (!(sender instanceof Player player)) {
                                send(sender, messages().playerOnly());
                                return Command.SINGLE_SUCCESS;
                            }

                            boolean disabled = plugin.arePhantomsDisabled(player.getUniqueId());
                            if (disabled) {
                                send(player, messages().statusDisabled());
                            } else {
                                send(player, messages().statusEnabled());
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                // Admin subcommand
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
            send(sender, messages().playerNotFound("unknown"));
            return Command.SINGLE_SUCCESS;
        }

        if (players.isEmpty()) {
            send(sender, messages().playerNotFound("unknown"));
            return Command.SINGLE_SUCCESS;
        }

        Player target = players.getFirst();
        return handleAdmin(sender, target, action);
    }

    private int handleAdmin(CommandSender sender, Player target, String action) {
        UUID targetUUID = target.getUniqueId();
        String targetName = target.getName();

        switch (action) {
            case "on":
                plugin.setPhantomsDisabled(targetUUID, false);
                send(sender, messages().adminPhantomsEnabled(targetName));
                messages().adminNotifyEnabled().ifPresent(target::sendMessage);
                break;

            case "off":
                plugin.setPhantomsDisabled(targetUUID, true);
                send(sender, messages().adminPhantomsDisabled(targetName));
                messages().adminNotifyDisabled().ifPresent(target::sendMessage);
                break;

            case "toggle":
                boolean nowDisabled = plugin.togglePhantoms(targetUUID);
                if (nowDisabled) {
                    send(sender, messages().adminPhantomsDisabled(targetName));
                    messages().adminNotifyDisabled().ifPresent(target::sendMessage);
                } else {
                    send(sender, messages().adminPhantomsEnabled(targetName));
                    messages().adminNotifyEnabled().ifPresent(target::sendMessage);
                }
                break;

            case "status":
                boolean isDisabled = plugin.arePhantomsDisabled(targetUUID);
                if (isDisabled) {
                    send(sender, messages().adminStatusDisabled(targetName));
                } else {
                    send(sender, messages().adminStatusEnabled(targetName));
                }
                break;
        }

        return Command.SINGLE_SUCCESS;
    }
}
