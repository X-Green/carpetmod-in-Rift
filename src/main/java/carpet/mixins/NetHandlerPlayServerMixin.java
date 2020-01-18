package carpet.mixins;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import carpet.helpers.TickSpeed;

@Mixin(NetHandlerPlayServer.class)
public class NetHandlerPlayServerMixin
{
    @Inject(method = "processInput", at = @At(value = "RETURN")
    )
    private void onTickSpeedingProcessInput(CPacketInput packetIn, CallbackInfo ci)
    {
        if (packetIn.getStrafeSpeed() != 0.0F || packetIn.getForwardSpeed() != 0.0F || packetIn.isJumping() || packetIn.isSneaking())
        {
            TickSpeed.reset_player_active_timeout();
        }
    }

    @Inject(method = "processPlayer", at = @At(value = "RETURN")
    )
    private void onTickSpeedingProcessPlayer(CPacketPlayer packetIn, CallbackInfo ci)
    {
        TickSpeed.reset_player_active_timeout();
    }
}
