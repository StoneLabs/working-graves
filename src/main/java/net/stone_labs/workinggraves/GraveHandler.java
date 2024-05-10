package net.stone_labs.workinggraves;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class GraveHandler
{
    public record WorldBlockPosTuple(ServerWorld server, BlockPos position){}

    public static GraveManager getManager(ServerWorld world)
    {
        // In analogy to ServerWorld RaidManager call
        return world.getPersistentStateManager().getOrCreate(
                GraveManager.getPersistentStateType(world),
                GraveManager.nameFor(world.getDimensionEntry()));
    }

    public static WorldBlockPosTuple GravePlayerInAllManagers(ServerPlayerEntity player)
    {
        ServerWorld currentWorld = player.getServerWorld();
        GraveManager graveManager = GraveHandler.getManager(currentWorld);

        if (!graveManager.getGraves().isEmpty())
        {
            return new WorldBlockPosTuple(currentWorld, graveManager.gravePlayer(player));
        }
        else if (WorkingGraves.Settings.graveInAllDimensions)
        {
            for (ServerWorld world : Objects.requireNonNull(player.getServer()).getWorlds())
            {
                GraveManager otherWorldGraveManager = GraveHandler.getManager(world);
                if (!otherWorldGraveManager.getGraves().isEmpty())
                    return new WorldBlockPosTuple(world, otherWorldGraveManager.gravePlayer(player));
            }
        }

        return null;
    }

    public static void Interact(ServerPlayerEntity player, ServerWorld world, SignBlockEntity sign)
    {
        if (!player.hasPermissionLevel(WorkingGraves.Settings.requiredPermissionLevel))
            return;

        if (WorkingGraves.Settings.requireSoulTorch && !player.getMainHandStack().getItem().equals(Items.SOUL_TORCH))
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
