package chylex.hee.block;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.ForgeDirection;
import chylex.hee.item.block.ItemBlockWithSubtypes.IBlockSubtypes;
import chylex.hee.mechanics.knowledge.KnowledgeRegistrations;
import chylex.hee.mechanics.knowledge.util.ObservationUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCrossedDecoration extends BlockFlower implements IShearable, IBlockSubtypes{
	private static final String[] decorTypes = new String[]{
		"decor_bullrush_bottom", "decor_bullrush_top", "decor_thorn_bush", "decor_infested_grass", "decor_infested_fern", "decor_infested_tallgrass",
		"decor_lily_fire"
	};
	
	public static final byte dataThornBush = 2, dataInfestedGrass = 3, dataInfestedFern = 4, dataInfestedTallgrass = 5,
					   		 dataLilyFire = 6;
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;
	
	public BlockCrossedDecoration(){
		super(0);
		setBlockBounds(0.1F,0.0F,0.1F,0.9F,0.8F,0.9F);
	}
	
	@Override
	public boolean canBlockStay(World world, int x, int y, int z){
		Block soil = world.getBlock(x,y-1,z);
		return (world.getFullBlockLightValue(x,y,z) >= 8 || world.canBlockSeeTheSky(x,y,z) || world.provider.dimensionId == 1)&&
			   (soil != null && soil.canSustainPlant(world,x,y-1,z,ForgeDirection.UP,this));
	}
	
	@Override
	protected boolean canPlaceBlockOn(Block block){
		return block == Blocks.end_stone || block == BlockList.end_terrain || super.canPlaceBlockOn(block);
	}
	
	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta){
		super.harvestBlock(world,player,x,y,z,meta);
		
		if (meta == dataLilyFire)KnowledgeRegistrations.LILYFIRE.tryUnlockFragment(player,1F);
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune){
		ArrayList<ItemStack> ret = new ArrayList<>();

		if (meta == dataLilyFire)ret.add(new ItemStack(this,1,meta));
		
		return ret;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z){
		int meta = world.getBlockMetadata(x,y,z);
		
		if (meta == dataLilyFire)return AxisAlignedBB.getBoundingBox(x+0.3F,y,z+0.3F,x+0.7F,y+0.8F,z+0.7F);
		else return super.getSelectedBoundingBoxFromPool(world,x,y,z);
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z){
		int meta = world.getBlockMetadata(x,y,z);
		return meta != dataLilyFire;
	}

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune){
		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(new ItemStack(this,1,world.getBlockMetadata(x,y,z)));
		return ret;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity){
		if (world.getBlockMetadata(x,y,z) == dataThornBush){
			entity.attackEntityFrom(DamageSource.generic,1F);
			if (world.rand.nextInt(80) == 0 && entity instanceof EntityLivingBase){
				((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.poison.id,30+world.rand.nextInt(40),1,true));
			}
			
			if (world.rand.nextInt(66) == 0){
				for(EntityPlayer observer:ObservationUtil.getAllObservers(entity,8D))KnowledgeRegistrations.INFESTED_FOREST_BIOME.tryUnlockFragment(observer,0.3F,new short[]{ 1 });
			}
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack is){
		switch(is.getItemDamage()){
			case BlockCrossedDecoration.dataThornBush: return "tile.crossedDecoration.thornyBush";
			case BlockCrossedDecoration.dataInfestedFern: return "tile.crossedDecoration.infestedFern";
			case BlockCrossedDecoration.dataInfestedGrass: return "tile.crossedDecoration.infestedBush";
			case BlockCrossedDecoration.dataInfestedTallgrass: return "tile.crossedDecoration.infestedGrass";
			case BlockCrossedDecoration.dataLilyFire: return "tile.crossedDecoration.lilyfire";
			default: return "";
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta){
		return iconArray[meta < decorTypes.length ? meta : 0];
	}

	@SuppressWarnings("unchecked")
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list){
		for(int a = 2; a < decorTypes.length; a++){
			list.add(new ItemStack(item,1,a));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister){
		iconArray = new IIcon[decorTypes.length];

		for(int a = 2; a < decorTypes.length; ++a){
			iconArray[a] = iconRegister.registerIcon("hardcoreenderexpansion:"+decorTypes[a]);
		}
	}
}