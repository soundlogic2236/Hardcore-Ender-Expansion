package chylex.hee.mechanics.temple;
import java.io.File;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import chylex.hee.item.ItemTempleCaller;
import chylex.hee.system.savedata.ServerSavefile;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.world.biome.BiomeDecoratorHardcoreEnd;
import chylex.hee.world.feature.TempleGenerator;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public final class TempleEvents{
	private static TempleEvents instance;
	
	public static void register(){
		instance = new TempleEvents();
		FMLCommonHandler.instance().bus().register(instance);
		MinecraftForge.EVENT_BUS.register(instance);
	}
	
	public static void destroyWorld(World endWorld){
		if (instance == null)throw new RuntimeException("EndDestroyTicker has not been registered!");
		instance.endWorld = endWorld;
		BiomeDecoratorHardcoreEnd.getCache(endWorld).setDestroyEnd(true);
	}
	
	/**
     * Returns true if player was in temple, not if the temple was destroyed
     */
	public static boolean attemptDestroyTemple(EntityPlayer player){
		ServerSavefile save = BiomeDecoratorHardcoreEnd.getCache(player.worldObj);
		List<String> playersInTemple = save.getPlayersInTemple();
		
		if (playersInTemple.contains(player.getCommandSenderName())){
			if (playersInTemple.size() == 1 && !save.shouldPreventTempleDestruction()){
				save.resetPlayersInTemple();
				for(WorldServer world:DimensionManager.getWorlds()){
					if (world.provider.dimensionId == 1)new TempleGenerator(world).preloadAndClearArea(ItemTempleCaller.templeX,ItemTempleCaller.templeY,ItemTempleCaller.templeZ);
				}
			}
			else save.setPlayerIsInTemple(player.getCommandSenderName(),false);
			return true;
		}
		return false;
	}
	
	private byte tickTimer = 0;
	private World endWorld = null;
	private byte counter = 0;
	private boolean failedDestroying = false;
	
	private TempleEvents(){}
	
	@SubscribeEvent
	public void onGetBlockBreakSpeed(BreakSpeed e){
		if (e.entityPlayer.dimension == 1 && isPlayerInTemple(e.entityPlayer))e.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent e){
		if (e.entity.worldObj.isRemote || e.entity.dimension != 1)return;
		
		EntityPlayer player = e.entityPlayer;
		ItemStack is = player.inventory.getCurrentItem();
		if (is == null)return;
		
		if ((e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK) && is.getItem() != Item.getItemFromBlock(Blocks.dragon_egg) && isPlayerInTemple(player)){
			e.useBlock = Result.DENY;
			e.useItem = Result.DENY;
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent e){
		if (e.player.dimension == 1 && attemptDestroyTemple(e.player))e.player.setPositionAndUpdate(0,DragonUtil.getTopBlock(e.player.worldObj,Blocks.end_stone,0,0,100),0);
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent e){
		if (e.phase != Phase.START || ++tickTimer < 12 || endWorld == null)return;
		
		if (++counter > 2){
			File dim1 = new File(DimensionManager.getCurrentSaveRootDirectory(),"DIM1");
			if (!dim1.exists()){
				DragonUtil.severe("DIM1 not found!");
				endWorld = null;
				counter = 0;
				return;
			}
			
			failedDestroying = false;
			for(File file:dim1.listFiles())deleteFile(file);
			
			if (!failedDestroying){
				ServerSavefile save = BiomeDecoratorHardcoreEnd.getCache(endWorld);
				save.setDestroyEnd(false);
				save.setPreventTempleDestruction(false);
				save.resetPlayersInTemple();
				save.addDragonDeath();
				save.setDragonDead(false);
				save.resetCrystals();
				save.save();
				
				endWorld = null;
				counter = 0;
			}
			else DragonUtil.severe("Error deleting DIM1!");
		}
	}
	
	private boolean isPlayerInTemple(EntityPlayer player){
		return player.posY >= ItemTempleCaller.templeY && player.posX >= ItemTempleCaller.templeX && player.posZ >= ItemTempleCaller.templeZ&&
			   player.posY <= ItemTempleCaller.templeY+7 && player.posX <= ItemTempleCaller.templeX+13 && player.posZ <= ItemTempleCaller.templeZ+19;
	}
	
	private void deleteFile(File file){
		if (file.isDirectory()){
			for(File f:file.listFiles())deleteFile(f);
		}
		else if (!file.delete())failedDestroying = true;
	}
}