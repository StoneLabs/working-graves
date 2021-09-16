package net.stone_labs.workinggraves;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.stone_labs.workinggraves.commands.GravesCommand;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkingGraves implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "workinggraves";
    public static final String MOD_NAME = "Working Graves";
    public static final String VERSION = "1.0.0";


    public static class PlayerBlockBreakEvent implements PlayerBlockBreakEvents.After
    {
        @Override
        public void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity)
        {

        }
    }

    public static class PlayerUseBlockEvent implements UseBlockCallback
    {
        @Override
        public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult)
        {
            ServerWorld serverWorld = (ServerWorld) world;



            BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
            if (blockEntity instanceof SignBlockEntity)
                GraveHandler.Interact((ServerPlayerEntity) player, (ServerWorld) world, (SignBlockEntity) blockEntity);

            return ActionResult.PASS;
        }
    }

    @Override
    public void onInitializeServer()
    {
        LOGGER.log(Level.INFO, "Initialized {} version {}", MOD_NAME, VERSION);

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> GravesCommand.register(dispatcher));
        PlayerBlockBreakEvents.AFTER.register(new PlayerBlockBreakEvent());
        UseBlockCallback.EVENT.register(new PlayerUseBlockEvent());
    }
}
