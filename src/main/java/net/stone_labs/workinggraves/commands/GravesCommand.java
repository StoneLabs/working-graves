package net.stone_labs.workinggraves.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.stone_labs.workinggraves.Grave;
import net.stone_labs.workinggraves.GraveHandler;
import net.stone_labs.workinggraves.GraveManager;

import java.util.*;

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
                        .then(argument("world", DimensionArgumentType.dimension())
                                .then(argument("page", IntegerArgumentType.integer(0))
                                        .executes((context) -> list(context.getSource(),
                                                DimensionArgumentType.getDimensionArgument(context, "world"),
                                                IntegerArgumentType.getInteger(context, "page"))))
                                .executes((context) -> list(context.getSource(),
                                        DimensionArgumentType.getDimensionArgument(context, "world"))))
                        .then(argument("page", IntegerArgumentType.integer(0))
                                .executes((context) -> listWorlds(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "page"))))
                        .executes((context) -> listWorlds(context.getSource())))
                .then(literal("find")
                        .executes((context) -> find(context.getSource())))
                .then(literal("grave")
                        .then(argument("targets", EntityArgumentType.players())
                                .executes((context) -> grave(context.getSource(), getPlayers(context, "targets"))))
                        .executes((context) -> grave(context.getSource(), List.of(context.getSource().getPlayer()))))

        );
    }

    private static int listWorlds(ServerCommandSource source) throws CommandSyntaxException
    {
        return listWorlds(source, 1);
    }
    private static int listWorlds(ServerCommandSource source, int page) throws CommandSyntaxException
    {
        List<Pair<ServerWorld, Integer>> worlds = new ArrayList<>();

        ServerWorld currentWorld = source.getWorld();
        int currentWorldGraves = GraveHandler.getManager(currentWorld).getGraves().size();
        worlds.add(new Pair<>(currentWorld, currentWorldGraves));

        for (ServerWorld serverWorld : source.getServer().getWorlds())
            if (serverWorld != currentWorld)
                worlds.add(new Pair<>(serverWorld, GraveHandler.getManager(serverWorld).getGraves().size()));

        source.sendFeedback(() -> GravesCommandFormatter.gravesMultiWorldListPage(worlds, page), false);
        return 0;
    }

    private static int list(ServerCommandSource source, ServerWorld world) throws CommandSyntaxException
    {
        return list(source, world, 1);
    }

    private static int list(ServerCommandSource source, ServerWorld world, int page) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(world);
        List<Grave> graves = manager.getGraves();

        source.sendFeedback(() -> GravesCommandFormatter.gravesListPage(manager, graves, page), false);
        return 0;
    }

    private static int find(ServerCommandSource source) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(source.getWorld());
        Grave grave = manager.findGrave(source.getPlayer().getBlockPos());

        if (grave == null)
        {
            source.sendFeedback(() -> Text.literal("No valid grave found :/"), false);
            return 0;
        }

        source.sendFeedback(() -> GravesCommandFormatter.graveDistance(grave, source.getPlayer().getBlockPos()), false);
        return 0;
    }

    private static int grave(ServerCommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException
    {
        source.sendFeedback(() -> GravesCommandFormatter.gravedListHeader(targets), false);
        for (ServerPlayerEntity player : targets)
        {
            GraveManager manager = GraveHandler.getManager(player.getServerWorld());
            var pos = GraveHandler.GravePlayerInAllManagers(player);

            source.sendFeedback(() -> GravesCommandFormatter.gravedListEntry(player, pos), false);
            player.sendMessage(GravesCommandFormatter.gravedDM(), false);
        }
        return 0;
    }
}
