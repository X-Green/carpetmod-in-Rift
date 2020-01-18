package carpet.mixins;

import net.minecraft.entity.monster.EntityZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityZombieVillager.class)
public interface EntityZombieVillagerMixin
{
    @Accessor("conversionTime")
    int getConversionTime();
}
