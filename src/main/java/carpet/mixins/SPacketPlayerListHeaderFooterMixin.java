package carpet.mixins;

import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerListHeaderFooter.class)
public interface SPacketPlayerListHeaderFooterMixin
{
    @Accessor("header")
    void setheader(ITextComponent header);

    @Accessor("footer")
    void setfooter(ITextComponent footer);
}