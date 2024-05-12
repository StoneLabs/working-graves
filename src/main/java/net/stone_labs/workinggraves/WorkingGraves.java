package net.stone_labs.workinggraves;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.stone_labs.workinggraves.commands.GravesCommand;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Objects;

public class WorkingGraves implements ModInitializer
{
    public static class Settings
    {
        public static boolean requireSoulTorch = true;
        public static boolean doLightningFire = true;
        public static boolean graveInAllDimensions = true;
    }

    public static final Logger LOGGER = LogManager.getLogger();
    public static final PermissionManager PERMISSION_MANAGER = PermissionManager.instance();

    public static final String MOD_ID = "workinggraves";
    public static final String MOD_NAME = "Working Graves";
    public static final String VERSION = "1.7.0";

    public static class PlayerUseBlockEvent implements UseBlockCallback
    {
        @Override
        public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult)
        {
            if (world instanceof ServerWorld)
            {
                BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
                if (blockEntity instanceof SignBlockEntity)
                    GraveHandler.Interact((ServerPlayerEntity) player, (ServerWorld) world, (SignBlockEntity) blockEntity);
            }
            return ActionResult.PASS;
        }
    }

    @Override
    public void onInitialize()
    {
        LOGGER.log(Level.INFO, "Initialized {} version {}", MOD_NAME, VERSION);

        // Register command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> GravesCommand.register(dispatcher));

        // Register Right click handler
        UseBlockCallback.EVENT.register(new PlayerUseBlockEvent());

        // Set values from gamerules on server start
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            WorkingGraves.Settings.requireSoulTorch = server.getGameRules().get(REQUIRE_SOUL_TORCH).get();
            PERMISSION_MANAGER.setPermissionLevel(PermissionManager.Permission.NEW, server.getGameRules().get(REQUIRED_PERMISSION_LEVEL).get());
            WorkingGraves.Settings.doLightningFire = server.getGameRules().get(DO_LIGHTNING_FIRE).get();
            WorkingGraves.Settings.graveInAllDimensions = server.getGameRules().get(GRAVE_IN_ALL_DIMENSIONS).get();
        });
    }

    public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_SOUL_TORCH = register("gravesRequireSoulTorch", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) ->
    {
        WorkingGraves.Settings.requireSoulTorch = rule.get();
    }));
    public static final GameRules.Key<GameRules.IntRule> REQUIRED_PERMISSION_LEVEL = register("gravesRequiredPermissionLevel", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0, (server, rule) ->
    {
        PERMISSION_MANAGER.setPermissionLevel(PermissionManager.Permission.NEW, rule.get());
    }));
    public static final GameRules.Key<GameRules.BooleanRule> DO_LIGHTNING_FIRE = register("gravesDoLightningFire", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) ->
    {
        WorkingGraves.Settings.doLightningFire = rule.get();
    }));
    public static final GameRules.Key<GameRules.BooleanRule> GRAVE_IN_ALL_DIMENSIONS = register("graveInAllDimensions", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) ->
    {
        WorkingGraves.Settings.graveInAllDimensions = rule.get();
    }));

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type)
    {
        return GameRuleRegistry.register(name, category, type);
    }
}
