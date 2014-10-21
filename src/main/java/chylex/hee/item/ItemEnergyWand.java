package chylex.hee.item;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import chylex.hee.block.BlockList;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.tileentity.TileEntityEnergyCluster;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEnergyWand extends Item{
	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
		if (is.stackTagCompound != null && is.stackTagCompound.hasKey("cluster")){
			switch(side){
				case 1: ++y; break;
				case 2: --z; break;
				case 3: ++z; break;
				case 4: --x; break;
				case 5: ++x; break;
			}

			if (!player.canPlayerEdit(x,y,z,side,is) || world.getBlock(x,y,z).getMaterial() != Material.air)return false;
			
			world.setBlock(x,y,z,BlockList.energy_cluster);
			TileEntityEnergyCluster tile = (TileEntityEnergyCluster)world.getTileEntity(x,y,z);
			
			if (tile != null){
				tile.readTileFromNBT(is.stackTagCompound.getCompoundTag("cluster"));
				if (world.rand.nextInt(100) < tile.data.getHealthStatus().chanceToWeaken)tile.data.weakenCluster();
				tile.synchronize();
			}
			
			is.stackTagCompound.removeTag("cluster");
			return true;
		}
		else if (world.getBlock(x,y,z) == BlockList.energy_cluster){
			TileEntityEnergyCluster tile = (TileEntityEnergyCluster)world.getTileEntity(x,y,z);
			
			if (tile != null){
				tile.shouldNotExplode = true;
				tile.onAbsorbed(player,is);
				
				if (!world.isRemote){
					NBTTagCompound tag = tile.writeTileToNBT(new NBTTagCompound());
					tag.setIntArray("loc",ArrayUtils.EMPTY_INT_ARRAY);
					
					if (is.stackTagCompound == null)is.stackTagCompound = new NBTTagCompound();
					is.stackTagCompound.setTag("cluster",tag);
					
					world.setBlockToAir(x,y,z);
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, EntityPlayer player, List textLines, boolean showAdvancedInfo){
		if (is.stackTagCompound == null || !is.stackTagCompound.hasKey("cluster"))return;
		textLines.add("Holding cluster with "+DragonUtil.formatTwoPlaces.format(is.stackTagCompound.getCompoundTag("cluster").getShort("energyAmt")*0.01F)+" Energy");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack is, int pass){
		return is.stackTagCompound != null && is.stackTagCompound.hasKey("cluster");
	}
}