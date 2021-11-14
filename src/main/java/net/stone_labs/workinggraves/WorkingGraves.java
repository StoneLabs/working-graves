package net.stone_labs.workinggraves;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.GameRules;
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

        // Register command
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> GravesCommand.register(dispatcher));

        // Register Right click handler
        UseBlockCallback.EVENT.register(new PlayerUseBlockEvent());

        // Set values from gamerules on server start
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            GraveHandler.requireSoulTorch = server.getGameRules().get(REQUIRE_SOUL_TORCH).get();
        });
    }

    public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_SOUL_TORCH = register("gravesRequireSoulTorch", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) ->
    {
        GraveHandler.requireSoulTorch = rule.get();
    }));

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type)
    {
        return GameRuleRegistry.register(name, category, type);
    }
}
