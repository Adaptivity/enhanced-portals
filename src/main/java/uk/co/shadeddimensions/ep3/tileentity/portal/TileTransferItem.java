package uk.co.shadeddimensions.ep3.tileentity.portal;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import uk.co.shadeddimensions.ep3.network.GuiHandler;
import uk.co.shadeddimensions.ep3.util.WorldUtils;
import uk.co.shadeddimensions.library.util.ItemHelper;
import cpw.mods.fml.common.network.ByteBufUtils;

public class TileTransferItem extends TileFrameTransfer implements IInventory//, IPeripheral
{
    ItemStack stack;
    
    @Override
    public boolean activate(EntityPlayer player, ItemStack stack)
    {
    	if (player.isSneaking())
		{
			return false;
		}
    	
        TileController controller = getPortalController();

        if (ItemHelper.isWrench(stack) && controller != null && controller.isFinalized())
        {
            GuiHandler.openGui(player, this, GuiHandler.TRANSFER_ITEM);
            return true;
        }
        
        return false;
    }
    
    @Override
    public void packetGui(NBTTagCompound tag, EntityPlayer player)
    {
        if (tag.hasKey("state"))
        {
            isSending = !isSending;
        }
    }
    
    @Override
    public void packetGuiFill(ByteBuf buffer)
    {
        ByteBufUtils.writeItemStack(buffer, stack);
    }
    
    @Override
    public void packetGuiUse(ByteBuf buffer)
    {
        stack = ByteBufUtils.readItemStack(buffer);
    }

    @Override
    public int getSizeInventory()
    {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return stack;
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        ItemStack stack = getStackInSlot(i);

        if (stack != null)
        {
            if (stack.stackSize <= j)
            {
                setInventorySlotContents(i, null);
            }
            else
            {
                stack = stack.splitStack(j);

                if (stack.stackSize == 0)
                {
                    setInventorySlotContents(i, null);
                }
            }
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return stack;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        stack = itemstack;
    }

    @Override
    public String getInventoryName()
    {
        return "tile.frame.item.name";
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return true;
    }
    
    int tickTimer = 20, time = 0;
    
    @Override
    public void updateEntity()
    {
        super.updateEntity();
        
        if (!worldObj.isRemote)
        {
            if (isSending)
            {
                if (time >= tickTimer)
                {
                    time = 0;
                    
                    TileController controller = getPortalController();
                    
                    if (controller != null && controller.isPortalActive() && stack != null)
                    {
                        TileController exitController =  (TileController) controller.getDestinationLocation().getTileEntity();
                        
                        if (exitController != null)
                        {
                            for (ChunkCoordinates c : exitController.getTransferItems())
                            {
                                TileEntity tile = WorldUtils.getTileEntity(exitController.getWorldObj(), c);
                                
                                if (tile != null && tile instanceof TileTransferItem)
                                {
                                    TileTransferItem item = (TileTransferItem) tile;
                                    
                                    if (!item.isSending)
                                    {
                                        if (item.getStackInSlot(0) == null)
                                        {
                                            item.setInventorySlotContents(0, stack);
                                            item.markDirty();
                                            stack = null;
                                            markDirty();
                                        }
                                    }
                                }
                                
                                if (stack == null)
                                {
                                    break;
                                }
                            }
                        }
                    }
                }
                
                time++;
            }
        }
    }
    
    /*@Override
    public String getType()
    {
        return "ITM";
    }

    @Override
    public String[] getMethodNames()
    {
        return new String[] { "getItemStored", "getAmountStored", "hasStack", "isSending" };
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
    {
        if (method == 0)
        {
            return new Object[] { stack != null ? stack.getItem() : 0 }; // TODO
        }
        else if (method == 1)
        {
            return new Object[] { stack != null ? stack.stackSize : 0 };
        }
        else if (method == 2)
        {
            return new Object[] { stack != null };
        }
        else if (method == 3)
        {
            return new Object[] { isSending };
        }
        
        return null;
    }

    @Override
    public boolean canAttachToSide(int side)
    {
        return true;
    }

    @Override
    public void attach(IComputerAccess computer)
    {
        
    }

    @Override
    public void detach(IComputerAccess computer)
    {
        
    }*/

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public void openInventory()
    {
        
    }

    @Override
    public void closeInventory()
    {
        
    }
}
