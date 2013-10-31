package uk.co.shadeddimensions.ep3.portal;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet43Experience;
import net.minecraft.network.packet.Packet9Respawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import uk.co.shadeddimensions.ep3.EnhancedPortals;
import uk.co.shadeddimensions.ep3.network.CommonProxy;
import uk.co.shadeddimensions.ep3.tileentity.TilePortal;
import uk.co.shadeddimensions.ep3.tileentity.frame.TilePortalController;
import uk.co.shadeddimensions.ep3.util.WorldCoordinates;

public class EntityManager
{
    static final int PLAYER_COOLDOWN_RATE = 10;

    private static ChunkCoordinates getActualExitLocation(Entity entity, TilePortalController controller)
    {
        int entityHeight = Math.round(entity.height), entityWidth = Math.round(entity.width);
        boolean horizontal = controller.portalType == 3;

        forloop:
            for (WorldCoordinates c : controller.getAllPortalBlocks())
            {
                for (int i = 0; i < (horizontal ? Math.round(entityWidth / 2) : entityHeight); i++)
                {
                    if (horizontal)
                    {
                        if (controller.worldObj.getBlockId(c.posX + i, c.posY, c.posZ) != CommonProxy.blockPortal.blockID || controller.worldObj.getBlockId(c.posX - i, c.posY, c.posZ) != CommonProxy.blockPortal.blockID || controller.worldObj.getBlockId(c.posX, c.posY, c.posZ + i) != CommonProxy.blockPortal.blockID || controller.worldObj.getBlockId(c.posX, c.posY, c.posZ + i) != CommonProxy.blockPortal.blockID)
                        {
                            continue forloop;
                        }
                    }
                    else
                    {
                        if (controller.worldObj.getBlockId(c.posX, c.posY + i, c.posZ) != CommonProxy.blockPortal.blockID && !controller.worldObj.isAirBlock(c.posX, c.posY + i, c.posZ))
                        {
                            continue forloop;
                        }
                    }
                }

                return c;
            }

        return null;
    }
    
    private static void handleMomentum(Entity entity, TilePortal portalTouched, float entityYaw)
    {
        float rotationYaw = (float) (Math.atan2(entity.motionX, entity.motionZ) * 180D / 3.141592653589793D);
        double cos = Math.cos(Math.toRadians(-rotationYaw));
        double sin = Math.sin(Math.toRadians(-rotationYaw));
        double tempXmotion = cos * entity.motionX - sin * entity.motionZ;
        double tempZmotion = sin * entity.motionX + cos * entity.motionZ;
        entity.motionX = tempXmotion;
        entity.motionZ = tempZmotion;

        cos = Math.cos(Math.toRadians(entityYaw));
        sin = Math.sin(Math.toRadians(entityYaw));
        tempXmotion = cos * entity.motionX - sin * entity.motionZ;
        tempZmotion = sin * entity.motionX + cos * entity.motionZ;
        entity.motionX = tempXmotion;
        entity.motionZ = tempZmotion;
    }

    private static float getRotation(Entity entity, TilePortalController controller, ChunkCoordinates loc)
    {        
        if (controller.portalType == 1)
        {
            if (controller.worldObj.isBlockOpaqueCube(loc.posX, loc.posY, loc.posZ + 1))
            {
                return 180f;
            }

            return 0f;
        }
        else if (controller.portalType == 2)
        {
            if (controller.worldObj.isBlockOpaqueCube(loc.posX - 1, loc.posY, loc.posZ))
            {
                return -90f;
            }

            return 90f;
        }
        
        return entity.rotationYaw;
    }
    
    private static void removeEntityFromWorld(Entity entity, WorldServer world)
    {
        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;

            player.closeScreen();
            world.playerEntities.remove(player);
            world.updateAllPlayersSleepingFlag();

            int chunkX = entity.chunkCoordX, chunkZ = entity.chunkCoordZ;

            if (entity.addedToChunk && world.getChunkProvider().chunkExists(chunkX, chunkZ))
            {
                world.getChunkFromChunkCoords(chunkX, chunkZ).removeEntity(entity);
                world.getChunkFromChunkCoords(chunkX, chunkZ).isModified = true;
            }

            world.loadedEntityList.remove(entity);
            world.onEntityRemoved(entity);
        }

        entity.isDead = false;
    }

    public static boolean isEntityFitForTravel(Entity entity)
    {
        return entity != null && entity.timeUntilPortal == 0;
    }

    public static void setEntityPortalCooldown(Entity entity)
    {
        if (EnhancedPortals.config.getBoolean("fasterPortalCooldown") || (entity instanceof EntityMinecart || entity instanceof EntityBoat || entity instanceof EntityHorse))
        {
            entity.timeUntilPortal = PLAYER_COOLDOWN_RATE;
        }
        else
        {
            entity.timeUntilPortal = entity.getPortalCooldown();
        }
    }

    public static void teleportEntity(Entity entity, GlyphIdentifier entryID, GlyphIdentifier exitID, TilePortal portal)
    {
        TilePortalController /*controllerEntry = CommonProxy.networkManager.getPortalController(entryID),*/ controllerDest = CommonProxy.networkManager.getPortalController(exitID);

        if (controllerDest == null)
        {
            CommonProxy.logger.fine("Failed to teleport entity - Cannot get TileEntity of exit Portal Controller!");
            return;
        }
        else if (!controllerDest.isPortalActive)
        {
            CommonProxy.logger.fine("Failed to teleport entity - Portal is not active!");
            return;
        }

        ChunkCoordinates exit = getActualExitLocation(entity, controllerDest);
        
        if (exit == null)
        {
            CommonProxy.logger.fine("Failed to teleport entity - Could not find a suitable exit location.");
            return;
        }
        else
        {
            CommonProxy.logger.fine(String.format("Found a suitable exit location for Entity (%s): %s, %s, %s", entity.getEntityName(), exit.posX, exit.posY, exit.posZ));
            teleportEntity(entity, exit, (WorldServer) controllerDest.worldObj, getRotation(entity, controllerDest, exit), portal);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Entity teleportEntity(Entity entity, ChunkCoordinates location, WorldServer world, float entityYaw, TilePortal portal)
    {
        double offsetY = entity instanceof EntityMinecart ? 0.4 : 0;
        boolean dimensionalTravel = entity.worldObj.provider.dimensionId != world.provider.dimensionId;
        Entity mount = entity.ridingEntity;
        
        if (mount != null) // If the entity is riding another entity
        {
            entity.mountEntity(null); // Dismount
            mount = teleportEntity(mount, location, world, entityYaw, portal); // Then send the mounted entity first. Store it for later use
        }
        
        entity.worldObj.updateEntityWithOptionalForce(entity, false);
        
        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;            
            player.closeScreen(); // Close any open GUI screen.
            
            if (dimensionalTravel)
            {
                player.dimension = world.provider.dimensionId; // Update the player's dimension
                player.playerNetServerHandler.sendPacketToPlayer(new Packet9Respawn(player.dimension, (byte) world.difficultySetting, world.provider.terrainType, world.provider.getHeight(), player.theItemInWorldManager.getGameType())); // Send a respawn packet to the player
                ((WorldServer) player.worldObj).getPlayerManager().removePlayer(player); // Remove the player from the world
            }
        }
        
        if (dimensionalTravel)
        {
            removeEntityFromWorld(entity, (WorldServer) entity.worldObj); // Remove the entity from the world
        }
        
        handleMomentum(entity, portal, entityYaw);
        
        world.getChunkProvider().loadChunk(location.posX >> 4, location.posZ >> 4); // Make sure the chunk is loaded
        entity.setPositionAndRotation(location.posX + 0.5, location.posY + offsetY, location.posZ + 0.5, entityYaw, entity.rotationPitch);
        
        if (dimensionalTravel)
        {
            if (!(entity instanceof EntityPlayer))
            {
                NBTTagCompound nbt = new NBTTagCompound();
                entity.isDead = false;
                entity.writeToNBTOptional(nbt); // Save all entity data to NBT, including it's ID
                entity.isDead = true;
                                
                entity = EntityList.createEntityFromNBT(nbt, world); // Make a new entity from the NBT data in the new world
                
                if (entity == null)
                {
                    return null; // If we failed, quit
                }
                
                entity.dimension = world.provider.dimensionId; // Update it's dimension
            }
            
            world.spawnEntityInWorld(entity); // Spawn it in the new world
            entity.setWorld(world); // Set the entities world to the new one
        }
        
        entity.setPositionAndRotation(location.posX + 0.5, location.posY + offsetY, location.posZ + 0.5, entityYaw, entity.rotationPitch);
        world.updateEntityWithOptionalForce(entity, false);
        entity.setPositionAndRotation(location.posX + 0.5, location.posY + offsetY, location.posZ + 0.5, entityYaw, entity.rotationPitch);
        
        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            
            if (dimensionalTravel)
            {
                player.mcServer.getConfigurationManager().func_72375_a(player, world); // ??
            }
            
            player.playerNetServerHandler.setPlayerLocation(location.posX + 0.5, location.posY + offsetY, location.posZ + 0.5, entityYaw, entity.rotationPitch); // Update the players location -- make sure the client gets this data too
        }
        
        world.updateEntityWithOptionalForce(entity, false);
        
        if (entity instanceof EntityPlayerMP && dimensionalTravel)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            
            player.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(player, world); // Make sure the client has the correct time & weather
            player.mcServer.getConfigurationManager().syncPlayerInventory(player); // And make sure their inventory isn't out of sync
            
            Iterator iterator = player.getActivePotionEffects().iterator();
            
            while (iterator.hasNext()) // Sync up any potion effects the player may have
            {
                PotionEffect effect = (PotionEffect) iterator.next();
                player.playerNetServerHandler.sendPacketToPlayer(new Packet41EntityEffect(player.entityId, effect));
            }
            
            player.playerNetServerHandler.sendPacketToPlayer(new Packet43Experience(player.experience, player.experienceTotal, player.experienceLevel)); // Sync up their xp
        }
        
        entity.setPositionAndRotation(location.posX + 0.5, location.posY + offsetY, location.posZ + 0.5, entityYaw, entity.rotationPitch);
        
        if (entity instanceof EntityMinecart) // Stops the minecart from derping about. TODO: Figure out a solution which isn't this.
        {
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;
        }
        
        if (mount != null) // Remount any mounted entities
        {
            if (!(entity instanceof EntityPlayerMP)) // Player re-mounting is derpy.
            {
                entity.mountEntity(mount);
            }
        }
        
        return entity;
    }
}
