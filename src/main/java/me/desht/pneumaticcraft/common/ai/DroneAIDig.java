package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class DroneAIDig extends DroneAIBlockInteraction {

    /**
     * @param drone the drone
     * @param widget needs to implement IBlockOrdered
     */
    public DroneAIDig(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        IBlockState blockState = worldCache.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!worldCache.isAirBlock(pos) && !ignoreBlock(block)) {
            NonNullList<ItemStack> droppedStacks = NonNullList.create();
            if (block.canSilkHarvest(drone.world(), pos, blockState, drone.getFakePlayer())) {
                droppedStacks.add(getSilkTouchBlock(block, blockState));
            } else {
                block.getDrops(droppedStacks, drone.world(), pos, blockState, 0);
            }
            for (ItemStack droppedStack : droppedStacks) {
                if (widget.isItemValidForFilters(droppedStack, blockState)) {
                    swapBestItemToFirstSlot(pos);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    private void swapBestItemToFirstSlot(BlockPos pos) {
        int bestSlot = 0;
        float bestSoftness = Float.MIN_VALUE;
        ItemStack oldCurrentStack = drone.getInv().getStackInSlot(0).copy();
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            drone.getInv().setStackInSlot(0, drone.getInv().getStackInSlot(i));
            float softness = worldCache.getBlockState(pos).getPlayerRelativeBlockHardness(drone.getFakePlayer(), drone.world(), pos);
            if (softness > bestSoftness) {
                bestSlot = i;
                bestSoftness = softness;
            }
        }
        drone.getInv().setStackInSlot(0, oldCurrentStack);
        if (bestSlot != 0) {
            ItemStack bestItem = drone.getInv().getStackInSlot(bestSlot).copy();
            drone.getInv().setStackInSlot(bestSlot, drone.getInv().getStackInSlot(0));
            drone.getInv().setStackInSlot(0, bestItem);
        }
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        PlayerInteractionManager manager = drone.getFakePlayer().interactionManager;
        if (!manager.isDestroyingBlock || !manager.receivedFinishDiggingPacket) { //is not destroying and is not acknowledged.
            IBlockState blockState = worldCache.getBlockState(pos);
            Block block = blockState.getBlock();
            if (!ignoreBlock(block) && isBlockValidForFilter(worldCache, drone, pos, widget)) {
                if (blockState.getBlockHardness(drone.world(), pos) < 0) {
                    addToBlacklist(pos);
                    drone.addDebugEntry("gui.progWidget.dig.debug.cantDigBlock", pos);
                    drone.setDugBlock(null);
                    return false;
                }
                manager.onBlockClicked(pos, EnumFacing.DOWN);
                manager.blockRemoving(pos);
                if (!manager.isDestroyingBlock) {
                    addToBlacklist(pos);
                    drone.addDebugEntry("gui.progWidget.dig.debug.cantDigBlock", pos);
                    drone.setDugBlock(null);
                    return false;
                }
                drone.setDugBlock(pos);
                return true;
            }
            drone.setDugBlock(null);
            return false;
        } else {
            return true;
        }
    }

    public static boolean isBlockValidForFilter(IBlockAccess worldCache, IDroneBase drone, BlockPos pos, ProgWidgetAreaItemBase widget) {
        IBlockState blockState = worldCache.getBlockState(pos);
        Block block = blockState.getBlock();

        if (!block.isAir(blockState, worldCache, pos)) {
            NonNullList<ItemStack> droppedStacks = NonNullList.create();
            if (block.canSilkHarvest(drone.world(), pos, blockState, drone.getFakePlayer())) {
                droppedStacks.add(getSilkTouchBlock(block, blockState));
            } else {
                block.getDrops(droppedStacks, drone.world(), pos, blockState, 0);
            }
            for (ItemStack droppedStack : droppedStacks) {
                if (widget.isItemValidForFilters(droppedStack, blockState)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    private static ItemStack getSilkTouchBlock(Block block, IBlockState state) {
        Item item = Item.getItemFromBlock(block);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        } else {
            return new ItemStack(item, 1, block.getMetaFromState(state));
        }
    }

    private static boolean ignoreBlock(Block block) {
        return PneumaticCraftUtils.isBlockLiquid(block);
    }

}
