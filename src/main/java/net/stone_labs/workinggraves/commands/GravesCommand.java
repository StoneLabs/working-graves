package net.stone_labs.workinggraves.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.stone_labs.workinggraves.*;

import java.util.Arrays;
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
                .then(literal("list")
                        .requires(WorkingGraves.PERMISSION_MANAGER.require(PermissionManager.Permission.COMMAND_LIST))
                        .then(argument("page", IntegerArgumentType.integer(0))
                                .executes((context) -> list(context.getSource(), IntegerArgumentType.getInteger(context, "page"))))
                        .executes((context) -> list(context.getSource())))
                .then(literal("find")
                        .requires(WorkingGraves.PERMISSION_MANAGER.require(PermissionManager.Permission.COMMAND_FIND))
                        .executes((context) -> find(context.getSource())))
                .then(literal("key")
                        .requires(WorkingGraves.PERMISSION_MANAGER.require(PermissionManager.Permission.COMMAND_KEY))
                        .then(literal("change")
                                .executes((context) -> changeGraveKey(context.getSource())))
                        .then(literal("get")
                                .executes((context) -> sendGraveKeyFeedback(context.getSource()))))
                .then(literal("grave")
                        .requires(WorkingGraves.PERMISSION_MANAGER.require(PermissionManager.Permission.COMMAND_DEBUG))
                        .then(argument("targets", EntityArgumentType.players())
                                .executes((context) -> grave(context.getSource(), getPlayers(context, "targets"))))
                        .executes((context) -> grave(context.getSource(), List.of(context.getSource().getPlayer()))))

        );
    }

    public static int sendGraveKeyFeedback(ServerCommandSource source) {
        var player = source.getPlayer();
        assert player != null;
        GraveManager manager = GraveHandler.getManager(player.getServerWorld());
        StringBuilder builder = new StringBuilder();
        builder.append("{\"text\":\"Grave key:");
        for (String text : manager.getKey()) {
            builder.append("\n");
            builder.append(text);
        }
        builder.append("\",\"color\":\"#2AA198\"}");
        source.sendFeedback(() -> Text.Serialization.fromJson(builder.toString()), false);
        return 1;
    }

    public static int changeGraveKey(ServerCommandSource source) {
        var player = source.getPlayer();
        assert player != null;
        HitResult hitResult = player.raycast(5.0, 0.0f, false);

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            source.sendFeedback(() -> Text.literal("You are not looking at any block."), false);
            return 0;
        }

        BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
        BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
        if (!(blockEntity instanceof SignBlockEntity signEntity)) {
            source.sendFeedback(() -> Text.literal("You must be looking at a sign to change the grave key."), false);
            return 0;
        }

        // Bestimme die Orientierung des Schildes relativ zur Spielerposition.
        boolean lookingAtFront = signEntity.isPlayerFacingFront(player);

        // Aktualisiere den Grave-Key basierend auf dem Text des Schildes.
        GraveManager manager = GraveHandler.getManager(player.getServerWorld());
        String[] newKey = Arrays.stream(signEntity.getText(lookingAtFront).getMessages(true))
                .map(Text::getString)
                .toArray(String[]::new);
        String[] oldKey = manager.getKey();
        manager.setKey(newKey);

        // Überprüfe und aktualisiere die Gültigkeit aller Gräber.
        manager.updateGravesKey(oldKey);

        source.sendFeedback(() -> Text.literal("Grave key successfully changed!"), false);
        return sendGraveKeyFeedback(source);
    }

    private static int list(ServerCommandSource source) throws CommandSyntaxException
    {
        return list(source, 1);
    }

    private static int list(ServerCommandSource source, int page) throws CommandSyntaxException
    {
        GraveManager manager = GraveHandler.getManager(source.getWorld());
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
