package net.stone_labs.workinggraves;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record Grave(ServerWorld world, BlockPos position)
{
    public boolean isValid()
    {
        BlockEntity blockEntity = world.getBlockEntity(position);
        if (!(blockEntity instanceof SignBlockEntity sign))
            return false;

        if (!sign.getTextOnRow(0, false).asString().trim().equalsIgnoreCase("empty grave"))
            return false;

        //noinspection RedundantIfStatement
        if (!sign.getTextOnRow(0, false).getStyle().isUnderlined())
            return false;

        return true;
    }
}
