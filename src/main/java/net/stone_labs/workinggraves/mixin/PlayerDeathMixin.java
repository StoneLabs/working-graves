package net.stone_labs.workinggraves.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.stone_labs.workinggraves.GraveManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin
{
    @Inject(at = @At(value = "HEAD"), method = "onDeath", require = 1)
    @SuppressWarnings("ConstantConditions")
    private void onPlayerDeath(DamageSource source, CallbackInfo info)
    {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        GraveManager.GravePlayerInAll(player);
    }
}