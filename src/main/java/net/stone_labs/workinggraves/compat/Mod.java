package net.stone_labs.workinggraves.compat;

import java.util.Optional;
import java.util.function.Supplier;

import net.fabricmc.loader.api.FabricLoader;
import net.stone_labs.workinggraves.compat.Trinkets.Trinkets;

public enum Mod
{
    TRINKETS("trinkets");

    public final String modID;

    Mod(String modID) {
        this.modID = modID;
    }

    public boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(this.modID);
    }

    public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
        if (isLoaded())
            return Optional.of(toRun.get().get());
        return Optional.empty();
    }

    public void executeIfInstalled(Supplier<Runnable> toExecute) {
        if (isLoaded()) {
            toExecute.get().run();
        }
    }
}