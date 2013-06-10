package enhancedportals.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enhancedportals.lib.BlockIds;
import enhancedportals.lib.ItemIds;
import enhancedportals.lib.Localization;
import enhancedportals.lib.Reference;
import enhancedportals.lib.Strings;
import enhancedportals.network.packet.PacketEnhancedPortals;
import enhancedportals.network.packet.PacketPortalModifierUpdate;
import enhancedportals.portal.upgrades.Upgrade;
import enhancedportals.portal.upgrades.modifier.UpgradeAdvancedDimensional;
import enhancedportals.portal.upgrades.modifier.UpgradeDimensional;
import enhancedportals.tileentity.TileEntityPortalModifier;

public class ItemPortalModifierUpgrade extends Item
{
    Icon[] textures;

    public ItemPortalModifierUpgrade()
    {
        super(ItemIds.PortalModifierUpgrade);
        hasSubtypes = true;
        setMaxDamage(0);
        setCreativeTab(Reference.CREATIVE_TAB);
        setUnlocalizedName(Localization.PortalModifierUpgrade_Name);
        maxStackSize = 1;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
    {
        list.add(Strings.PortalModifierUpgrade.toString());
        list.addAll(Upgrade.getUpgrade(itemStack.getItemDamage()).getText(false));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIconFromDamage(int par1)
    {
        if (par1 > textures.length)
        {
            return textures[0];
        }

        return textures[par1];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack itemStack)
    {
        return EnumRarity.rare;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int var4 = 0; var4 < textures.length; var4++)
        {
            if (var4 == 4)
            {
                continue;
            }

            par3List.add(new ItemStack(par1, 1, var4));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 15);
        return super.getUnlocalizedName() + "." + Upgrade.getUpgradeNames()[i];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack par1ItemStack)
    {
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        if (world.isRemote)
        {
            return false;
        }

        if (world.getBlockId(x, y, z) == BlockIds.PortalModifier)
        {
            TileEntityPortalModifier modifier = (TileEntityPortalModifier) world.getBlockTileEntity(x, y, z);

            if (!modifier.upgradeHandler.hasUpgrade(Upgrade.getUpgrade(stack.getItemDamage())))
            {
                if (stack.getItemDamage() == 3 && modifier.upgradeHandler.hasUpgrade(new UpgradeDimensional()))
                {
                    player.sendChatToPlayer(Strings.ChatDimAlreadyInstalled.toString());
                    return false;
                }
                else if (stack.getItemDamage() == 2 && modifier.upgradeHandler.hasUpgrade(new UpgradeAdvancedDimensional()))
                {
                    player.sendChatToPlayer(Strings.ChatAdvDimAlreadyInstalled.toString());
                    return false;
                }

                if (modifier.upgradeHandler.addUpgrade(Upgrade.getUpgrade(stack.getItemDamage()), modifier))
                {
                    if (!player.capabilities.isCreativeMode)
                    {
                        player.inventory.mainInventory[player.inventory.currentItem] = null;
                        ((EntityPlayerMP) player).mcServer.getConfigurationManager().syncPlayerInventory((EntityPlayerMP) player);
                    }

                    PacketDispatcher.sendPacketToAllAround(x + 0.5, y + 0.5, z + 0.5, 128, world.provider.dimensionId, PacketEnhancedPortals.makePacket(new PacketPortalModifierUpdate(modifier)));
                }
                else
                {
                    player.sendChatToPlayer(Strings.ChatMaxUpgradesInstalled.toString());
                }
            }
            else
            {
                player.sendChatToPlayer(Strings.ChatUpgradeInstalled.toString());
            }
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister)
    {
        textures = new Icon[Upgrade.getUpgradeNames().length];
        String[] names = Upgrade.getUpgradeNames();

        for (int i = 0; i < textures.length; i++)
        {
            textures[i] = iconRegister.registerIcon(Reference.MOD_ID + ":" + Localization.PortalModifierUpgrade_Name + "_" + names[i]);
        }
    }
}
