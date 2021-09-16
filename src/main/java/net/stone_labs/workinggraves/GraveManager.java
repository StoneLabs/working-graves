package net.stone_labs.workinggraves;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;

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

    public static String nameFor(DimensionType dimension)
    {
        // In analogy to RaidManager.nameFor(...)
        return "graves" + dimension.getSuffix();
    }

    public void addGrave(BlockPos pos)
    {
        if (graves.stream().anyMatch(grave -> grave.position().equals(pos)))
            return;

        graves.add(new Grave(world, pos));
        this.setDirty(true);
    }
    public void removeGrave(BlockPos pos)
    {
        graves.removeIf(grave -> grave.position().equals(pos));
    }

    public List<Grave> getGraves()
    {
        return graves;
    }

    public ServerWorld getWorld()
    {
        return world;
    }


    public Grave findGrave(ServerPlayerEntity player)
    {
        return findGrave(player, false);
    }
    public Grave findGrave(ServerPlayerEntity player, boolean ignoreValidity)
    {
        Grave[] closestGraves = graves.stream().sorted((o1, o2) -> {
            double o1Dist = o1.position().getSquaredDistance(player.getBlockPos());
            double o2Dist = o2.position().getSquaredDistance(player.getBlockPos());
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
