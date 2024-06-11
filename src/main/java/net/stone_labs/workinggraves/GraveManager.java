package net.stone_labs.workinggraves;

import net.minecraft.datafixer.DataFixTypes;
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

import java.util.*;

public class GraveManager extends PersistentState {
    ServerWorld world;
    List<Grave> graves;
    String[] privateKey;
    String[] publicKey;

    public void setPrivateKey(String[] key) {
        this.privateKey = key;
    }

    public String[] getPrivateKey() {
        return privateKey;
    }

    public void setPublicKey(String[] key) {
        this.publicKey = key;
    }

    public String[] getPublicKey() {
        return publicKey;
    }

    public GraveManager(ServerWorld world) {
        this.world = world;
        graves = new ArrayList<>();
        privateKey = new String[4];
        publicKey = new String[4];
        for (int i = 1; i < 4; i++) {
            privateKey[i] = "";
            publicKey[i] = "";
        }
        privateKey[0] = "hic portus animae";
        privateKey[1] = "meae animae";
        privateKey[2] = "reservatus";
        publicKey[0] = "hic portus animae";
    }

    // In analogy to net.minecraft.village.raid.RaidManager.nameFor
    public static String nameFor(RegistryEntry<DimensionType> dimensionTypeEntry) {
        if (dimensionTypeEntry.matchesKey(DimensionTypes.THE_END)) {
            return "graves_end";
        }
        return "graves";
    }

    public static Type<GraveManager> getPersistentStateType(ServerWorld world) {
        return new Type<GraveManager>(
                () -> new GraveManager(world),
                (nbt) -> fromNbt(world, nbt),
                DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    public Grave addGrave(BlockPos pos, boolean isPrivate, UUID ownerUUID) {
        Optional<Grave> existingGrave = graves.stream().filter(grave -> grave.position().equals(pos)).findFirst();
        if (existingGrave.isPresent()) {
            return existingGrave.get();
        }

        Grave newGrave = new Grave(world, pos, isPrivate, ownerUUID);
        graves.add(newGrave);
        this.setDirty(true);
        return newGrave;
    }

    public void removeGrave(BlockPos pos) {
        if (graves.removeIf(grave -> grave.position().equals(pos)))
            this.setDirty(true);
    }

    public List<Grave> getGraves() {
        return graves;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public BlockPos gravePlayer(ServerPlayerEntity player) {
        // TODO: How should the nearest grave be defined in interdimensional graving?
        Grave grave = findGrave(player.getBlockPos(), player.getUuid());
        if (grave == null) {
            WorkingGraves.LOGGER.info("No grave found for player %s".formatted(player.getGameProfile().getName()));
            return null;
        }

        WorkingGraves.LOGGER.info("Found grave for player %s at %s".formatted(player.getGameProfile().getName(), grave.position().toShortString()));
        grave.gravePlayer(player);
        removeGrave(grave.position());
        return grave.position();
    }

    public Grave findGrave(BlockPos pos, UUID playerUUID) {
        List<Grave> closestGraves = graves.stream()
                .sorted(Comparator.comparingDouble(grave -> grave.position().getSquaredDistance(pos)))
                .toList();

        Grave publicGrave = null;
        for (Grave grave : closestGraves) {
            if (grave.isValid()) {
                if (grave.isPrivate() && grave.ownerUUID().equals(playerUUID)) {
                    return grave;
                } else if (!grave.isPrivate() && publicGrave == null) {
                    publicGrave = grave;
                }
            }
        }

        return publicGrave != null ? publicGrave : (closestGraves.isEmpty() ? null : closestGraves.get(0));
    }

    public void updateGravesKey(String[] oldKey) {
        for (Grave grave : getGraves()) {
            if (grave.isValid())
                continue;

            if (grave.isGrave(true, oldKey))
                grave.makeValid(true);
            if (grave.isGrave(false, oldKey))
                grave.makeValid(false);
        }
    }

    public static GraveManager fromNbt(ServerWorld serverWorld, NbtCompound nbt) {
        GraveManager manager = new GraveManager(serverWorld);
        NbtList graveList = nbt.getList("graves", NbtElement.COMPOUND_TYPE);

        if (!graveList.isEmpty()) {
            for (NbtElement graveEntry : graveList) {
                NbtCompound graveCompound = (NbtCompound) graveEntry;
                BlockPos pos = new BlockPos(graveCompound.getInt("x"), graveCompound.getInt("y"), graveCompound.getInt("z"));
                UUID ownerUUID = graveCompound.getUuid("ownerUUID");
                boolean isPrivate = graveCompound.getBoolean("isPrivate");
                manager.addGrave(pos, isPrivate, ownerUUID);
            }
        } else {
            graveList = nbt.getList("graves", NbtElement.INT_ARRAY_TYPE);
            for (NbtElement graveEntry : graveList) {
                NbtIntArray gravePosition = (NbtIntArray) graveEntry;
                BlockPos pos = new BlockPos(gravePosition.get(0).intValue(), gravePosition.get(1).intValue(), gravePosition.get(2).intValue());
                manager.addGrave(pos, false, UUID.randomUUID());
            }
        }

        return manager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList nbtGraves = new NbtList();
        for (Grave grave : graves) {
            NbtCompound graveCompound = new NbtCompound();
            graveCompound.putInt("x", grave.position().getX());
            graveCompound.putInt("y", grave.position().getY());
            graveCompound.putInt("z", grave.position().getZ());
            graveCompound.putUuid("ownerUUID", grave.ownerUUID());
            graveCompound.putBoolean("isPrivate", grave.isPrivate());
            nbtGraves.add(graveCompound);
        }

        nbt.put("graves", nbtGraves);
        return nbt;
    }
}
