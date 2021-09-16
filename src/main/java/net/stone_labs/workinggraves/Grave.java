package net.stone_labs.workinggraves;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Grave(ServerWorld world, BlockPos position)
{
    public static String KEY = "hic portus animae";

    public boolean isValid()
    {
        BlockEntity blockEntity = world.getBlockEntity(position);
        if (!(blockEntity instanceof SignBlockEntity sign))
            return false;

        if (!isGraveSign(sign))
            return false;

        //noinspection RedundantIfStatement
        if (!sign.getTextOnRow(0, false).getStyle().isUnderlined())
            return false;

        return true;
    }

    public void makeValid()
    {
        BlockEntity blockEntity = world.getBlockEntity(position);
        if (!(blockEntity instanceof SignBlockEntity sign))
            return;

        // Underline if required
        sign.setTextOnRow(0, new LiteralText(Grave.KEY).formatted(Formatting.UNDERLINE));
        world.getServer().getPlayerManager().sendToAll(sign.toUpdatePacket());
    }

    public static boolean isGraveSign(SignBlockEntity sign)
    {
        return sign.getTextOnRow(0, false).asString().trim().equalsIgnoreCase(KEY);
    }

    public List<Inventory> getInventoryStorage()
    {
        List<Inventory> inventories = new ArrayList<>();
        for (int x = -1; x <= 1; x++)
            for (int y = -3; y <= -1; y++)
                for (int z = -1; z <= 1; z++)
                {
                    BlockEntity entity = world.getBlockEntity(position.add(x, y, z));
                    if (entity instanceof LootableContainerBlockEntity entityInventory)
                        inventories.add(entityInventory);
                }

        return inventories;
    }

    public void gravePlayer(ServerPlayerEntity player)
    {
        BlockEntity blockEntity = world.getBlockEntity(position);
        if (!(blockEntity instanceof SignBlockEntity sign))
            return;

        // Change sign
        sign.setTextOnRow(0, new LiteralText(player.getEntityName()));
        sign.setTextOnRow(1, new LiteralText("Level %d".formatted(player.experienceLevel)));
        sign.setTextOnRow(2, new LiteralText(new SimpleDateFormat("yyyy MM dd").format(new Date())));
        sign.setTextOnRow(3, new LiteralText(new SimpleDateFormat("HH:mm:ss").format(new Date())));
        world.getServer().getPlayerManager().sendToAll(sign.toUpdatePacket());

        // Items
        PlayerInventory playerInventory = player.getInventory();
        List<Inventory> targetInventories = getInventoryStorage();
        List<Integer> targetSlots = IntStream.range(0, 30).boxed().collect(Collectors.toList());

        java.util.function.Consumer<ItemStack> saveStack = (itemStack) ->
        {
            if (itemStack.getItem().equals(Items.AIR))
                return;

            Collections.shuffle(targetInventories);
            Collections.shuffle(targetSlots);
            for (Inventory inventory : targetInventories)
                for (Integer slot : targetSlots)
                    try
                    {
                        ItemStack targetSlot = inventory.getStack(slot);
                        if (targetSlot.getItem().equals(Items.AIR))
                        {
                            inventory.setStack(slot, itemStack);
                            return;
                        }
                    }
                    catch (Exception ignored)
                    {
                    }

            ItemEntity itemEntity = new ItemEntity(this.world, position.getX(), position.getY(), position.getZ(), itemStack);
            itemEntity.setToDefaultPickupDelay();
            itemEntity.setInvulnerable(true);
            this.world.spawnEntity(itemEntity);
        };

        for (ItemStack stack : playerInventory.armor)
            saveStack.accept(stack);

        for (ItemStack stack : playerInventory.offHand)
            saveStack.accept(stack);

        for (ItemStack stack : playerInventory.main)
            saveStack.accept(stack);

        playerInventory.clear();

        // Effects
        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 500, 5, 3, 5, 0.001);
        for (int i = 0; i < 5; i++)
        {
            LightningEntity entitybolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            entitybolt.setPos(sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ());
            world.spawnEntity(entitybolt);
        }
    }
}
