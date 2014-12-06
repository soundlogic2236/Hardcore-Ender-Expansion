package chylex.hee.block;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.entity.boss.EntityMiniBossFireFiend;
import chylex.hee.entity.fx.FXType;
import chylex.hee.entity.technical.EntityTechnicalPuzzleChain;
import chylex.hee.item.block.ItemBlockWithSubtypes.IBlockSubtypes;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C20Effect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockDungeonPuzzle extends Block implements IBlockSubtypes{
	private static final Material dungeonPuzzle = new MaterialDungeonPuzzle();
	public static final byte dungeonSize = 9;
	
	public static final byte metaTriggerUnlit = 0, metaTriggerLit = 1, metaChainedUnlit = 2, metaChainedLit = 3,
							 metaDistributorChainUnlit = 4, metaDistributorChainLit = 5,
							 metaDistributorSquareUnlit = 6, metaDistributorSquareLit = 7,
							 metaWall = 13, metaRock = 14, metaCeiling = 15;
	
	public static final byte[] icons = new byte[]{ 2, 3, 4, 5, 6, 7, 8, 9, 3, 3, 3, 3, 3, 3, 1, 3 };
	
	public static final String[] names = new String[]{
		"trigger.unlit", "trigger.lit", "chained.unlit", "chained.lit", "distr.chain.unlit", "distr.chain.lit", "distr.square.unlit", "distr.square.lit",
		null, null, null, null, null, null, "wall", "rock", "ceiling"
	};
	
	public static final boolean canTrigger(int meta){
		return meta == metaTriggerUnlit || meta == metaTriggerLit;
	}
	
	public static final int toggleState(int meta){
		if (meta == metaWall || meta == metaRock || meta == metaCeiling)return meta;
		else return (meta&1) == 0 ? meta+1 : meta-1;
	}
	
	public static final boolean isLit(int meta){
		return (meta&1) != 0;
	}
	
	public static final int getUnlit(int meta){
		return (meta&1) != 0 ? meta-1 : meta;
	}
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;
	
	public BlockDungeonPuzzle(){
		super(dungeonPuzzle);
	}
	
	/**
	 * Update chain from the entity, return false to stop the chain.
	 */
	public boolean updateChain(World world, int x, int y, int z, byte chainDir){
		int meta = world.getBlockMetadata(x,y,z), toggled = toggleState(meta);
		
		if (meta != toggled){
			world.setBlockMetadataWithNotify(x,y,z,toggled,3);
			
			int unlit = getUnlit(meta);
			
			if (unlit == metaDistributorChainUnlit){
				for(int dir = 0, distrMeta, distrToggled, tx, tz; dir < 4; dir++){
					if (dir == Direction.rotateOpposite[chainDir])continue;
					
					if ((distrToggled = toggleState(distrMeta = world.getBlockMetadata(x+(tx = Direction.offsetX[dir]),y,z+(tz = Direction.offsetZ[dir])))) != distrMeta){
						PacketPipeline.sendToAllAround(world.provider.dimensionId,x+tx+0.5D,y+0.5D,z+tz+0.5D,64D,new C20Effect(FXType.Basic.DUNGEON_PUZZLE_BURN,x+tx+0.5D,y+0.5D,z+tz+0.5D));
						world.setBlockMetadataWithNotify(x+tx,y,z+tz,distrToggled,3);
						world.spawnEntityInWorld(new EntityTechnicalPuzzleChain(world,x+tx,y,z+tz,dir));
					}
				}
			}
			else if (unlit == metaDistributorSquareUnlit){
				for(int xx = -1, zz, distrMeta, distrToggled; xx <= 1; xx++){
					for(zz = -1; zz <= 1; zz++){
						if ((distrToggled = toggleState(distrMeta = world.getBlockMetadata(x+xx,y,z+zz))) != distrMeta && !(xx+x == x+Direction.offsetX[chainDir] && zz+z == z+Direction.offsetZ[chainDir])){
							PacketPipeline.sendToAllAround(world.provider.dimensionId,x+xx+0.5D,y+0.5D,z+zz+0.5D,64D,new C20Effect(FXType.Basic.DUNGEON_PUZZLE_BURN,x+xx+0.5D,y+0.5D,z+zz+0.5D));
							world.setBlockMetadataWithNotify(x+xx,y,z+zz,distrToggled,3);
						}
					}
				}
			}
			else return true;
		}
		
		if (world.getEntitiesWithinAABB(EntityTechnicalPuzzleChain.class,AxisAlignedBB.getBoundingBox(x+0.5D-dungeonSize,y,z+0.5D-dungeonSize,x+0.5D+dungeonSize,y+1D,z+0.5D+dungeonSize)).isEmpty()){
			int startX = x+1, startZ = z+1, cnt = 0;
			boolean isFinished = true;
			
			while(world.getBlock(--startX,y,z) != this);
			while(world.getBlock(x,y,--startZ) != this);
			
			++startX;
			++startZ;
			
			for(int xx = startX; xx < startX+dungeonSize; xx++){
				for(int zz = startZ; zz < startZ+dungeonSize; zz++){
					if (world.getBlock(xx,y,zz) != this)continue;
					
					++cnt;
					
					if (!isLit(toggleState(world.getBlockMetadata(xx,y,zz)))){
						isFinished = false;
						xx += dungeonSize;
						break;
					}
				}
			}
			
			int cx = startX+((dungeonSize-1)>>1), cz = startZ+((dungeonSize-1)>>1);
			
			if (isFinished && cnt > 32 && world.getEntitiesWithinAABB(EntityMiniBossFireFiend.class,AxisAlignedBB.getBoundingBox(cx-4,y-8,cz-4,cx+4,y,cz+4)).isEmpty()){
				HardcoreEnderExpansion.notifications.report("solved");
				// TODO
				/*EntityMiniBossFireFiend fireFiend = new EntityMiniBossFireFiend(world);
				fireFiend.setLocationAndAngles(cx+0.5D,y-4D,cz+0.5D,world.rand.nextFloat()*360F,0F);
				world.spawnEntityInWorld(fireFiend);*/
			}
		}
		
		return false;
	}
	
	@Override
	public Item getItemDropped(int meta, Random rand, int fortune){
		return null;
	}
	
	@Override
	protected ItemStack createStackedBlock(int meta){
		return null;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z){
		return new ItemStack(this,1,world.getBlockMetadata(x,y,z));
	}
	
	@Override
	public String getUnlocalizedName(ItemStack is){
		String name = names[Math.max(0,Math.min(names.length-1,is.getItemDamage()))];
		return name == null ? "" : "tile.dungeonPuzzle."+name;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta){
		return meta >= 0 && meta < icons.length ? iconArray[icons[meta]] : iconArray[3];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list){
		for(byte meta:new byte[]{
			metaWall, metaRock, metaCeiling, metaTriggerUnlit, metaTriggerLit, metaChainedUnlit, metaChainedLit,
			metaDistributorChainUnlit, metaDistributorChainLit, metaDistributorSquareUnlit, metaDistributorSquareLit,
		})list.add(new ItemStack(item,1,meta));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister){
		iconArray = new IIcon[10];
		iconArray[0] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_wall");
		iconArray[1] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_wall_rock");
		iconArray[2] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_trigger_unlit");
		iconArray[3] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_trigger_lit");
		iconArray[4] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_chained_unlit");
		iconArray[5] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_chained_lit");
		iconArray[6] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_distributor_spread_unlit");
		iconArray[7] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_distributor_spread_lit");
		iconArray[8] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_distributor_square_unlit");
		iconArray[9] = iconRegister.registerIcon("hardcoreenderexpansion:dungeon_puzzle_distributor_square_lit");
	}
}
