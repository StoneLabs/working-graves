package net.stone_labs.workinggraves;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.text.SimpleDateFormat;
import java.util.Date;

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
