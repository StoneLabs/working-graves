package net.stone_labs.workinggraves;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.ArrayList;
import java.util.List;

public class GraveManager extends PersistentState
{
    ServerWorld world;
    List<Grave> graves;

    public GraveManager(ServerWorld world)
    {
        this.world = world;
        graves = new ArrayList<>();
    }

    // In analogy to net.minecraft.village.raid.RaidManager.nameFor
    public static String nameFor(RegistryEntry<DimensionType> dimensionTypeEntry) {
        if (dimensionTypeEntry.matchesKey(DimensionTypes.THE_END)) {
            return "graves_end";
        }
        return "graves";
    }

    public void addGrave(BlockPos pos)
    {
        if (graves.stream().anyMatch(grave -> grave.position().equals(pos)))
            return;

        Grave grave = new Grave(world, pos);
        graves.add(grave);
        this.setDirty(true);
    }

    public void removeGrave(BlockPos pos)
    {
        if (graves.removeIf(grave -> grave.position().equals(pos)))
            this.setDirty(true);
    }

    public List<Grave> getGraves()
    {
        return graves;
    }

    public ServerWorld getWorld()
    {
        return world;
    }

    public BlockPos gravePlayer(ServerPlayerEntity player)
    {
        if (player.getWorld() != world)
        {
            WorkingGraves.LOGGER.warn("Attempting to grave player in incorrect dimension.");
            return null;
        }

        Grave grave = findGrave(player.getBlockPos());
        if (grave == null)
        {
            WorkingGraves.LOGGER.info("No grave found for player %s".formatted(player.getEntityName()));
            return null;
        }

        WorkingGraves.LOGGER.info("Found grave for player %s at %s".formatted(player.getEntityName(), grave.position().toShortString()));
        grave.gravePlayer(player);
        removeGrave(grave.position());
        return grave.position();
    }

    public Grave findGrave(BlockPos pos)
    {
        Grave[] closestGraves = graves.stream().sorted((o1, o2) -> {
            double o1Dist = o1.position().getSquaredDistance(pos);
            double o2Dist = o2.position().getSquaredDistance(pos);
            return Double.compare(o1Dist, o2Dist);
        }).toArray(Grave[]::new);

        for (Grave grave : closestGraves)
        {
            if (grave.isValid())
                return grave;
            else
                removeGrave(grave.position());
        }
        return null;
    }

    public static GraveManager fromNbt(ServerWorld serverWorld, NbtCompound nbt)
    {
        GraveManager manager = new GraveManager(serverWorld);
        NbtList graveList = nbt.getList("graves", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement graveEntry : graveList)
        {
            NbtIntArray gravePosition = (NbtIntArray) graveEntry;
            manager.addGrave(new BlockPos(gravePosition.get(0).intValue(), gravePosition.get(1).intValue(), gravePosition.get(2).intValue()));
        }

        return manager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt)
    {
        NbtList NbtGraves = new NbtList();
        for (Grave grave : graves)
           NbtGraves.add(new NbtIntArray(new int[] {grave.position().getX(), grave.position().getY(), grave.position().getZ()}));

        nbt.put("graves", NbtGraves);
        return nbt;
    }
}
