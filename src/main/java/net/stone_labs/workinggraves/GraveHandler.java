package net.stone_labs.workinggraves;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
public class GraveHandler
{
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
        if (!player.getMainHandStack().getItem().equals(Items.SOUL_TORCH))
            return;

        if (!Grave.isGraveSign(sign))
            return;

        Grave grave = new Grave(world, sign.getPos());
        GraveManager manager = getManager(world);

        // Make grave valid and register
        grave.makeValid();
        manager.addGrave(grave.position());

        world.spawnParticles(ParticleTypes.GLOW, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 5, 1, 1, 1, 0.1);
    }
}
