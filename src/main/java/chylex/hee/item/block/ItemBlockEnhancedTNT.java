package chylex.hee.item.block;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import chylex.hee.mechanics.enhancements.EnhancementHandler;
import chylex.hee.tileentity.TileEntityEnhancedTNT;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockEnhancedTNT extends ItemBlock{
	public ItemBlockEnhancedTNT(Block block){
		super(block);
	}
	
	@Override
	public boolean placeBlockAt(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata){
		ItemStack isCopy = is.copy();
		
		if (super.placeBlockAt(is,player,world,x,y,z,side,hitX,hitY,hitZ,metadata)){
			TileEntityEnhancedTNT tile = (TileEntityEnhancedTNT)world.getTileEntity(x,y,z);
			if (tile != null)tile.loadEnhancementsFromItem(isCopy);
			return true;
		}
		else return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, EntityPlayer player, List textLines, boolean showAdvancedInfo){
		EnhancementHandler.appendEnhancementNames(is,textLines);
	}
}
