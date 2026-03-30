package dev.oakheart.togglephantoms.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.oakheart.togglephantoms.TogglePhantoms;
import dev.oakheart.command.CommandRegistrar;
import dev.oakheart.message.MessageManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        CommandRegistrar.register(plugin, buildCommand(), "Toggle phantom spawning for yourself");
    }

    private LiteralCommandNode<CommandSourceStack> buildCommand() {
        return Commands.literal("togglephantoms")
                .requires(src -> src.getSender() instanceof Player
                        && src.getSender().hasPermission("togglephantoms.use"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    boolean nowDisabled = plugin.togglePhantoms(player.getUniqueId());
                    if (nowDisabled) {
                        messages().send(player, "phantoms-disabled");
                    } else {
                        messages().send(player, "phantoms-enabled");
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(src -> src.getSender().hasPermission("togglephantoms.reload"))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            try {
                                if (plugin.reloadPlugin()) {
                                    messages().sendCommand(sender, "reload-success");
                                } else {
                                    messages().sendCommand(sender, "reload-failed");
                                }
                            } catch (Exception e) {
                                messages().sendCommand(sender, "reload-failed");
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("status")
                        .requires(src -> src.getSender() instanceof Player
                                && src.getSender().hasPermission("togglephantoms.use"))
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getSender();
                            if (plugin.arePhantomsDisabled(player.getUniqueId())) {
                                messages().send(player, "status-disabled");
                            } else {
                                messages().send(player, "status-enabled");
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
            messages().send(sender, "player-not-found", Placeholder.unparsed("player", "unknown"));
            return Command.SINGLE_SUCCESS;
        }

        if (players.isEmpty()) {
            messages().send(sender, "player-not-found", Placeholder.unparsed("player", "unknown"));
            return Command.SINGLE_SUCCESS;
        }

        Player target = players.getFirst();
        UUID targetUUID = target.getUniqueId();
        String targetName = target.getName();

        switch (action) {
            case "on" -> {
                plugin.setPhantomsDisabled(targetUUID, false);
                messages().send(sender, "admin-phantoms-enabled", Placeholder.unparsed("player", targetName));
                messages().send(target, "admin-notify-enabled");
            }
            case "off" -> {
                plugin.setPhantomsDisabled(targetUUID, true);
                messages().send(sender, "admin-phantoms-disabled", Placeholder.unparsed("player", targetName));
                messages().send(target, "admin-notify-disabled");
            }
            case "toggle" -> {
                boolean nowDisabled = plugin.togglePhantoms(targetUUID);
                if (nowDisabled) {
                    messages().send(sender, "admin-phantoms-disabled", Placeholder.unparsed("player", targetName));
                    messages().send(target, "admin-notify-disabled");
                } else {
                    messages().send(sender, "admin-phantoms-enabled", Placeholder.unparsed("player", targetName));
                    messages().send(target, "admin-notify-enabled");
                }
            }
            case "status" -> {
                if (plugin.arePhantomsDisabled(targetUUID)) {
                    messages().send(sender, "admin-status-disabled", Placeholder.unparsed("player", targetName));
                } else {
                    messages().send(sender, "admin-status-enabled", Placeholder.unparsed("player", targetName));
                }
            }
            default -> { }
        }

        return Command.SINGLE_SUCCESS;
    }
}
