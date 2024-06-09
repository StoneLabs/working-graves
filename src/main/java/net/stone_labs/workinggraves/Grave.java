package net.stone_labs.workinggraves;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.stone_labs.workinggraves.compat.Mod;
import net.stone_labs.workinggraves.compat.Trinkets.Trinkets;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Grave(ServerWorld world, BlockPos position)
{
    public SignBlockEntity getSignBlockEntity()
    {
        BlockEntity blockEntity = world.getBlockEntity(position);
        if (!(blockEntity instanceof SignBlockEntity sign))
        {
            WorkingGraves.LOGGER.warn("Block entity at sign position is not a sign!");
            return null;
        }
        return sign;
    }

    public boolean isValid()
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return false;

        return isValid(sign);
    }
    private boolean isValid(boolean side)
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return false;

        return isValid(sign, side);
    }
    private static boolean isValid(SignBlockEntity sign)
    {
        return isValid(sign, true) || isValid(sign, false);
    }

    private static boolean isValid(SignBlockEntity sign, boolean side)
    {
        if (!isGrave(sign, side))
            return false;

        GraveManager manager = GraveHandler.getManager((ServerWorld) sign.getWorld());
        Text[] signText = sign.getText(side).getMessages(false);

        boolean valid = true;
        String[] texts = manager.getKey();
        for (int i = 0; i < signText.length; i++) {
            valid = valid && texts[i].equalsIgnoreCase(signText[i].getString()) && signText[i].getStyle().isUnderlined();
        }
        return valid;
    }

    public boolean isGrave()
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return false;

        return isGrave(sign);
    }
    public boolean isGrave(String[] key)
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return false;

        return isGrave(sign, key);
    }
    public boolean isGrave(boolean side)
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return false;

        return isGrave(sign, side);
    }
    public boolean isGrave(boolean side, String[] key)
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return false;

        return isGrave(sign, side, key);
    }
    public static boolean isGrave(SignBlockEntity sign)
    {
        return isGrave(sign, true) || isGrave(sign, false);
    }
    public static boolean isGrave(SignBlockEntity sign, String[] key)
    {
        return isGrave(sign, true, key) || isGrave(sign, false, key);
    }public static boolean isGrave(SignBlockEntity sign, boolean side)
{
    GraveManager manager = GraveHandler.getManager((ServerWorld) sign.getWorld());
    return isGrave(sign, side, manager.getKey());
}
    public static boolean isGrave(SignBlockEntity sign, boolean side, String[] key)
    {
        Text[] signText = sign.getText(side).getMessages(true);

        boolean equal = true;
        for (int i = 0; i < signText.length; i++) {
            equal = equal && signText[i].getString().equalsIgnoreCase(key[i]);
        }
        return equal;
    }

    public void makeValid()
    {
        if (isGrave(true))
            makeValid(true);

        if (isGrave(false))
            makeValid(false);
    }
    public void makeValid(boolean side)
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return;

        GraveManager manager = GraveHandler.getManager(world);
        String[] texts = manager.getKey();
        SignText signText = new SignText();
        for (int i = 0; i < texts.length; i++) {
            signText = signText.withMessage(i, Text.literal(texts[i]).formatted(Formatting.UNDERLINE));
        }
        sign.setText(signText, side);
        sign.setWaxed(true);
        world.getServer().getPlayerManager().sendToAll(sign.toUpdatePacket());
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

    private void setGraveText(ServerPlayerEntity player)
    {
        Date date = new Date();

        if (isGrave(true))
            setGraveText(player, date, true);

        if (isGrave(false))
            setGraveText(player, date, false);
    }
    private void setGraveText(ServerPlayerEntity player, Date time, boolean side)
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return;

        // Change sign
        SignText signText = new SignText();
        signText = signText.withMessage(0, Text.literal(player.getGameProfile().getName()));
        signText = signText.withMessage(1, Text.literal("Level %d".formatted(player.experienceLevel)));
        signText = signText.withMessage(2, Text.literal(new SimpleDateFormat("yyyy MM dd").format(time)));
        signText = signText.withMessage(3, Text.literal(new SimpleDateFormat("HH:mm:ss").format(time)));
        sign.setText(signText, side);
        sign.setWaxed(true);

        world.getServer().getPlayerManager().sendToAll(sign.toUpdatePacket());
    }

    private void gravePlayerInventory(ServerPlayerEntity player)
    {
        PlayerInventory playerInventory = player.getInventory();
        List<Inventory> targetInventories = getInventoryStorage();
        List<Integer> targetSlots = IntStream.range(0, 30).boxed().collect(Collectors.toList());

        java.util.function.Consumer<ItemStack> saveStack = (itemStack) ->
        {
            if (itemStack.getItem().equals(Items.AIR))
                return;

            if (EnchantmentHelper.hasVanishingCurse(itemStack))
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


        for (ItemStack stack : Mod.TRINKETS.runIfInstalled(() -> () -> Trinkets.getItems(player)).orElse(new ArrayList<ItemStack>()))
            saveStack.accept(stack);

        playerInventory.clear();
    }

    public void spawnGraveEffects()
    {
        SignBlockEntity sign = getSignBlockEntity();
        if (sign == null)
            return;

        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ(), 500, 5, 3, 5, 0.001);
        for (int i = 0; i < 5; i++)
        {
            Random random = world.getRandom();

            LightningEntity lightningEntity = (LightningEntity)EntityType.LIGHTNING_BOLT.create(world);
            if (!WorkingGraves.Settings.doLightningFire)
                lightningEntity.setCosmetic(true);

            //noinspection ConstantConditions
            lightningEntity.refreshPositionAfterTeleport(sign.getPos().getX() + random.nextFloat(), sign.getPos().getY(), sign.getPos().getZ() + random.nextFloat());
            world.spawnEntity(lightningEntity);
        }
    }

    public void gravePlayer(ServerPlayerEntity player)
    {
        // Populate grave with items
        if (!player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY))
            gravePlayerInventory(player);

        setGraveText(player);
        spawnGraveEffects();
    }
}
