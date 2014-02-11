package uk.co.shadeddimensions.ep3.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import uk.co.shadeddimensions.ep3.EnhancedPortals;
import uk.co.shadeddimensions.ep3.lib.Reference;
import buildcraft.api.tools.IToolWrench;
import cofh.api.block.IDismantleable;

public class ItemWrench extends Item implements IToolWrench
{
    public static ItemWrench instance;

    public ItemWrench()
    {
        super();
        instance = this;
        setCreativeTab(Reference.creativeTab);
        setUnlocalizedName("wrench");
        setTextureName("enhancedportals:wrench");
        setMaxStackSize(1);
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean canWrench(EntityPlayer player, int x, int y, int z)
    {
        return true;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, int x, int y, int z)
    {

    }
    
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote && player.isSneaking())
        {
            Block block = world.getBlock(x, y, z);

            if (block instanceof IDismantleable)
            {
                if (((IDismantleable) block).canDismantle(player, world, x, y, z))
                {
                    ((IDismantleable) block).dismantleBlock(player, world, x, y, z, false);
                    return true;
                }
            }
        }

        return false;
    }
}
