package carpet.mixins;

import net.minecraft.entity.monster.EntitySlime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntitySlime.class)
public interface EntitySlimeMixin
{
    @Invoker("getAttackStrength")
    int callgetAttackStrength();
}
