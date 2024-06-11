package net.stone_labs.workinggraves;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.UUID;

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
        else if (WorkingGraves.Settings.graveInAllDimensions && WorkingGraves.PERMISSION_MANAGER.check(player, PermissionManager.Permission.INTERDIMENSIONAL))
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
        if (!WorkingGraves.PERMISSION_MANAGER.check(player, PermissionManager.Permission.NEW))
            return;

        if (WorkingGraves.Settings.requireSoulTorch && !player.getMainHandStack().getItem().equals(Items.SOUL_TORCH))
            return;

        BlockState signState = sign.getCachedState();
        int signFacing = 0;

        try {
            signFacing = switch (signState.get(Properties.HORIZONTAL_FACING).asString()) {
                case "east" -> 270;
                case "north" -> 180;
                case "west" -> 90;
                default -> signFacing;
            };
        } catch (IllegalArgumentException e) {
            signFacing = (int) (signState.get(Properties.ROTATION) * 22.5);
        }

        float playerRotation = player.getYaw();
        playerRotation = (playerRotation % 360 + 360) % 360;

        float relativeAngle = Math.abs(playerRotation - signFacing);
        relativeAngle = relativeAngle > 180 ? 360 - relativeAngle : relativeAngle;

        boolean lookingAtFront = relativeAngle > 90;

        if (!Grave.isGrave(sign, lookingAtFront))
            return;

        GraveManager manager = getManager(world);
        boolean isPrivate = Grave.isGrave(sign, lookingAtFront, manager.getPrivateKey());
        boolean noPermission =
            (isPrivate && !WorkingGraves.PERMISSION_MANAGER.check(player,
                PermissionManager.Permission.NEW_PRIVATE)) ||
            (!isPrivate && !WorkingGraves.PERMISSION_MANAGER.check(player,
                PermissionManager.Permission.NEW_PUBLIC));

        if (noPermission)
            return;

        UUID ownerUUID = isPrivate ? player.getUuid() : UUID.randomUUID();
        Grave grave = manager.addGrave(sign.getPos(), isPrivate, ownerUUID);
        grave.makeValid(lookingAtFront);

        world.spawnParticles(ParticleTypes.GLOW, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 5, 1, 1, 1, 0.1);
    }
}
