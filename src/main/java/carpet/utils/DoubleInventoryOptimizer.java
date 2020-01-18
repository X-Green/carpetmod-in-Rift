package carpet.utils;

import net.minecraft.item.ItemStack;

public class DoubleInventoryOptimizer extends InventoryOptimizer
{
    private final OptimizedInventory first;
    private int firstSize;
    private final OptimizedInventory second;
    private int secondSize;
    
    public DoubleInventoryOptimizer(OptimizedInventory first, OptimizedInventory second)
    {
        super(null);
        this.first = first;
        this.second = second;
        firstSize = first.getSizeInventory();
        secondSize = second.getSizeInventory();
    }
    
    @Override
    public void recalculate()
    {
        totalSlots = size();
        InventoryOptimizer firstOpto = first.getOptimizer();
        InventoryOptimizer secondOpto = second.getOptimizer();
        bloomFilter = firstOpto.bloomFilter | secondOpto.bloomFilter;
        firstFreeSlot = firstOpto.firstFreeSlot;
        if (firstFreeSlot < 0)
        {
            preEmptyBloomFilter = firstOpto.bloomFilter | secondOpto.preEmptyBloomFilter;
            firstFreeSlot = firstSize + secondOpto.firstFreeSlot;
            if (firstFreeSlot < firstSize)
                firstFreeSlot = -1;
        }
        else
        {
            preEmptyBloomFilter = firstOpto.preEmptyBloomFilter;
        }
        occupiedSlots = firstOpto.occupiedSlots + secondOpto.occupiedSlots;
        fullSlots = firstOpto.fullSlots + secondOpto.fullSlots;
    }
    
    @Override
    protected ItemStack getSlot(int index)
    {
        if (index < 0)
            return ItemStack.EMPTY;
        if (index < firstSize)
            return first.getStackInSlot(index);
        return second.getStackInSlot(index - firstSize);
    }
    
    @Override
    protected int size()
    {
        return firstSize + secondSize;
    }
}
