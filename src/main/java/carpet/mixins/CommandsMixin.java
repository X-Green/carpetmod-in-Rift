package carpet.mixins;

import carpet.CarpetServer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.command.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public class CommandsMixin
{
    @Final
    @Shadow
    private CommandDispatcher<CommandSource> dispatcher;

    @Inject(method = "<init>", at = @At(value = "RETURN")
    )
    private void initCarpetCommands(boolean isDedicatedServer, CallbackInfo ci)
    {
        CarpetServer.registerCarpetCommands(this.dispatcher);
    }
}
