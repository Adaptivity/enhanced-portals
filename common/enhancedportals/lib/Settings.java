package enhancedportals.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.util.Icon;
import net.minecraftforge.common.Configuration;
import enhancedportals.portal.PortalTexture;

public class Settings
{
    public static Configuration config;

    public static boolean AllowPortalModifiers = true;
    public static boolean AllowDialHomeDevices = true;
    public static final boolean AllowTeleporting = true;

    public static boolean DisableModifierRecipe = false;
    public static boolean DisableDHDRecipe = false;

    public static boolean RenderPortalEffect = true;
    public static boolean AllowPortalColours = true;

    public static int PigmenLevel = 100;
    public static int SoundLevel = 100;
    public static int ParticleLevel = 100;

    public static int[] LiquidList = new int[] { Block.lavaStill.blockID, Block.lavaMoving.blockID, Block.waterStill.blockID, Block.waterMoving.blockID };
    public static int[] ExcludedBlockList = new int[] { Block.torchWood.blockID, Block.leaves.blockID, Block.sapling.blockID, Block.web.blockID, Block.rail.blockID, Block.railActivator.blockID, Block.railDetector.blockID, Block.railPowered.blockID, Block.deadBush.blockID, Block.mushroomBrown.blockID, Block.mushroomRed.blockID, Block.fire.blockID, Block.redstoneComparatorActive.blockID, Block.redstoneComparatorIdle.blockID, Block.redstoneRepeaterActive.blockID, Block.redstoneRepeaterIdle.blockID, Block.redstoneWire.blockID, Block.crops.blockID, Block.lever.blockID, Block.doorIron.blockID, Block.doorWood.blockID, Block.torchRedstoneActive.blockID, Block.torchRedstoneIdle.blockID, Block.cake.blockID, Block.tripWire.blockID, Block.tripWireSource.blockID, Block.melonStem.blockID, Block.waterlily.blockID, Block.vine.blockID, Block.tallGrass.blockID, Block.skull.blockID, Block.potato.blockID, Block.netherStalk.blockID, Block.plantYellow.blockID, Block.plantRed.blockID };

    public static Map<String, Icon> CustomIconMap = new HashMap<String, Icon>();
    public static Map<String, Integer> CustomParticleMap = new HashMap<String, Integer>();
    public static Map<String, PortalTexture> ItemPortalTextureMap = new HashMap<String, PortalTexture>();

    public static List<Integer> ValidItemsList = new ArrayList<Integer>();
    public static List<Integer> BorderBlocks = new ArrayList<Integer>();
    public static List<Integer> DestroyBlocks = new ArrayList<Integer>();

    public static boolean isBlockExcluded(int id)
    {
        for (int i : ExcludedBlockList)
        {
            if (i == id)
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isLiquid(PortalTexture text)
    {
        if (text.colour != -1)
        {
            return false;
        }

        for (int i : LiquidList)
        {
            if (i == text.blockID)
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidItem(int itemID)
    {
        for (int i : ValidItemsList)
        {
            if (i == itemID)
            {
                return true;
            }
        }

        return false;
    }
}
