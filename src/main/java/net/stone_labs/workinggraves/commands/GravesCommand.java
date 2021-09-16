package net.stone_labs.workinggraves.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.GameModeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.OpCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.stone_labs.workinggraves.Grave;
import net.stone_labs.workinggraves.GraveHandler;
import net.stone_labs.workinggraves.GraveManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class GravesCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(literal("graves")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(literal("list")
                        .executes((context) -> list(context.getSource())))
                .then(literal("check")
                        .executes((context) -> check(context.getSource())))
                .then(literal("find")
                        .executes((context) -> find(context.getSource())))

        );
    }

    private static int list(ServerCommandSource source) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(source.getWorld());
        StringBuilder builder = new StringBuilder();

        builder.append("Listing graves in %s:".formatted(manager.getWorld().getRegistryKey().getValue().toString()));
        for (Grave grave : manager.getGraves())
            builder.append("\n - (%d, %d, %d)".formatted(grave.position().getX(), grave.position().getY(), grave.position().getZ()));

        source.sendFeedback(new LiteralText(builder.toString()), false);
        return 0;
    }

    private static int check(ServerCommandSource source) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(source.getWorld());
        StringBuilder builder = new StringBuilder();

        builder.append("Verifying grave validity in %s:".formatted(manager.getWorld().getRegistryKey().getValue().toString()));
        for (Grave grave : manager.getGraves())
        {
            String state = grave.isValid() ? "§2VALID§r" : "§4VALID§r";
            builder.append("\n - (%d, %d, %d): %s".formatted(grave.position().getX(), grave.position().getY(), grave.position().getZ(), state));
        }

        source.sendFeedback(new LiteralText(builder.toString()), false);
        return 0;
    }

    private static int find(ServerCommandSource source) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(source.getWorld());
        Grave grave = manager.findGrave(source.getPlayer());

        if (grave == null)
        {
            source.sendFeedback(new LiteralText("No valid grave found :/"), false);
            return 0;
        }

        source.sendFeedback(
                Text.Serializer.fromJson("[\"\",\"Next grave at \",{\"text\":\"[%s]\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp %d %d %d\"}}]"
                .formatted(grave.position().toShortString(), grave.position().getX(), grave.position().getY(), grave.position().getZ())), false);
        return 0;
    }
}
