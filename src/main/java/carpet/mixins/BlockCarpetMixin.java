package carpet.mixins;

import carpet.utils.WoolTool;
import net.minecraft.block.BlockCarpet;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.BlockItemUseContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockCarpet.class)
public class BlockCarpetMixin extends Block
{
    @Final
    @Shadow
    private EnumDyeColor color;

    public BlockCarpetMixin(Properties properties) {
        super(properties);
    }

    public IBlockState getStateForPlacement(BlockItemUseContext context)
    {
        IBlockState state = super.getStateForPlacement(context);
        if (context.getPlayer() != null && !context.getWorld().isRemote)
        {
            WoolTool.carpetPlacedAction(this.color, context.getPlayer(), context.getPos(), context.getWorld());
        }
        return state;
    }
}
