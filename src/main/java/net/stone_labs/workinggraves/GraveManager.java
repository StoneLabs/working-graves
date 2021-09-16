package net.stone_labs.workinggraves;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class GraveManager
{
    public static boolean IsValid(AbstractSignBlock sign)
    {
        return true; //todo, return valid grave or not
    }

    public static void Interact(ServerPlayerEntity player, ServerWorld world, SignBlockEntity sign)
    {
        if (!sign.getTextOnRow(0, false).asString().trim().equalsIgnoreCase("empty grave"))
            return;

        if (!player.isSneaking())
            return;

        // Underline if required
        if (!sign.getTextOnRow(0, false).getStyle().isUnderlined())
        {
            sign.setTextOnRow(0, new LiteralText("empty grave").formatted(Formatting.UNDERLINE));
            player.server.getPlayerManager().sendToAll(sign.toUpdatePacket());
        }

        // Register grave if not registered
        // Todo, check not registered here
        {
            // Todo, register
        }

        // Show particles if grave is valid
        // Todo, check valid
        {
            world.spawnParticles(ParticleTypes.GLOW, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 5, 1, 1, 1, 0.1);
        }
    }
}
