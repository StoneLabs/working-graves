package net.stone_labs.workinggraves.compat;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketEnums.DropRule;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class TrinketsCompat
{
    private static boolean enabled = false;
    public static List<ItemStack> getItems(ServerPlayerEntity player)
    {
        List<ItemStack> droppedTrinkets = new ArrayList<>();

        if(TrinketsApi.getTrinketComponent(player).isEmpty())
        {
            return droppedTrinkets;
        }
        List<Pair<SlotReference, ItemStack>> trinkets = TrinketsApi.getTrinketComponent(player).get().getAllEquipped();

        for (Pair<SlotReference, ItemStack> pair: trinkets)
        {
            ItemStack stack = pair.getRight();
            SlotReference slot = pair.getLeft();

            // Mimic behavior in dev.emi.trinkets.mixin.LivingEntityMixin#dropInventory(...)
            DropRule dropRule = TrinketsApi.getTrinket(stack.getItem()).getDropRule(stack, slot, player);
            if (dropRule == DropRule.DEFAULT)
                dropRule = slot.inventory().getSlotType().getDropRule();
            if (dropRule == DropRule.DEFAULT || dropRule == DropRule.DROP)
            {
                droppedTrinkets.add(stack);
                slot.inventory().setStack(slot.index(), ItemStack.EMPTY);
            }
        }
        return droppedTrinkets;
    }

    public static void enable()
    {
        enabled = true;
    }

    public static boolean isEnabled()
    {
        return enabled;
    }
}
