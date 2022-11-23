package net.stone_labs.workinggraves.commands;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.stone_labs.workinggraves.Grave;
import net.stone_labs.workinggraves.GraveManager;

import java.util.Collection;
import java.util.List;

public class GravesCommandFormatter
{
    public static int pageLimit = 8;


    public static Text gravesListPage(GraveManager manager, List<Grave> graves, int page)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("[\"\nThe following graves were found: %s\", ".formatted(manager.getWorld().getRegistryKey().getValue().toString()));
        for (int i = pageLimit * (page-1); i < page * pageLimit; i++)
        {
            if (i < graves.size())
            {
                builder.append("\"\n- %2d: \", {\"text\":\"%d, %d, %d\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp %2$d %3$d %4$d\"}}, "
                        .formatted(i, graves.get(i).position().getX(), graves.get(i).position().getY(), graves.get(i).position().getZ()));

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
        return Text.Serializer.fromJson(builder.toString());
    }

    public static Text graveDistance(Grave grave, BlockPos reference)
    {
        return Text.Serializer.fromJson("[\"\",\"Next grave \",{\"text\":\"[%s]\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp %d %d %d\"}}, \" at distance %.2f blocks\"]"
                        .formatted(grave.position().toShortString(), grave.position().getX(), grave.position().getY(), grave.position().getZ(), grave.position().getSquaredDistance(reference)));
    }

    public static Text gravedListEntry(ServerPlayerEntity player, BlockPos pos)
    {
        if (pos == null)
            return new LiteralText("- %s: §4no grave available.§r".formatted(player.getEntityName()));
        else
            return Text.Serializer.fromJson("[\"- %s: \", {\"text\":\"[%d %d %d]\",\"underlined\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp %2$d %3$d %4$d\"}}]"
                    .formatted(player.getEntityName(), pos.getX(), pos.getY(), pos.getZ()));
    }

    public static Text gravedListHeader(Collection<ServerPlayerEntity> targets)
    {
        return new LiteralText("Graved %d players:".formatted(targets.size()));
    }

    public static Text gravedDM()
    {
        return new LiteralText("Your death has been simulated by a server operator.").formatted(Formatting.RED);
    }
}
