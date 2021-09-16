package net.stone_labs.workinggraves;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        graves.add(new Grave(pos));
        this.setDirty(true);
    }

    public static GraveManager fromNbt(ServerWorld serverWorld, NbtCompound nbt)
    {
        GraveManager manager = new GraveManager(serverWorld);
        manager.world.getPlayers().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(new LiteralText(nbt.asString()), false));

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
