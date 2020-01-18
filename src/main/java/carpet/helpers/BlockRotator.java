package carpet.helpers;

import carpet.settings.CarpetSettings;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockRotator
{
    public static boolean flipBlockWithCactus(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!playerIn.abilities.allowEdit || !CarpetSettings.flippinCactus || !player_holds_cactus_mainhand(playerIn))
        {
            return false;
        }
        return flip_block(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    public static IBlockState alternativeBlockPlacement(Block block, BlockItemUseContext context)//World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        //actual alternative block placement code
        //
        EnumFacing facing;
        float hitX = context.getHitX();
        EntityPlayer placer = context.getPlayer();
        BlockPos pos = context.getPos();
        World world = context.getWorld();

        if (block instanceof BlockGlazedTerracotta)
        {
            facing = EnumFacing.byIndex((int)hitX - 2);
            if(facing == EnumFacing.UP || facing == EnumFacing.DOWN)
            {
                facing = placer.getHorizontalFacing().getOpposite();
            }
            return block.getDefaultState().with(BlockHorizontal.HORIZONTAL_FACING, facing);
        }
        else if (block instanceof BlockObserver)
        {
            return block.getDefaultState()
                    .with(BlockDirectional.FACING, EnumFacing.byIndex((int)hitX - 2))
                    .with(BlockObserver.POWERED, true);
        }
        else if (block instanceof BlockRedstoneRepeater)
        {
            facing = EnumFacing.byIndex((((int)hitX) % 10) - 2);
            if(facing == EnumFacing.UP || facing == EnumFacing.DOWN)
            {
                facing = placer.getHorizontalFacing().getOpposite();
            }
            return block.getDefaultState()
                    .with(BlockHorizontal.HORIZONTAL_FACING, facing)
                    .with(BlockRedstoneRepeater.DELAY, MathHelper.clamp((((int) hitX) / 10) + 1, 1, 4))
                    .with(BlockRedstoneRepeater.LOCKED, Boolean.FALSE);
        }
        else if (block instanceof BlockTrapDoor)
        {
            return block.getDefaultState()
                    .with(BlockTrapDoor.HORIZONTAL_FACING, EnumFacing.byIndex((((int)hitX) % 10) - 2))
                    .with(BlockTrapDoor.OPEN, Boolean.FALSE)
                    .with(BlockTrapDoor.HALF, (hitX > 10) ? Half.TOP : Half.BOTTOM)
                    .with(BlockTrapDoor.OPEN, world.isBlockPowered(pos));
        }
        else if (block instanceof BlockRedstoneComparator)
        {
            facing = EnumFacing.byIndex((((int)hitX) % 10) - 2);
            if((facing == EnumFacing.UP) || (facing == EnumFacing.DOWN))
            {
                facing = placer.getHorizontalFacing().getOpposite();
            }
            ComparatorMode m = (hitX > 10)?ComparatorMode.SUBTRACT: ComparatorMode.COMPARE;
            return block.getDefaultState()
                    .with(BlockHorizontal.HORIZONTAL_FACING, facing)
                    .with(BlockRedstoneComparator.POWERED, Boolean.FALSE)
                    .with(BlockRedstoneComparator.MODE, m);
        }
        else if (block instanceof BlockDispenser)
        {
            return block.getDefaultState()
                    .with(BlockDispenser.FACING, EnumFacing.byIndex((int)hitX - 2))
                    .with(BlockDispenser.TRIGGERED, Boolean.FALSE);
        }
        else if (block instanceof BlockPistonBase)
        {
            return block.getDefaultState()
                    .with(BlockDirectional.FACING,EnumFacing.byIndex((int)hitX - 2) )
                    .with(BlockPistonBase.EXTENDED, Boolean.FALSE);
        }
        else if (block instanceof BlockStairs)
        {
            return block.getStateForPlacement(context)//worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                    .with(BlockStairs.FACING, EnumFacing.byIndex((((int)hitX) % 10) - 2))
                    .with(BlockStairs.HALF, ( hitX > 10)?Half.TOP : Half.BOTTOM);
        }
        return null;
    }

    public static ItemStack dispenserRotate(IBlockSource source, ItemStack stack)
    {
        EnumFacing sourceFace = source.getBlockState().get(BlockDispenser.FACING);
        World world = source.getWorld();
        BlockPos blockpos = source.getBlockPos().offset(sourceFace);
        IBlockState iblockstate = world.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        // Block rotation for blocks that can be placed in all 6 rotations.
        if(block instanceof BlockDirectional || block instanceof BlockDispenser)
        {
            EnumFacing face = iblockstate.get(BlockDirectional.FACING);
            face = face.rotateAround(sourceFace.getAxis());
            if(sourceFace.getIndex() % 2 == 0)
            {   // Rotate twice more to make blocks always rotate clockwise relative to the dispenser
                // when index is equal to zero. when index is equal to zero the dispenser is in the opposite direction.
                face = face.rotateAround(sourceFace.getAxis());
                face = face.rotateAround(sourceFace.getAxis());
            }
            world.setBlockState(blockpos, iblockstate.with(BlockDirectional.FACING, face), 3);

        }
        else if(block instanceof BlockHorizontal) // Block rotation for blocks that can be placed in only 4 horizontal rotations.
        {
            EnumFacing face = iblockstate.get(BlockHorizontal.HORIZONTAL_FACING);
            face = face.rotateAround(sourceFace.getAxis());
            if(sourceFace.getIndex() % 2 == 0)
            { // same as above.
                face = face.rotateAround(sourceFace.getAxis());
                face = face.rotateAround(sourceFace.getAxis());
            }
            if(sourceFace.getIndex() <= 1)
            {   // Make sure to suppress rotation when index is lower then 2 as that will result in a faulty rotation for
                // blocks that only can be placed horizontaly.
                world.setBlockState(blockpos, iblockstate.with(BlockHorizontal.HORIZONTAL_FACING, face), 3);
            }
        }
        // Send block update to the block that just have been rotated.
        world.neighborChanged(blockpos, block, source.getBlockPos());

        return stack;
    }




    public static boolean flip_block(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        Block block = state.getBlock();
        if ( (block instanceof BlockGlazedTerracotta) || (block instanceof BlockRedstoneDiode) || (block instanceof BlockRailBase) ||
             (block instanceof BlockTrapDoor)         || (block instanceof BlockFenceGate))
        {
            worldIn.setBlockState(pos, block.rotate(state, Rotation.CLOCKWISE_90), 2 | 1024);
        }
        else if ((block instanceof BlockObserver) || (block instanceof BlockEndRod))
        {
            worldIn.setBlockState(pos, state.with(BlockDirectional.FACING, (EnumFacing)state.get(BlockDirectional.FACING).getOpposite()), 2 | 1024);
        }
        else if (block instanceof BlockDispenser)
        {
            worldIn.setBlockState(pos, state.with(BlockDispenser.FACING, state.get(BlockDispenser.FACING).getOpposite()), 2 | 1024);
        }
        else if (block instanceof BlockPistonBase)
        {
            if (!(state.get(BlockPistonBase.EXTENDED)))
                worldIn.setBlockState(pos, state.with(BlockDirectional.FACING, state.get(BlockDirectional.FACING).getOpposite()), 2 | 1024);
        }
        else if (block instanceof BlockSlab)
        {
            if (!((BlockSlab) block).isFullCube(state))
            {
                worldIn.setBlockState(pos, state.with(BlockSlab.TYPE, state.get(BlockSlab.TYPE) == SlabType.TOP ? SlabType.BOTTOM : SlabType.TOP), 2 | 1024);
            }
        }
        else if (block instanceof BlockHopper)
        {
            if ((EnumFacing)state.get(BlockHopper.FACING) != EnumFacing.DOWN)
            {
                worldIn.setBlockState(pos, state.with(BlockHopper.FACING, state.get(BlockHopper.FACING).rotateY()), 2 | 1024);
            }
        }
        else if (block instanceof BlockStairs)
        {
            //LOG.error(String.format("hit with facing: %s, at side %.1fX, X %.1fY, Y %.1fZ",facing, hitX, hitY, hitZ));
            if ((facing == EnumFacing.UP && hitY == 1.0f) || (facing == EnumFacing.DOWN && hitY == 0.0f))
            {
                worldIn.setBlockState(pos, state.with(BlockStairs.HALF, state.get(BlockStairs.HALF) == Half.TOP ? Half.BOTTOM : Half.TOP ), 2 | 1024);
            }
            else
            {
                boolean turn_right;
                if (facing == EnumFacing.NORTH)
                {
                    turn_right = (hitX <= 0.5);
                }
                else if (facing == EnumFacing.SOUTH)
                {
                    turn_right = !(hitX <= 0.5);
                }
                else if (facing == EnumFacing.EAST)
                {
                    turn_right = (hitZ <= 0.5);
                }
                else if (facing == EnumFacing.WEST)
                {
                    turn_right = !(hitZ <= 0.5);
                }
                else
                {
                    return false;
                }
                if (turn_right)
                {
                    worldIn.setBlockState(pos, block.rotate(state, Rotation.COUNTERCLOCKWISE_90), 2 | 1024);
                }
                else
                {
                    worldIn.setBlockState(pos, block.rotate(state, Rotation.CLOCKWISE_90), 2 | 1024);
                }
            }
        }
        else
        {
            return false;
        }
        worldIn.markBlockRangeForRenderUpdate(pos, pos);
        return true;
    }
    private static boolean player_holds_cactus_mainhand(EntityPlayer playerIn)
    {
        return (!playerIn.getHeldItemMainhand().isEmpty()
                && playerIn.getHeldItemMainhand().getItem() instanceof ItemBlock &&
                ((ItemBlock) (playerIn.getHeldItemMainhand().getItem())).getBlock() == Blocks.CACTUS);
    }
    public static boolean flippinEligibility(Entity entity)
    {
        if (CarpetSettings.flippinCactus
                && (entity instanceof EntityPlayer))
        {
            EntityPlayer player = (EntityPlayer)entity;
            return (!player.getHeldItemOffhand().isEmpty()
                    && player.getHeldItemOffhand().getItem() instanceof ItemBlock &&
                    ((ItemBlock) (player.getHeldItemOffhand().getItem())).getBlock() == Blocks.CACTUS);
        }
        return false;
    }
}
