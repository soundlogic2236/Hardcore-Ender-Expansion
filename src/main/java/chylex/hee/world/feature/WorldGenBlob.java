package chylex.hee.world.feature;
import java.util.List;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.commons.lang3.tuple.Pair;
import chylex.hee.block.BlockList;
import chylex.hee.item.ItemKnowledgeNote;
import chylex.hee.item.ItemList;
import chylex.hee.item.ItemMusicDisk;
import chylex.hee.system.commands.HeeDebugCommand.HeeTest;
import chylex.hee.system.logging.Stopwatch;
import chylex.hee.system.weight.ObjectWeightPair;
import chylex.hee.system.weight.WeightedList;
import chylex.hee.world.feature.blobs.BlobGenerator;
import chylex.hee.world.feature.blobs.BlobPattern;
import chylex.hee.world.feature.blobs.BlobPopulator;
import chylex.hee.world.feature.blobs.generators.BlobGeneratorChain;
import chylex.hee.world.feature.blobs.generators.BlobGeneratorFromCenter;
import chylex.hee.world.feature.blobs.generators.BlobGeneratorRecursive;
import chylex.hee.world.feature.blobs.generators.BlobGeneratorSingle;
import chylex.hee.world.feature.blobs.generators.BlobGeneratorSingleCut;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorCave;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorChest;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorCover;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorHollower;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorLake;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorLiquidFall;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorOreCluster;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorOreScattered;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorPlant;
import chylex.hee.world.feature.blobs.populators.BlobPopulatorSpikes;
import chylex.hee.world.feature.util.DecoratorFeatureGenerator;
import chylex.hee.world.feature.util.DecoratorFeatureGenerator.IDecoratorGenPass;
import chylex.hee.world.loot.IItemPostProcessor;
import chylex.hee.world.loot.LootItemStack;
import chylex.hee.world.loot.WeightedLootList;
import chylex.hee.world.util.BlockLocation;
import chylex.hee.world.util.IRandomAmount;

public class WorldGenBlob extends WorldGenerator{
	private enum BlobType{
		COMMON, UNCOMMON, RARE;
		
		WeightedList<BlobPattern> patterns = new WeightedList<>();
	}
	
	private static final WeightedList<ObjectWeightPair<BlobType>> types = new WeightedList<>();
	
	static{
		types.add(ObjectWeightPair.of(BlobType.COMMON,42));
		types.add(ObjectWeightPair.of(BlobType.UNCOMMON,7));
		//types.add(ObjectWeightPair.of(BlobType.RARE,1));
		// TODO
		
		BlobType.COMMON.patterns.addAll(new BlobPattern[]{
			// basic random pattern
			new BlobPattern(1).addGenerators(new BlobGenerator[]{
				new BlobGeneratorFromCenter(10).amount(IRandomAmount.preferSmaller,2,6).rad(2.5D,4.5D).dist(3.5D,6D),
				new BlobGeneratorSingle(9).rad(2D,5D),
				new BlobGeneratorRecursive(8).baseAmount(IRandomAmount.linear,1,3).totalAmount(IRandomAmount.preferSmaller,4,8).recursionAmount(IRandomAmount.preferSmaller,0,5).recursionChance(0.1D,0.5D,0.8D,4).rad(2.8D,6D).distMp(0.5D,1.1D).cacheRecursionChance(),
				new BlobGeneratorFromCenter(7).amount(IRandomAmount.aroundCenter,2,8).rad(2.2D,5D).dist(6D,6D).limitDist().unifySize(),
				new BlobGeneratorSingle(4).rad(4D,10D),
				new BlobGeneratorFromCenter(3).amount(IRandomAmount.linear,4,10).rad(2.4D,3D).dist(2D,6D),
				new BlobGeneratorChain(3).amount(IRandomAmount.linear,3,6).rad(2.5D,4D).distMp(1.5D,2.5D)
			}).addPopulators(new BlobPopulator[]{
				new BlobPopulatorCave(4).rad(2D,2.8D).totalCaveAmount(IRandomAmount.linear,2,6).fullCaveAmount(IRandomAmount.preferSmaller,1,3).recursionChance(0.2D,0.8D,3).recursionRadMp(0.7D,0.9D).cacheRecursionChance(),
				new BlobPopulatorSpikes(4).block(Blocks.obsidian).amount(IRandomAmount.aroundCenter,2,10).maxOffset(16),
				new BlobPopulatorLiquidFall(2).block(BlockList.ender_goo).amount(IRandomAmount.preferSmaller,1,4).attempts(50,80),
				new BlobPopulatorLake(2).block(BlockList.ender_goo).rad(1.8D,3D),
				new BlobPopulatorOreCluster(4).block(BlockList.end_powder_ore).blockAmount(IRandomAmount.linear,4,7).iterationAmount(IRandomAmount.preferSmaller,1,4),
				new BlobPopulatorOreScattered(3).block(BlockList.igneous_rock_ore).blockAmount(IRandomAmount.preferSmaller,1,4).attempts(5,10).visiblePlacementAttempts(10).knownBlockLocations(),
				new BlobPopulatorOreScattered(1).block(BlockList.end_powder_ore).blockAmount(IRandomAmount.aroundCenter,3,8).attempts(8,15).visiblePlacementAttempts(5),
				new BlobPopulatorCover(1).block(Blocks.obsidian).replaceTopBlock(),
				new BlobPopulatorPlant(3).block(BlockList.death_flower).blockAmount(IRandomAmount.linear,3,7).attempts(20,35).knownBlockLocations()
			}).setPopulatorAmountProvider(IRandomAmount.preferSmaller,1,7)
		});
		
		BlobType.UNCOMMON.patterns.addAll(new BlobPattern[]{
			// tiny blob with ores
			new BlobPattern(7).addGenerators(new BlobGenerator[]{
				new BlobGeneratorSingle(4).rad(2.5D,3.5D),
				new BlobGeneratorSingle(1).rad(3.2D,5.2D)
			}).addPopulators(new BlobPopulator[]{
				new BlobPopulatorOreScattered(1).block(BlockList.end_powder_ore).blockAmount(IRandomAmount.linear,10,20).attempts(40,40).visiblePlacementAttempts(15).knownBlockLocations()
			}).setPopulatorAmountProvider(IRandomAmount.exact,1,1),
			
			// blob with a cut off part
			new BlobPattern(6).addGenerators(new BlobGenerator[]{
				new BlobGeneratorSingleCut(1).cutRadMp(0.2D,0.6D).cutDistMp(0.6D,0.8D).rad(3.5D,6D)
			}),
			
			// caterpillar
			new BlobPattern(4).addGenerators(new BlobGenerator[]{
				new BlobGeneratorChain(1).amount(IRandomAmount.aroundCenter,4,9).rad(2.7D,3.6D).distMp(0.9D,1.1D).unifySize()
			}),
			
			// hollow goo covered blob with a chest inside
			new BlobPattern(2).addGenerators(new BlobGenerator[]{
				new BlobGeneratorSingle(1).rad(5D,7.5D)
			}).addPopulators(new BlobPopulator[]{
				new BlobPopulatorHollower(1),
				new BlobPopulatorChest(1).loot(new WeightedLootList(new LootItemStack[]{
					new LootItemStack(ItemList.end_powder).setAmount(1,5).setWeight(15),
					new LootItemStack(ItemList.knowledge_note).setWeight(10),
					new LootItemStack(Items.ender_pearl).setAmount(1,4).setWeight(9),
					new LootItemStack(Items.bucket).setWeight(7),
					new LootItemStack(ItemList.bucket_ender_goo).setWeight(5),
					new LootItemStack(ItemList.adventurers_diary).setWeight(5),
					new LootItemStack(ItemList.music_disk).setDamage(0,ItemMusicDisk.getRecordCount()-1).setWeight(5),
					new LootItemStack(Items.ender_eye).setWeight(4)
				}).addItemPostProcessor(new IItemPostProcessor(){
					@Override
					public ItemStack processItem(ItemStack is, Random rand){
						return is.getItem() == ItemList.knowledge_note ? ItemKnowledgeNote.setRandomNote(is,rand,3) : is;
					}
				}),IRandomAmount.preferSmaller,3,10).onlyInside(),
				new BlobPopulatorCover(1).block(BlockList.ender_goo)
			}).setPopulatorAmountProvider(IRandomAmount.exact,3,3),
			
			// explosions from center
			new BlobPattern(2).addGenerators(new BlobGenerator[]{
				new BlobGeneratorFromCenter(1).amount(IRandomAmount.linear,1,5).rad(2.6D,4.5D).dist(3.2D,5.5D)
			}).addPopulators(new BlobPopulator[]{
				new BlobPopulatorSpikes(1).block(Blocks.air).amount(IRandomAmount.linear,25,42)
			}).setPopulatorAmountProvider(IRandomAmount.exact,1,1)
		});
	}
	
	private static final IDecoratorGenPass genSmootherPass = new IDecoratorGenPass(){
		private final byte[] airOffX = new byte[]{ -1, 1, 0, 0, 0, 0 },
							 airOffY = new byte[]{ 0, 0, 0, 0, -1, 1 },
							 airOffZ = new byte[]{ 0, 0, -1, 1, 0, 0 };
		
		@Override
		public void run(DecoratorFeatureGenerator gen, List<BlockLocation> blocks){
			for(BlockLocation loc:blocks){
				for(int a = 0, adjacentAir = 0; a < 6; a++){
					if (gen.getBlock(loc.x+airOffX[a],loc.y+airOffY[a],loc.z+airOffZ[a]) == Blocks.air && ++adjacentAir >= 4){
						gen.setBlock(loc.x,loc.y,loc.z,Blocks.air);
						break;
					}
				}
			}
		}
	};
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z){
		if (world.getBlock(x-7,y,z) != Blocks.air ||
			world.getBlock(x+7,y,z) != Blocks.air ||
			world.getBlock(x,y,z-7) != Blocks.air ||
			world.getBlock(x,y,z+7) != Blocks.air ||
			world.getBlock(x,y-7,z) != Blocks.air ||
			world.getBlock(x,y+7,z) != Blocks.air)return false;
		
		DecoratorFeatureGenerator gen = new DecoratorFeatureGenerator();
		Pair<BlobGenerator,List<BlobPopulator>> pattern = types.getRandomItem(rand).getObject().patterns.getRandomItem(rand).generatePattern(rand);
		
		pattern.getLeft().generate(gen,rand);
		gen.runPass(genSmootherPass);
		for(BlobPopulator populator:pattern.getRight())populator.generate(gen,rand);
		
		if (gen.getOutOfBoundsCounter() > 6)return false;
		
		gen.generate(world,rand,x,y,z);
		return true;
	}
	
	public static final HeeTest $debugTest = new HeeTest(){
		@Override
		public void run(){
			WeightedList<BlobPattern> patterns = new WeightedList<>(new BlobPattern[]{
				new BlobPattern(10).addGenerators(new BlobGenerator[]{
					new BlobGeneratorChain(1).amount(IRandomAmount.linear,3,6).rad(2.5D,4D).distMp(1.5D,2.5D)
				}).addPopulators(new BlobPopulator[]{
					
				}).setPopulatorAmountProvider(IRandomAmount.exact,1,1)
			});
			
			DecoratorFeatureGenerator gen = new DecoratorFeatureGenerator();
			Pair<BlobGenerator,List<BlobPopulator>> pattern = patterns.getRandomItem(world.rand).generatePattern(world.rand);
			
			Stopwatch.time("WorldGenBlob - test blob generator");
			pattern.getLeft().generate(gen,world.rand);
			Stopwatch.finish("WorldGenBlob - test blob generator");
			
			Stopwatch.time("WorldGenBlob - test smoother pass");
			gen.runPass(genSmootherPass);
			Stopwatch.finish("WorldGenBlob - test smoother pass");
			
			Stopwatch.time("WorldGenBlob - test pattern generator");
			for(BlobPopulator populator:pattern.getRight())populator.generate(gen,world.rand);
			Stopwatch.finish("WorldGenBlob - test pattern generator");
			
			Stopwatch.time("WorldGenBlob - test generate");
			gen.generate(world,world.rand,(int)player.posX+10,(int)player.posY-5,(int)player.posZ);
			Stopwatch.finish("WorldGenBlob - test generate");
		}
	};
}
