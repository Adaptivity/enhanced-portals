package uk.co.shadeddimensions.ep3.portal;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import uk.co.shadeddimensions.ep3.network.CommonProxy;
import uk.co.shadeddimensions.ep3.tileentity.portal.TileBiometricIdentifier;
import uk.co.shadeddimensions.ep3.tileentity.portal.TileController;
import uk.co.shadeddimensions.ep3.tileentity.portal.TileModuleManipulator;
import cpw.mods.fml.common.registry.GameRegistry;

public class EntityManager
{
    static final int PLAYER_COOLDOWN_RATE = 10;

    static ChunkCoordinates getActualExitLocation(Entity entity, TileController controller)
    {
        int entityHeight = Math.round(entity.height);
        //boolean horizontal = controller.portalType == 3;

        //forloop:
        for (ChunkCoordinates c : new ArrayList<ChunkCoordinates>(controller.getPortals()))
        {
            //if (!horizontal)
            //{
            //    for (int i = 0; i < entityHeight; i++)
            //    {
            //        if (controller.worldObj.getBlockId(c.posX, c.posY + i, c.posZ) != CommonProxy.blockPortal.blockID && !controller.worldObj.isAirBlock(c.posX, c.posY + i, c.posZ))
            //        {
            //            continue forloop;
            //        }
            //    }
            //}
            //else if (horizontal && !controller.worldObj.isAirBlock(c.posX, c.posY + 1, c.posZ))
            //{
            //    return new ChunkCoordinates(c.posX, c.posY - 1, c.posZ);
            //}

            return new ChunkCoordinates(c.posX, c.posY, c.posZ);
        }

        return null;
    }

    static float getRotation(Entity entity, TileController controller, ChunkCoordinates loc)
    {
        if (controller.portalType == 1)
        {
            if (controller.getWorldObj().isBlockNormalCubeDefault(loc.posX, loc.posY, loc.posZ + 1, false))
            {
                return 180f;
            }

            return 0f;
        }
        else if (controller.portalType == 2)
        {
            if (controller.getWorldObj().isBlockNormalCubeDefault(loc.posX - 1, loc.posY, loc.posZ, false))
            {
                return -90f;
            }

            return 90f;
        }
        else if (controller.portalType == 4)
        {
            if (controller.getWorldObj().isBlockNormalCubeDefault(loc.posX + 1, loc.posY, loc.posZ + 1, false))
            {
                return 135f;
            }

            return -45f;
        }
        else if (controller.portalType == 5)
        {
            if (controller.getWorldObj().isBlockNormalCubeDefault(loc.posX - 1, loc.posY, loc.posZ + 1, false))
            {
                return -135f;
            }

            return 45f;
        }

        return entity.rotationYaw;
    }

    static void handleMomentum(Entity entity, int touchedPortalType, int exitPortalType, float exitYaw, boolean keepMomentum)
    {
        if (!keepMomentum)
        {
            entity.motionX = entity.motionY = entity.motionZ = 0;
            return;
        }
        else if (touchedPortalType == 1)
        {
            if (exitPortalType == 2)
            {
                double temp = entity.motionZ;
                entity.motionZ = entity.motionX;
                entity.motionX = exitYaw == -90 ? -temp : temp;
            }
            else if (exitPortalType == 3)
            {
                double temp = entity.motionZ;
                entity.motionZ = entity.motionY;
                entity.motionY = temp;
            }
            else if (exitPortalType == 4)
            {
                double temp = entity.motionZ;
                entity.motionZ = entity.motionY;
                entity.motionY = -temp;
            }
        }
        else if (touchedPortalType == 2)
        {
            if (exitPortalType == 1)
            {
                double temp = entity.motionZ;
                entity.motionZ = entity.motionX;
                entity.motionX = exitYaw == 0 ? -temp : temp;
            }
            else if (exitPortalType == 3)
            {
                double temp = entity.motionX;
                entity.motionX = entity.motionY;
                entity.motionY = temp;
            }
            else if (exitPortalType == 4)
            {
                double temp = entity.motionX;
                entity.motionX = entity.motionY;
                entity.motionY = -temp;
            }
        }
        else if (touchedPortalType == 3 || touchedPortalType == 4)
        {
            if (exitPortalType == 1)
            {
                double temp = entity.motionY;
                entity.motionY = entity.motionZ;
                entity.motionZ = exitYaw == 0 ? -temp : temp;
            }
            else if (exitPortalType == 2)
            {
                double temp = entity.motionY;
                entity.motionY = entity.motionX;
                entity.motionX = exitYaw == -90 ? -temp : temp;
            }
            else if (exitPortalType == 3)
            {
                entity.motionY = touchedPortalType == 3 ? -entity.motionY : entity.motionY;
            }
        }

        entity.velocityChanged = true;
    }

    public static boolean isEntityFitForTravel(Entity entity)
    {
        return entity != null && entity.timeUntilPortal == 0;
    }

    public static void setEntityPortalCooldown(Entity entity)
    {
        if (entity == null)
        {
            return;
        }

        if (CommonProxy.fasterPortalCooldown || entity instanceof EntityPlayer || entity instanceof EntityMinecart || entity instanceof EntityBoat || entity instanceof EntityHorse)
        {
            entity.timeUntilPortal = PLAYER_COOLDOWN_RATE;
        }
        else
        {
            entity.timeUntilPortal = 300; // Reduced to 300 ticks from 900.
        }
    }

    public static void teleportEntityHighestInstability(Entity par1Entity) // TODO: CRIMSON
    {
        //boolean nether = MinecraftServer.getServer().getAllowNether();
        ChunkCoordinates spawn = /*nether ? DimensionManager.getWorld(-1).getSpawnPoint() :*/ par1Entity.worldObj.getSpawnPoint();

        //if (nether)
        //{
        //    transferEntityToDimension(par1Entity, spawn.posX, spawn.posY, spawn.posZ, 0f, (WorldServer) par1Entity.worldObj, DimensionManager.getWorld(-1), -1, -1, false);
        //}
        //else
        //{
        transferEntityWithinDimension(par1Entity, spawn.posX, par1Entity.worldObj.getTopSolidOrLiquidBlock(spawn.posX, spawn.posY), spawn.posZ, 0f, -1, -1, false);
        //}
    }

    static Entity transferEntity(Entity entity, double x, double y, double z, float yaw, WorldServer world, int touchedPortalType, int exitPortalType, boolean keepMomentum)
    {
        if (entity.worldObj.provider.dimensionId == world.provider.dimensionId)
        {
            return transferEntityWithinDimension(entity, x, y, z, yaw, touchedPortalType, exitPortalType, keepMomentum);
        }
        else
        {
            return transferEntityToDimension(entity, x, y, z, yaw, (WorldServer) entity.worldObj, world, touchedPortalType, exitPortalType, keepMomentum);
        }
    }

    public static void transferEntity(Entity entity, TileController entry, TileController exit) throws PortalException
    {
        TileBiometricIdentifier bio1 = entry.getBiometricIdentifier(), bio2 = exit.getBiometricIdentifier();

        if (bio1 != null && !bio1.canEntityTravel(entity))
        {
            throw new PortalException("noValidEntitySignatureSend");
        }

        if (bio2 != null && !bio2.canEntityTravel(entity))
        {
            throw new PortalException("noValidEntitySignatureReceive");
        }

        ChunkCoordinates exitLoc = getActualExitLocation(entity, exit);

        if (exitLoc == null)
        {
            throw new PortalException("failedToTransfer");
        }
        else
        {
            boolean keepMomentum = false;
            TileModuleManipulator manip = exit.getModuleManipulator();

            if (manip != null)
            {
                keepMomentum = manip.shouldKeepMomentumOnTeleport();
            }

            while (entity.ridingEntity != null)
            {
                entity = entity.ridingEntity;
            }

            transferEntityWithRider(entity, exitLoc.posX + 0.5, exitLoc.posY, exitLoc.posZ + 0.5, getRotation(entity, exit, exitLoc), (WorldServer) exit.getWorldObj(), entry.portalType, exit.portalType, keepMomentum);
        }
    }
    
    static Entity transferEntityToDimension(Entity entity, double x, double y, double z, float yaw, WorldServer exitingWorld, WorldServer enteringWorld, int touchedPortalType, int exitPortalType, boolean keepMomentum)
    {
        if (touchedPortalType == -1 && exitPortalType == -1)
        {
            while (!enteringWorld.isAirBlock((int) x, (int) y, (int) z) || !enteringWorld.isAirBlock((int) x, (int) y + 1, (int) z))
            {
                y++;

                if (y > 250)
                {
                    break;
                }
            }

            y++;
        }

        if (entity == null)
        {
            return null;
        }
        else if (!isEntityFitForTravel(entity))
        {
            return entity;
        }
        else if (entity instanceof EntityPlayer)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            MinecraftServer server = player.mcServer;
            ServerConfigurationManager config = server.getConfigurationManager();

            player.closeScreen();
            player.dimension = enteringWorld.provider.dimensionId;
            //player.playerNetServerHandler.sendPacketToPlayer(new Packet9Respawn(player.dimension, (byte) player.worldObj.difficultySetting, enteringWorld.getWorldInfo().getTerrainType(), enteringWorld.getHeight(), player.theItemInWorldManager.getGameType()));  // TODO

            exitingWorld.removePlayerEntityDangerously(player);
            player.isDead = false;
            player.setLocationAndAngles(x, y, z, yaw, player.rotationPitch);
            handleMomentum(player, touchedPortalType, exitPortalType, yaw, keepMomentum);

            enteringWorld.spawnEntityInWorld(player);
            player.setWorld(enteringWorld);

            config.func_72375_a(player, exitingWorld);
            player.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, entity.rotationPitch);
            player.theItemInWorldManager.setWorld(enteringWorld);

            config.updateTimeAndWeatherForPlayer(player, enteringWorld);
            config.syncPlayerInventory(player);

            Iterator potion = player.getActivePotionEffects().iterator();
            while (potion.hasNext())
            {
               // player.playerNetServerHandler.sendPacketToPlayer(new Packet41EntityEffect(player.entityId, (PotionEffect) potion.next())); // TODO
            }

            // player.playerNetServerHandler.sendPacketToPlayer(new Packet43Experience(player.experience, player.experienceTotal, player.experienceLevel))  // TODO
            //GameRegistry.onPlayerChangedDimension(player);  // TODO

            setEntityPortalCooldown(player);
            return player;
        }
        else
        {
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBTOptional(tag);

            int chunkX = entity.chunkCoordX;
            int chunkZ = entity.chunkCoordZ;

            if (entity.addedToChunk && exitingWorld.getChunkProvider().chunkExists(chunkX, chunkZ))
            {
                exitingWorld.getChunkFromChunkCoords(chunkX, chunkZ).removeEntity(entity);
            }

            exitingWorld.loadedEntityList.remove(entity);
            //exitingWorld.onEntityRemoved(entity); // TODO

            Entity newEntity = EntityList.createEntityFromNBT(tag, enteringWorld);

            if (newEntity != null)
            {
                handleMomentum(newEntity, touchedPortalType, exitPortalType, yaw, keepMomentum);
                newEntity.setLocationAndAngles(x, y, z, yaw, entity.rotationPitch);
                newEntity.forceSpawn = true;
                enteringWorld.spawnEntityInWorld(newEntity);
                newEntity.setWorld(enteringWorld);
                setEntityPortalCooldown(newEntity);
            }

            exitingWorld.resetUpdateEntityTick();
            enteringWorld.resetUpdateEntityTick();

            return newEntity;
        }
    }

    static Entity transferEntityWithinDimension(Entity entity, double x, double y, double z, float yaw, int touchedPortalType, int exitPortalType, boolean keepMomentum)
    {
        if (entity == null)
        {
            return null;
        }
        else if (!isEntityFitForTravel(entity))
        {
            return entity;
        }
        else if (entity instanceof EntityPlayer)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            player.rotationYaw = yaw;
            player.setPositionAndUpdate(x, y, z);
            handleMomentum(player, touchedPortalType, exitPortalType, yaw, keepMomentum);
            player.worldObj.updateEntityWithOptionalForce(player, false);

            setEntityPortalCooldown(player);
            return player;
        }
        else
        {
            WorldServer world = (WorldServer) entity.worldObj;
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBTOptional(tag);

            int chunkX = entity.chunkCoordX;
            int chunkZ = entity.chunkCoordZ;

            if (entity.addedToChunk && world.getChunkProvider().chunkExists(chunkX, chunkZ))
            {
                world.getChunkFromChunkCoords(chunkX, chunkZ).removeEntity(entity);
            }

            world.loadedEntityList.remove(entity);
            //world.onEntityRemoved(entity); // TODO

            Entity newEntity = EntityList.createEntityFromNBT(tag, world);

            if (newEntity != null)
            {
                handleMomentum(newEntity, touchedPortalType, exitPortalType, yaw, keepMomentum);
                newEntity.setLocationAndAngles(x, y, z, yaw, entity.rotationPitch);
                newEntity.forceSpawn = true;
                world.spawnEntityInWorld(newEntity);
                newEntity.setWorld(world);
                setEntityPortalCooldown(newEntity);
            }

            world.resetUpdateEntityTick();
            return newEntity;
        }
    }

    static Entity transferEntityWithRider(Entity entity, double x, double y, double z, float yaw, WorldServer world, int touchedPortalType, int exitPortalType, boolean keepMomentum)
    {
        Entity rider = entity.riddenByEntity;

        if (rider != null)
        {
            rider.mountEntity(null);
            rider = transferEntityWithRider(rider, x, y, z, yaw, world, touchedPortalType, exitPortalType, keepMomentum);
        }

        entity = transferEntity(entity, x, y, z, yaw, world, touchedPortalType, exitPortalType, keepMomentum);

        if (rider != null)
        {
            rider.mountEntity(entity);
        }

        return entity;
    }
}
