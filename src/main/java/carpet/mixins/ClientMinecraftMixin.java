package carpet.mixins;

import carpet.CarpetServer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientMinecraftMixin
{
    @Inject(method = "init", at = @At(value = "RETURN")
    )
    private void onInit(CallbackInfo ci) {
        //CM start game hook
        CarpetServer.onGameStarted();
    }
}

