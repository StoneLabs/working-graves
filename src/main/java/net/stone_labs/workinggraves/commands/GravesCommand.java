package net.stone_labs.workinggraves.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.stone_labs.workinggraves.Grave;
import net.stone_labs.workinggraves.GraveHandler;
import net.stone_labs.workinggraves.GraveManager;

import java.util.Collection;
import java.util.List;

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
                        .then(argument("page", IntegerArgumentType.integer(0))
                                .executes((context) -> list(context.getSource(), IntegerArgumentType.getInteger(context, "page"))))
                        .executes((context) -> list(context.getSource())))
                .then(literal("find")
                        .executes((context) -> find(context.getSource())))
                .then(literal("grave")
                        .then(argument("targets", EntityArgumentType.players())
                                .executes((context) -> grave(context.getSource(), getPlayers(context, "targets"))))
                        .executes((context) -> grave(context.getSource(), List.of(context.getSource().getPlayer()))))

        );
    }

    private static int list(ServerCommandSource source) throws CommandSyntaxException
    {
        return list(source, 1);
    }

    private static int list(ServerCommandSource source, int page) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(source.getWorld());
        List<Grave> graves = manager.getGraves();

        source.sendFeedback(GravesCommandFormatter.gravesListPage(manager, graves, page), false);
        return 0;
    }

    private static int find(ServerCommandSource source) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(source.getWorld());
        Grave grave = manager.findGrave(source.getPlayer().getBlockPos());

        if (grave == null)
        {
            source.sendFeedback(new LiteralText("No valid grave found :/"), false);
            return 0;
        }

        source.sendFeedback(GravesCommandFormatter.graveDistance(grave, source.getPlayer().getBlockPos()), false);
        return 0;
    }

    private static int grave(ServerCommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException
    {
        source.sendFeedback(GravesCommandFormatter.gravedListHeader(targets), false);
        for (ServerPlayerEntity player : targets)
        {
            GraveManager manager = GraveHandler.getManager(player.getServerWorld());
            BlockPos pos = manager.gravePlayer(player);

            source.sendFeedback(GravesCommandFormatter.gravedListEntry(player, pos), false);
        }
        return 0;
    }
}
