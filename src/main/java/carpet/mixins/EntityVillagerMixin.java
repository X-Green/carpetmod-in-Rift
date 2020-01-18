package carpet.mixins;

import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVillager.class)
public interface EntityVillagerMixin
{
    @Accessor("wealth")
    int getwealth();
}
