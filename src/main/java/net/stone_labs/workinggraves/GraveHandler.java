package net.stone_labs.workinggraves;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.RaidManager;

public class GraveHandler
{
    public static boolean IsValid(AbstractSignBlock sign)
    {
        return true; //todo, return valid grave or not
    }

    public static GraveManager getManager(ServerWorld world)
    {
        return world.getPersistentStateManager().getOrCreate((nbtCompound) -> {
            return GraveManager.fromNbt(world, nbtCompound);
        }, () -> {
            return new GraveManager(world);
        }, GraveManager.nameFor(world.getDimension()));
    }

    public static void Interact(ServerPlayerEntity player, ServerWorld world, SignBlockEntity sign)
    {
        if (!sign.getTextOnRow(0, false).asString().trim().equalsIgnoreCase("empty grave"))
            return;

        // Underline if required
        if (!sign.getTextOnRow(0, false).getStyle().isUnderlined())
        {
            sign.setTextOnRow(0, new LiteralText("empty grave").formatted(Formatting.UNDERLINE));
            player.server.getPlayerManager().sendToAll(sign.toUpdatePacket());
        }

        // Register grave if not registered
        GraveManager manager = getManager(world);
        manager.addGrave(sign.getPos());

        // Show particles if grave is valid
        // Todo, check valid
        {
            world.spawnParticles(ParticleTypes.GLOW, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 5, 1, 1, 1, 0.1);
        }
    }

    public static void GravePlayerAtSign(ServerPlayerEntity player, ServerWorld world, SignBlockEntity sign)
    {
        // On death
        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 500, 5, 3, 5, 0.001);
        for (int i = 0; i < 5; i++)
        {
            LightningEntity entitybolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            entitybolt.setPos(sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ());
            world.spawnEntity(entitybolt);
        }
    }
}
