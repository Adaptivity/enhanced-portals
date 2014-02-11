package uk.co.shadeddimensions.ep3.tileentity;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import uk.co.shadeddimensions.ep3.EnhancedPortals;
import uk.co.shadeddimensions.ep3.block.BlockStabilizer;
import uk.co.shadeddimensions.ep3.item.ItemLocationCard;
import uk.co.shadeddimensions.ep3.network.CommonProxy;
import uk.co.shadeddimensions.ep3.network.GuiHandler;
import uk.co.shadeddimensions.ep3.network.packet.PacketTileUpdate;
import uk.co.shadeddimensions.ep3.portal.GlyphIdentifier;
import uk.co.shadeddimensions.ep3.portal.PortalException;
import uk.co.shadeddimensions.ep3.tileentity.portal.TileController;
import uk.co.shadeddimensions.ep3.util.GeneralUtils;
import uk.co.shadeddimensions.ep3.util.PortalTextureManager;
import uk.co.shadeddimensions.library.util.ItemHelper;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileStabilizerMain extends TileEP implements IInventory
{
	static final int ACTIVE_PORTALS_PER_ROW = 2, ENERGY_STORAGE_PER_ROW = CommonProxy.REDSTONE_FLUX_COST + CommonProxy.REDSTONE_FLUX_COST / 2;

	ArrayList<ChunkCoordinates> blockList = new ArrayList<ChunkCoordinates>();

	HashMap<String, String> activeConnections = new HashMap<String, String>();
	HashMap<String, String> activeConnectionsReverse = new HashMap<String, String>();

	ItemStack inventory;
	int rows, tickTimer;
	EnergyStorage energyStorage = new EnergyStorage(0);
	public int powerState, instability = 0;
	Random rand = new Random();
	public boolean is3x3 = false;

	@SideOnly(Side.CLIENT)
	public int intActiveConnections;

	public boolean activate(EntityPlayer player)
	{
		ItemStack stack = player.inventory.getCurrentItem();

		if (stack != null && stack.getItem() instanceof ItemBlock && Block.getBlockFromItem(stack.getItem()) == BlockStabilizer.instance)
		{
			return false;
		}

		GuiHandler.openGui(player, this, GuiHandler.DIMENSIONAL_BRIDGE_STABILIZER);
		return true;
	}

	void addHighInstabilityEffects(Entity entity)
	{
		if (entity instanceof EntityLivingBase)
		{
			PotionEffect blindness = new PotionEffect(Potion.blindness.id, 600, 1);
			PotionEffect hunger = new PotionEffect(Potion.hunger.id, 600, 1);
			PotionEffect poison = new PotionEffect(Potion.poison.id, 600, 1);

			blindness.setCurativeItems(new ArrayList<ItemStack>());
			hunger.setCurativeItems(new ArrayList<ItemStack>());
			poison.setCurativeItems(new ArrayList<ItemStack>());

			((EntityLivingBase) entity).addPotionEffect(blindness);
			((EntityLivingBase) entity).addPotionEffect(hunger);
			((EntityLivingBase) entity).addPotionEffect(poison);
		}
	}

	void addLowInstabilityEffects(Entity entity)
	{
		if (entity instanceof EntityLivingBase)
		{
			PotionEffect blindness = new PotionEffect(Potion.blindness.id, 200, 1);
			PotionEffect hunger = new PotionEffect(Potion.hunger.id, 200, 1);
			PotionEffect poison = new PotionEffect(Potion.poison.id, 200, 1);

			blindness.setCurativeItems(new ArrayList<ItemStack>());
			hunger.setCurativeItems(new ArrayList<ItemStack>());
			poison.setCurativeItems(new ArrayList<ItemStack>());

			int effect = rand.nextInt(3);
			((EntityLivingBase) entity).addPotionEffect(effect == 0 ? blindness : effect == 1 ? hunger : poison);
		}
	}

	void addMediumInstabilityEffects(Entity entity)
	{
		if (entity instanceof EntityLivingBase)
		{
			PotionEffect blindness = new PotionEffect(Potion.blindness.id, 400, 1);
			PotionEffect hunger = new PotionEffect(Potion.hunger.id, 400, 1);
			PotionEffect poison = new PotionEffect(Potion.poison.id, 400, 1);

			blindness.setCurativeItems(new ArrayList<ItemStack>());
			hunger.setCurativeItems(new ArrayList<ItemStack>());
			poison.setCurativeItems(new ArrayList<ItemStack>());

			int effect = rand.nextInt(3);

			if (effect == 0)
			{
				((EntityLivingBase) entity).addPotionEffect(blindness);
				((EntityLivingBase) entity).addPotionEffect(hunger);
			}
			else if (effect == 1)
			{
				((EntityLivingBase) entity).addPotionEffect(blindness);
				((EntityLivingBase) entity).addPotionEffect(poison);
			}
			else
			{
				((EntityLivingBase) entity).addPotionEffect(poison);
				((EntityLivingBase) entity).addPotionEffect(hunger);
			}
		}
	}

	public void breakBlock(Block oldBlockID, int oldMetadata)
	{
		for (int i = activeConnections.size() - 1; i > -1; i--) // Go backwards so we don't get messed up by connections getting removed from this list
		{
			try
			{
				terminateExistingConnection(new GlyphIdentifier(activeConnections.values().toArray(new String[activeConnections.size()])[i]));
			}
			catch (Exception e)
			{

			}
		}

		for (ChunkCoordinates c : blockList)
		{
			TileEntity tile = worldObj.getTileEntity(c.posX, c.posY, c.posZ);

			if (tile instanceof TileStabilizer)
			{
				TileStabilizer t = (TileStabilizer) tile;
				t.mainBlock = null;
				EnhancedPortals.packetPipeline.sendToAllAround(new PacketTileUpdate(this), this);
			}
		}
	}

	/***
	 * Whether or not this stabilizer can create a new connection
	 */
	public boolean canAcceptNewConnection()
	{
		return activeConnections.size() * 2 + 2 <= ACTIVE_PORTALS_PER_ROW * rows;
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	public void deconstruct()
	{
		breakBlock(null, 0);
		worldObj.setBlock(xCoord, yCoord, zCoord, BlockStabilizer.instance, 0, 3);
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

	public int getActiveConnections()
	{
		return activeConnections != null ? activeConnections.size() : -1;
	}

	public GlyphIdentifier getConnectedPortal(GlyphIdentifier uniqueIdentifier)
	{
		if (activeConnections.containsKey(uniqueIdentifier.getGlyphString()))
		{
			return new GlyphIdentifier(activeConnections.get(uniqueIdentifier.getGlyphString()));
		}
		else if (activeConnectionsReverse.containsKey(uniqueIdentifier.getGlyphString()))
		{
			return new GlyphIdentifier(activeConnectionsReverse.get(uniqueIdentifier.getGlyphString()));
		}

		return null;
	}

	public EnergyStorage getEnergyStorage()
	{
		return energyStorage;
	}

	public int getEnergyStoragePerRow()
	{
		return is3x3 ? ENERGY_STORAGE_PER_ROW + ENERGY_STORAGE_PER_ROW / 2 : ENERGY_STORAGE_PER_ROW;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return inventory;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return inventory;
	}

	/***
	 * Gets whether or not this stabilizer has enough power to keep the portal open for at least one second.
	 */
	public boolean hasEnoughPowerToStart()
	{
	    return true;
		//if (CommonProxy.redstoneFluxPowerMultiplier == 0)
		//{
		//	return true;
		//}

		//int powerRequirement = CommonProxy.redstoneFluxPowerMultiplier * 1 * CommonProxy.REDSTONE_FLUX_COST;
		//return extractEnergy(null, (int) (powerRequirement * 0.3), true) == (int) (powerRequirement * 0.3);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return ItemHelper.isEnergyContainerItem(itemstack);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void packetGui(NBTTagCompound tag, EntityPlayer player)
	{
		if (tag.hasKey("energy"))
		{
			energyStorage.setEnergyStored(tag.getInteger("energy"));
		}
		else if (tag.hasKey("button"))
		{
			powerState++;

			if (powerState >= 4)
			{
				powerState = 0;
			}
		}
	}

	@Override
	public void packetGuiFill(ByteBuf buffer)
	{
	    buffer.writeInt(activeConnections.size());
	    buffer.writeInt(powerState);
	    buffer.writeInt(instability);
	    buffer.writeInt(energyStorage.getMaxEnergyStored());
	    buffer.writeInt(energyStorage.getEnergyStored());
	}

	@Override
	public void packetGuiUse(ByteBuf buffer)
	{
		intActiveConnections = buffer.readInt();
		powerState = buffer.readInt();
		instability = buffer.readInt();
		energyStorage.setCapacity(buffer.readInt());
		energyStorage.setEnergyStored(buffer.readInt());
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		powerState = tag.getInteger("powerState");
		is3x3 = tag.getBoolean("is3x3");
		rows = tag.getInteger("rows");
		energyStorage = new EnergyStorage(rows * getEnergyStoragePerRow());
		blockList = GeneralUtils.loadChunkCoordList(tag, "blockList");
		energyStorage.readFromNBT(tag);

		if (tag.hasKey("activeConnections"))
		{
			NBTTagList c = tag.getTagList("activeConnections", 9);

			for (int i = 0; i < c.tagCount(); i++)
			{
				//NBTTagCompound t = (NBTTagCompound) c.tagAt(i);
				//String A = t.getString("Key"), B = t.getString("Value");

				//activeConnections.put(A, B);
				//activeConnectionsReverse.put(B, A);
			}
		}

		if (tag.hasKey("inventory"))
		{
			inventory = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("inventory"));
		}
	}

	/***
	 * Removes a connection from the active list.
	 */
	public void removeExistingConnection(GlyphIdentifier portalA, GlyphIdentifier portalB)
	{
		activeConnections.remove(portalA.getGlyphString());
		activeConnections.remove(portalB.getGlyphString());
		activeConnectionsReverse.remove(portalA.getGlyphString());
		activeConnectionsReverse.remove(portalB.getGlyphString());

		if (activeConnections.size() == 0 && powerState == 0 && instability > 0)
		{
			setInstability(0);
		}
	}

	public void setData(ArrayList<ChunkCoordinates> blocks, int rows2, boolean is3)
	{
		is3x3 = is3;
		rows = rows2;
		blockList = blocks;
		energyStorage = new EnergyStorage(rows * getEnergyStoragePerRow());
		EnhancedPortals.packetPipeline.sendToAllAround(new PacketTileUpdate(this), this);
	}

	void setInstability(int newInstability)
	{
		if (newInstability == instability)
		{
			return;
		}

		instability = newInstability;

		for (Entry<String, String> pair : activeConnections.entrySet())
		{
			TileController controllerA = CommonProxy.networkManager.getPortalController(new GlyphIdentifier(pair.getKey()));
			TileController controllerB = CommonProxy.networkManager.getPortalController(new GlyphIdentifier(pair.getValue()));

			if (controllerA != null)
			{
				controllerA.setInstability(newInstability);
			}

			if (controllerB != null)
			{
				controllerB.setInstability(newInstability);
			}
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		inventory = itemstack;
	}

	/***
	 * Sets up a new connection between two portals.
	 * 
	 * @return True if connection was successfully established.
	 */
	public boolean setupNewConnection(GlyphIdentifier portalA, GlyphIdentifier portalB, PortalTextureManager textureManager) throws PortalException
	{
		if (activeConnections.containsKey(portalA.getGlyphString()))
		{
			throw new PortalException("diallingPortalAlreadyActive");
		}
		else if (activeConnections.containsValue(portalB.getGlyphString()))
		{
			throw new PortalException("receivingPortalAlreadyActive");
		}
		else if (!hasEnoughPowerToStart())
		{
			throw new PortalException("notEnoughPowerToStart");
		}
		else if (!canAcceptNewConnection())
		{
			throw new PortalException("maxedConnectionLimit");
		}
		else if (portalA.equals(portalB))
		{
			throw new PortalException("cannotDialDiallingPortal");
		}
		else if (!CommonProxy.networkManager.portalIdentifierExists(portalA))
		{
			throw new PortalException("noPortalWithThatIdentifierSending");
		}
		else if (!CommonProxy.networkManager.portalIdentifierExists(portalB))
		{
			throw new PortalException("noPortalWithThatIdentifierReceiving");
		}

		TileController cA = CommonProxy.networkManager.getPortalController(portalA), cB = CommonProxy.networkManager.getPortalController(portalB);

		if (cA == null)
		{
			throw new PortalException("diallingPortalNotFound");
		}
		else if (cB == null)
		{
			throw new PortalException("receivingPortalNotFound");
		}
		else if (cA.isPortalActive())
		{
			throw new PortalException("diallingPortalAlreadyActive");
		}
		else if (cB.isPortalActive()) // Make sure both portals are inactive
		{
			throw new PortalException("receivingPortalAlreadyActive");
		}
		else if (!cA.isFinalized())
		{
			throw new PortalException("sendingPortalNotInitialized");
		}
		else if (!cB.isFinalized()) // Make sure they're set up correctly...
		{
			throw new PortalException("receivingPortalNotInitialized");
		}
		else if (!cA.getDimensionalBridgeStabilizer().getWorldCoordinates().equals(cB.getDimensionalBridgeStabilizer().getWorldCoordinates())) // And make sure they're on the same DBS. We're getting the tile instead of the worldcoordinates to make sure the DBS hasn't expanded
		{
			throw new PortalException("notOnSameStabilizer");
		}
		else if (cA.getDiallingDeviceCount() > 0 && cB.getNetworkInterfaceCount() > 0)
		{
			throw new PortalException("receivingPortalNoDialler");
		}
		else if (cA.getNetworkInterfaceCount() > 0 && cB.getDiallingDeviceCount() > 0)
		{
			throw new PortalException("sendingPortalNoDialler"); // Should never happen but it doesn't hurt to check.
		}

		if (textureManager != null)
		{
			cA.swapTextureData(textureManager);
			cB.swapTextureData(textureManager);
		}

		try
		{
			cA.setInstability(instability);
			cA.portalCreate();
			cA.cacheDestination(portalB, cB.getWorldCoordinates());
		}
		catch (PortalException e)
		{
			cA.revertTextureData();
			cB.revertTextureData();
			throw new PortalException("sendingPortalFailedToCreatePortal");
		}

		try
		{
			cB.setInstability(instability);
			cB.portalCreate();
			cB.cacheDestination(portalA, cA.getWorldCoordinates());
		}
		catch (PortalException e)
		{
			cA.portalRemove();
			cA.cacheDestination(null, null);
			cA.revertTextureData();
			cB.revertTextureData();
			throw new PortalException("receivingPortalFailedToCreatePortal");
		}

		activeConnections.put(portalA.getGlyphString(), portalB.getGlyphString());
		activeConnectionsReverse.put(portalB.getGlyphString(), portalA.getGlyphString());
		return true;
	}

	/***
	 * Terminates both portals and removes them from the active connection list. Used by dialling devices when the exit location is not known by the controller.
	 */
	public void terminateExistingConnection(GlyphIdentifier identifier) throws PortalException
	{
		if (identifier == null || identifier.isEmpty())
		{
			throw new PortalException("No identifier found for first portal");
		}

		GlyphIdentifier portalA = new GlyphIdentifier(identifier), portalB = null;

		if (activeConnections.containsKey(identifier.getGlyphString()))
		{
			portalB = new GlyphIdentifier(activeConnections.get(identifier.getGlyphString()));
		}
		else if (activeConnectionsReverse.containsKey(identifier.getGlyphString()))
		{
			portalB = new GlyphIdentifier(activeConnectionsReverse.get(identifier.getGlyphString()));
		}

		terminateExistingConnection(portalA, portalB);
	}

	/***
	 * Terminates both portals and removes them from the active connection list.
	 */
	public void terminateExistingConnection(GlyphIdentifier portalA, GlyphIdentifier portalB) throws PortalException
	{
		if (portalA == null)
		{
			throw new PortalException("No identifier found for first portal");
		}
		else if (portalB == null)
		{
			throw new PortalException("No identifier found for second portal");
		}

		TileController cA = CommonProxy.networkManager.getPortalController(portalA), cB = CommonProxy.networkManager.getPortalController(portalB);

		if (cA == null || cB == null)
		{
			if (cA == null)
			{
				throw new PortalException("No identifier found for first portal");
			}
			else if (cB == null)
			{
				throw new PortalException("No identifier found for second portal");
			}

			if (cA != null)
			{
				cA.portalRemove();
				cA.revertTextureData();
				cA.cacheDestination(null, null);
			}

			if (cB != null)
			{
				cB.portalRemove();
				cB.revertTextureData();
				cB.cacheDestination(null, null);
			}

			removeExistingConnection(portalA, portalB);
		}
		else if (activeConnections.containsKey(portalA.getGlyphString()) && activeConnections.get(portalA.getGlyphString()).equals(portalB.getGlyphString()) || activeConnectionsReverse.containsKey(portalA.getGlyphString()) && activeConnectionsReverse.get(portalA.getGlyphString()).equals(portalB.getGlyphString()))
		{
			// Make sure we're terminating the correct connection, also don't mind that we're terminating it from the other side that we started it from
			cA.portalRemove();
			cB.portalRemove();
			cA.cacheDestination(null, null);
			cB.cacheDestination(null, null);
			cA.revertTextureData();
			cB.revertTextureData();

			removeExistingConnection(portalA, portalB);
		}
		else
		{
			throw new PortalException("Could not find both portals in the active connection list.");
		}
	}

	@Override
	public void updateEntity()
	{
		if (activeConnections.size() > 0 && CommonProxy.redstoneFluxPowerMultiplier > 0 && tickTimer >= CommonProxy.REDSTONE_FLUX_TIMER)
		{
			int powerRequirement = CommonProxy.redstoneFluxPowerMultiplier * activeConnections.size() * CommonProxy.REDSTONE_FLUX_COST;

			if (powerState == 0 /*&& extractEnergy(null, powerRequirement, true) == powerRequirement*/) // Simulate the full power requirement
			{
				//extractEnergy(null, powerRequirement, false);
				setInstability(0);
			}
			else if ((powerState == 1 || powerState == 0) /*&& extractEnergy(null, (int) (powerRequirement * 0.8), true) == (int) (powerRequirement * 0.8)*/) // Otherwise, try it at 80%
			{
				//extractEnergy(null, (int) (powerRequirement * 0.8), false);
				setInstability(20);
			}
			else if ((powerState == 2 || powerState == 0) /*&& extractEnergy(null, (int) (powerRequirement * 0.5), true) == (int) (powerRequirement * 0.5)*/) // Otherwise, try it at 50%
			{
				//extractEnergy(null, (int) (powerRequirement * 0.5), false);
				setInstability(50);
			}
			else if ((powerState == 3 || powerState == 0) /*&& extractEnergy(null, (int) (powerRequirement * 0.3), true) == (int) (powerRequirement * 0.3)*/) // Otherwise, try it at 30%
			{
				//extractEnergy(null, (int) (powerRequirement * 0.3), false);
				setInstability(70);
			}
			else
			{
				for (int i = activeConnections.size() - 1; i > -1; i--) // Go backwards so we don't get messed up by connections getting removed from this list
				{
					try
					{
						terminateExistingConnection(new GlyphIdentifier(activeConnections.values().toArray(new String[activeConnections.size()])[i]));
					}
					catch (PortalException e)
					{
						CommonProxy.logger.warn(e.getMessage());
					}
				}

				setInstability(0);
			}

			tickTimer = -1;
		}

		if (inventory != null)
		{
			if (inventory.getItem() instanceof IEnergyContainerItem)
			{
				int requiredEnergy = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();

				if (requiredEnergy > 0 && ((IEnergyContainerItem) inventory.getItem()).getEnergyStored(inventory) > 0)
				{
					energyStorage.receiveEnergy(((IEnergyContainerItem) inventory.getItem()).extractEnergy(inventory, Math.min(requiredEnergy, 10000), false), false);
				}
			}
			else if (inventory.isItemEqual(new ItemStack(ItemLocationCard.instance)))
			{
				if (!ItemLocationCard.hasDBSLocation(inventory) && !worldObj.isRemote)
				{
					ItemLocationCard.setDBSLocation(inventory, getWorldCoordinates());
				}
			}
		}

		tickTimer++;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		energyStorage.writeToNBT(tag);
		tag.setBoolean("is3x3", is3x3);
		tag.setInteger("powerState", powerState);
		tag.setInteger("rows", rows);
		GeneralUtils.saveChunkCoordList(tag, blockList, "blockList");

		if (!activeConnections.isEmpty())
		{
			NBTTagList c = new NBTTagList();

			for (Entry<String, String> entry : activeConnections.entrySet())
			{
				NBTTagCompound t = new NBTTagCompound();
				t.setString("Key", entry.getKey());
				t.setString("Value", entry.getValue());
				c.appendTag(t);
			}

			tag.setTag("activeConnections", c);
		}

		if (inventory != null)
		{
			NBTTagCompound t = new NBTTagCompound();
			inventory.writeToNBT(t);
			tag.setTag("inventory", t);
		}
	}

    @Override
    public String getInventoryName()
    {
        return "tile.stabilizer.name";
    }

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
