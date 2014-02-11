package uk.co.shadeddimensions.ep3.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import uk.co.shadeddimensions.ep3.EnhancedPortals;
import uk.co.shadeddimensions.ep3.lib.Reference;
import uk.co.shadeddimensions.ep3.network.GuiHandler;

public class ItemGuide extends Item
{
    public static ItemGuide instance;
    
    IIcon texture;
    
    public ItemGuide()
    {
        super();
        instance = this;
        setCreativeTab(Reference.creativeTab);
        setUnlocalizedName("guide");
        setMaxStackSize(1);
    }

    @Override
    public IIcon getIconFromDamage(int par1)
    {
        return texture;
    }
    
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        texture = iconRegister.registerIcon("enhancedportals:guide");
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.openGui(EnhancedPortals.instance, GuiHandler.GUIDE, world, 0, 0, 0);
        return super.onItemRightClick(stack, world, player);
    }
}
