package net.stone_labs.workinggraves.commands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.stone_labs.workinggraves.Grave;
import net.stone_labs.workinggraves.GraveManager;
import net.stone_labs.workinggraves.WorkingGraves;

import java.util.Collection;
import java.util.List;

public class GravesCommandFormatter
{
    public static int pageLimit = 8;

    public static Text gravesMultiWorldListPage(List<Pair<ServerWorld, Integer>> worlds, int page)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("[\"\nThe following dimensions contain graves:\", ");
        for (int i = pageLimit * (page-1); i < page * pageLimit; i++)
        {
            if (i < worlds.size())
            {
                builder.append("\"\n- %2s: \", {\"text\":\"%s\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/graves list %2$s\"}}, "
                        .formatted(i, worlds.get(i).getLeft().getRegistryKey().getValue().toString()));

                builder.append("\" \", ");
                if (i > 0 && !WorkingGraves.Settings.graveInAllDimensions)
                    builder.append("{\"text\":\"(%d) [MULTIDIM IS OFF]\",\"color\":\"red\"}, ".formatted(worlds.get(i).getRight()));
                else if (worlds.get(i).getRight() > 0)
                    builder.append("{\"text\":\"(%d)\",\"color\":\"green\"}, ".formatted(worlds.get(i).getRight()));
                else if (worlds.get(i).getRight() <= 0)
                    builder.append("{\"text\":\"(%d)\",\"color\":\"yellow\"}, ".formatted(worlds.get(i).getRight()));
            }
            else
                builder.append("\"\n- %2d: \", ".formatted(i));
        }

        int maxPage = (worlds.size()-1) / pageLimit + 1;
        if (page == 1)
            builder.append("\"\n<<<<\", ");
        else
            builder.append("\"\n\", {\"text\":\"<<<<\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/graves list %d\"}}, ".formatted(page - 1));
        builder.append("\" | Page %d of %d | \", ".formatted(page, maxPage));
        if (page == maxPage)
            builder.append("\">>>>\", ");
        else
            builder.append(" {\"text\":\">>>>\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/graves list %d\"}}, ".formatted(page + 1));

        builder.append("\"\"]");
        //return new LiteralText(builder.toString());
        return Text.Serialization.fromJson(builder.toString());
    }
    public static Text gravesListPage(GraveManager manager, List<Grave> graves, int page)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("[\"\nThe following graves were found: \",  {\"text\":\"%s\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/graves list\"}}, "
                .formatted(manager.getWorld().getRegistryKey().getValue().toString()));
        for (int i = pageLimit * (page-1); i < page * pageLimit; i++)
        {
            if (i < graves.size())
            {
                builder.append("\"\n- %2d: \", {\"text\":\"%d, %d, %d\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/execute in %s run tp %2$d %3$d %4$d\"}}, "
                        .formatted(i, graves.get(i).position().getX(), graves.get(i).position().getY(), graves.get(i).position().getZ(), manager.getWorld().getRegistryKey().getValue().toString()));

                builder.append("\" \", ");
                if (graves.get(i).isValid())
                    builder.append("{\"text\":\"(GRAVE IS VALID)\",\"color\":\"green\"}, ");
                else
                    builder.append("{\"text\":\"(GRAVE IS INVALID)\",\"color\":\"dark_red\"}, ");
            }
            else
                builder.append("\"\n- %2d: \", ".formatted(i));
        }

        int maxPage = (graves.size()-1) / pageLimit + 1;
        if (page == 1)
            builder.append("\"\n<<<<\", ");
        else
            builder.append("\"\n\", {\"text\":\"<<<<\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/graves list %d\"}}, ".formatted(page - 1));
        builder.append("\" | Page %d of %d | \", ".formatted(page, maxPage));
        if (page == maxPage)
            builder.append("\">>>>\", ");
        else
            builder.append(" {\"text\":\">>>>\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/graves list %d\"}}, ".formatted(page + 1));

        builder.append("\"\"]");
        //return new LiteralText(builder.toString());
        return Text.Serialization.fromJson(builder.toString());
    }

    public static Text graveDistance(Grave grave, BlockPos reference)
    {
        return Text.Serialization.fromJson("[\"\",\"Next grave \",{\"text\":\"[%s]\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp %d %d %d\"}}, \" at distance %.2f blocks\"]"
                        .formatted(grave.position().toShortString(), grave.position().getX(), grave.position().getY(), grave.position().getZ(), Math.sqrt(grave.position().getSquaredDistance(reference))));
    }

    public static Text gravedListEntry(ServerPlayerEntity player, GraveManager.WorldBlockPosTuple pos)
    {
        if (pos == null)
            return Text.literal("- %s: §4no grave available.§r".formatted(player.getGameProfile().getName()));
        else
            return Text.Serialization.fromJson("[\"- %s: \", {\"text\":\"[%d %d %d] (%s)\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/execute in %5$s run tp %2$d %3$d %4$d\"}}]"
                    .formatted(player.getGameProfile().getName(), pos.position().getX(), pos.position().getY(), pos.position().getZ(), pos.server().getRegistryKey().getValue().toString()));
    }

    public static Text gravedListHeader(Collection<ServerPlayerEntity> targets)
    {
        return Text.literal("Graved %d players:".formatted(targets.size()));
    }

    public static Text gravedDM()
    {
        return Text.literal("Your death has been simulated by a server operator.").formatted(Formatting.RED);
    }
}
