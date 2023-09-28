package net.stone_labs.workinggraves;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.function.Function;
import java.util.function.Supplier;

public class GraveHandler
{
    public static boolean requireSoulTorch = true;

    public static GraveManager getManager(ServerWorld world)
    {
        // In analogy to ServerWorld RaidManager call
        return world.getPersistentStateManager().getOrCreate(
                GraveManager.getPersistentStateType(world),
                GraveManager.nameFor(world.getDimensionEntry()));
    }

    public static void Interact(ServerPlayerEntity player, ServerWorld world, SignBlockEntity sign)
    {
        if (requireSoulTorch && !player.getMainHandStack().getItem().equals(Items.SOUL_TORCH))
            return;

        if (!Grave.isGrave(sign))
            return;

        Grave grave = new Grave(world, sign.getPos());
        GraveManager manager = getManager(world);

        // Make grave valid and register
        grave.makeValid();
        manager.addGrave(grave.position());

        world.spawnParticles(ParticleTypes.GLOW, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 5, 1, 1, 1, 0.1);
    }
}
