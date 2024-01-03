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
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.stone_labs.workinggraves.commands.GravesCommand;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorkingGraves implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger();

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
            GraveHandler.requireSoulTorch = server.getGameRules().get(REQUIRE_SOUL_TORCH).get();
            GraveHandler.requiredPermissionLevel = server.getGameRules().get(REQUIRED_PERMISSION_LEVEL).get();
            Grave.doLightningFire = server.getGameRules().get(DO_LIGHTNING_FIRE).get();
        });
    }

    public static final GameRules.Key<GameRules.BooleanRule> REQUIRE_SOUL_TORCH = register("gravesRequireSoulTorch", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) ->
    {
        GraveHandler.requireSoulTorch = rule.get();
    }));
    public static final GameRules.Key<GameRules.IntRule> REQUIRED_PERMISSION_LEVEL = register("gravesRequiredPermissionLevel", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0, (server, rule) ->
    {
        GraveHandler.requiredPermissionLevel = rule.get();
    }));
    public static final GameRules.Key<GameRules.BooleanRule> DO_LIGHTNING_FIRE = register("gravesDoLightningFire", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true, (server, rule) ->
    {
        Grave.doLightningFire = rule.get();
    }));

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type)
    {
        return GameRuleRegistry.register(name, category, type);
    }
}
