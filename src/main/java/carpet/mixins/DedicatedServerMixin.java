package carpet.mixins;

import carpet.CarpetServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin
{
    @Inject(method = "init", at = @At(value = "RETURN")
    )
    private void onSetupServer(CallbackInfoReturnable<Boolean> cir)
    {
        //CM init - all stuff loaded from the server, just before worlds loading
        CarpetServer.onServerLoaded((MinecraftServer)(Object) this);
        //CM start game hook
        CarpetServer.onGameStarted();
    }
}
