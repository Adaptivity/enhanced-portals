package enhancedportals.network;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import enhancedportals.block.BlockAutomaticDialler;
import enhancedportals.block.BlockDialDevice;
import enhancedportals.block.BlockDialDeviceBasic;
import enhancedportals.block.BlockDummyPortal;
import enhancedportals.block.BlockNetherPortal;
import enhancedportals.block.BlockObsidian;
import enhancedportals.block.BlockObsidianStairs;
import enhancedportals.block.BlockPortalModifier;
import enhancedportals.item.ItemEnhancedFlintSteel;
import enhancedportals.item.ItemMiscellaneous;
import enhancedportals.item.ItemNetworkCard;
import enhancedportals.item.ItemPortalModifierUpgrade;
import enhancedportals.lib.BlockIds;
import enhancedportals.lib.ItemIds;
import enhancedportals.lib.Localization;
import enhancedportals.lib.Settings;
import enhancedportals.portal.network.DialDeviceNetwork;
import enhancedportals.portal.network.ModifierNetwork;
import enhancedportals.tileentity.TileEntityAutomaticDialler;
import enhancedportals.tileentity.TileEntityDialDevice;
import enhancedportals.tileentity.TileEntityDialDeviceBasic;
import enhancedportals.tileentity.TileEntityNetherPortal;
import enhancedportals.tileentity.TileEntityPortalModifier;

public class CommonProxy
{
    public BlockNetherPortal         blockNetherPortal;
    public BlockObsidian             blockObsidian;
    public BlockObsidianStairs       blockObsidianStairs;
    public BlockPortalModifier       blockPortalModifier;
    public BlockDummyPortal          blockDummyPortal;
    public BlockDialDevice           blockDialDevice;
    public BlockDialDeviceBasic      blockDialDeviceBasic;
    public BlockAutomaticDialler     blockAutomaticDialler;

    public ItemPortalModifierUpgrade itemPortalModifierUpgrade;
    public ItemNetworkCard           itemNetworkCard;
    public ItemEnhancedFlintSteel    itemEnhancedFlintSteel;
    public ItemMiscellaneous         itemMisc;

    public ModifierNetwork           ModifierNetwork;
    public DialDeviceNetwork         DialDeviceNetwork;
    public boolean                   isIdentifierTaken = false;

    public World getWorld(int dimension)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimension);
    }

    public void loadBlocks()
    {
        blockNetherPortal = new BlockNetherPortal();
        blockPortalModifier = new BlockPortalModifier();
        blockObsidianStairs = new BlockObsidianStairs();
        blockDialDevice = new BlockDialDevice();
        blockDialDeviceBasic = new BlockDialDeviceBasic();
        blockAutomaticDialler = new BlockAutomaticDialler();
        blockDummyPortal = new BlockDummyPortal();

        GameRegistry.registerBlock(blockNetherPortal, Localization.NetherPortal_Name);
        GameRegistry.registerBlock(blockPortalModifier, Localization.PortalModifier_Name);
        GameRegistry.registerBlock(blockObsidianStairs, Localization.ObsidianStairs_Name);
        GameRegistry.registerBlock(blockDummyPortal, "dummyPortal");
        GameRegistry.registerBlock(blockDialDevice, Localization.DialDevice_Name);
        GameRegistry.registerBlock(blockDialDeviceBasic, Localization.DialDeviceBasic_Name);
        GameRegistry.registerBlock(blockAutomaticDialler, Localization.AutomaticDialler_Name);
    }

    public void loadItems()
    {
        itemPortalModifierUpgrade = new ItemPortalModifierUpgrade();
        itemNetworkCard = new ItemNetworkCard();
        itemEnhancedFlintSteel = new ItemEnhancedFlintSteel();
        itemMisc = new ItemMiscellaneous();

        GameRegistry.registerItem(itemPortalModifierUpgrade, Localization.PortalModifierUpgrade_Name);
        GameRegistry.registerItem(itemNetworkCard, Localization.NetworkCard_Name);
        GameRegistry.registerItem(itemEnhancedFlintSteel, Localization.EnhancedFlintSteel_Name);
        GameRegistry.registerItem(itemMisc, Localization.MiscellaneousItems_Name);
    }

    public void loadRecipes()
    {
        // --- ITEMS
        // Upgrade Card
        GameRegistry.addShapedRecipe(new ItemStack(itemMisc, 1, 0), "NDN", " P ", "N N", Character.valueOf('N'), Item.goldNugget, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5), Character.valueOf('P'), Item.paper);
        GameRegistry.addShapedRecipe(new ItemStack(itemMisc, 1, 0), "N N", " P ", "NDN", Character.valueOf('N'), Item.goldNugget, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5), Character.valueOf('P'), Item.paper);
        GameRegistry.addShapedRecipe(new ItemStack(itemMisc, 1, 0), "N N", "DP ", "N N", Character.valueOf('N'), Item.goldNugget, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5), Character.valueOf('P'), Item.paper);
        GameRegistry.addShapedRecipe(new ItemStack(itemMisc, 1, 0), "N N", " PD", "N N", Character.valueOf('N'), Item.goldNugget, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 5), Character.valueOf('P'), Item.paper);
        
        // Network Card
        GameRegistry.addShapedRecipe(new ItemStack(itemNetworkCard), "IDI", " P ", "I I", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('P'), Item.paper);
        GameRegistry.addShapedRecipe(new ItemStack(itemNetworkCard), "I I", "DP ", "I I", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('P'), Item.paper);
        GameRegistry.addShapedRecipe(new ItemStack(itemNetworkCard), "I I", " PD", "I I", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('P'), Item.paper);
        GameRegistry.addShapedRecipe(new ItemStack(itemNetworkCard), "I I", " P ", "IDI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('D'), new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('P'), Item.paper);
        
        // Flint & Steel
        GameRegistry.addShapelessRecipe(new ItemStack(itemEnhancedFlintSteel), Item.flintAndSteel, Item.lightStoneDust, Item.lightStoneDust);
        
        // Particle Upgrade
        GameRegistry.addShapelessRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 0), new ItemStack(itemMisc, 1, 0), Item.redstone, Item.blazePowder);
        
        // Sound Upgrade
        GameRegistry.addShapelessRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 1), new ItemStack(itemMisc, 1, 0), Item.redstone, Block.music);
        
        // Dimensional Upgrade
        GameRegistry.addShapedRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 2), "IEI", "GUG", "IRI", Character.valueOf('U'), new ItemStack(itemMisc, 1, 0), Character.valueOf('R'), Item.redstone, Character.valueOf('G'), Item.ingotGold, Character.valueOf('I'), Item.ingotIron, Character.valueOf('E'), Item.enderPearl);
        
        // Advanced Dimensional Upgrade
        GameRegistry.addShapedRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 3), "GEG", "DUD", "GIG", Character.valueOf('U'), new ItemStack(itemPortalModifierUpgrade, 1, 2), Character.valueOf('D'), Item.diamond, Character.valueOf('G'), Item.ingotGold, Character.valueOf('I'), Item.ingotIron, Character.valueOf('E'), Item.enderPearl);
        
        // Nether Frame Upgrade
        GameRegistry.addShapelessRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 5), new ItemStack(itemMisc, 1, 0), Block.glowStone, Block.netherBrick, Block.blockNetherQuartz);
        
        // Resourceful Frame Upgrade
        GameRegistry.addShapelessRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 6), new ItemStack(itemMisc, 1, 0), Block.blockIron, Block.blockGold, Item.diamond, Item.diamond, Item.emerald, Item.emerald);
        
        // Modifier Camouflage
        GameRegistry.addShapelessRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 7), new ItemStack(itemMisc, 1, 0), Item.diamond, Item.redstone, Item.enderPearl);
        
        // Dialling Upgrade
        GameRegistry.addShapelessRecipe(new ItemStack(itemPortalModifierUpgrade, 1, 8), new ItemStack(itemMisc, 1, 0), Item.redstone, Item.redstone, Item.enderPearl);
        
        // --- BLOCKS
        
    }

    public void loadSettings(Configuration config)
    {
        Settings.config = config;

        // Block IDs
        BlockIds.ObsidianStairs = config.getBlock("ObsidianStairs", BlockIds.ObsidianStairs).getInt();
        BlockIds.NetherPortal = config.getBlock("NetherPortal", BlockIds.NetherPortal).getInt();
        BlockIds.PortalModifier = config.getBlock("PortalModifier", BlockIds.PortalModifier).getInt();
        BlockIds.DialHomeDevice = config.getBlock("DialDevice", BlockIds.DialHomeDevice).getInt();

        // Item IDs
        ItemIds.PortalModifierUpgrade = config.getItem("PortalModifierUpgrade", ItemIds.PortalModifierUpgrade).getInt();
        ItemIds.NetworkCard = config.getItem("NetworkCard", ItemIds.NetworkCard).getInt();

        // Boolean configs
        Settings.AllowDialHomeDevices = config.get("Settings", "AllowDialDevices", Settings.AllowDialHomeDevices).getBoolean(Settings.AllowDialHomeDevices);
        Settings.AllowPortalColours = config.get("Settings", "AllowPortalColours", Settings.AllowPortalColours).getBoolean(Settings.AllowPortalColours);
        Settings.AllowPortalModifiers = config.get("Settings", "AllowPortalModifiers", Settings.AllowPortalModifiers).getBoolean(Settings.AllowPortalModifiers);
        Settings.DisableDHDRecipe = config.get("Settings", "DisableDialDeviceRecipe", Settings.DisableDHDRecipe).getBoolean(Settings.DisableDHDRecipe);
        Settings.DisableModifierRecipe = config.get("Settings", "DisableModifierRecipe", Settings.DisableModifierRecipe).getBoolean(Settings.DisableModifierRecipe);
        Settings.RenderPortalEffect = config.get("Effects", "RenderPortalEffect", Settings.RenderPortalEffect).getBoolean(Settings.RenderPortalEffect);

        // Integer configs
        Settings.SoundLevel = MathHelper.clamp_int(config.get("Effects", "SoundLevel", 100).getInt(), 0, 100);
        Settings.ParticleLevel = MathHelper.clamp_int(config.get("Effects", "ParticleLevel", 100).getInt(), 0, 100);
        Settings.PigmenLevel = MathHelper.clamp_int(config.get("Settings", "PigmenLevel", 100).getInt(), 0, 100);

        config.save();
    }

    public void loadTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityNetherPortal.class, "EPNPortal");
        GameRegistry.registerTileEntity(TileEntityPortalModifier.class, "EPPModifier");
        GameRegistry.registerTileEntity(TileEntityDialDevice.class, "EPDDevice");
        GameRegistry.registerTileEntity(TileEntityDialDeviceBasic.class, "EPDDBasic");
        GameRegistry.registerTileEntity(TileEntityAutomaticDialler.class, "EPADialler");
    }

    public void registerIcons(TextureStitchEvent.Pre event)
    {

    }
}