package chylex.hee.mechanics.misc;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import chylex.hee.entity.mob.EntityMobHomelandEnderman;
import chylex.hee.entity.technical.EntityTechnicalBiomeInteraction;
import chylex.hee.world.structure.island.biome.interaction.BiomeInteractionEnchantedIsland;

public final class HomelandEndermen{
	public enum HomelandRole{
		WORKER, ISLAND_LEADERS, GUARD, COLLECTOR, OVERWORLD_EXPLORER, BUSINESSMAN;
		public static final HomelandRole[] values = values();
	}
	
	public enum OvertakeGroupRole{
		LEADER, CHAOSMAKER, FIGHTER, TELEPORTER;
		public static final OvertakeGroupRole[] values = values();
		
		public static OvertakeGroupRole getRandomMember(Random rand){
			int r = rand.nextInt(10);
			
			if (r < 5)return FIGHTER;
			else if (r < 8)return CHAOSMAKER;
			else return TELEPORTER;
		}
	}
	
	public static boolean isOvertakeHappening(EntityMobHomelandEnderman source){
		return getOvertakeGroup(source) != -1;
	}
	
	public static long getOvertakeGroup(EntityMobHomelandEnderman source){
		List<EntityTechnicalBiomeInteraction> list = source.worldObj.getEntitiesWithinAABB(EntityTechnicalBiomeInteraction.class,source.boundingBox.expand(260D,128D,260D));
		
		if (!list.isEmpty()){
			for(EntityTechnicalBiomeInteraction entity:list){
				if (entity.getInteractionType() == BiomeInteractionEnchantedIsland.InteractionOvertake.class && entity.ticksExisted > 2){
					return ((BiomeInteractionEnchantedIsland.InteractionOvertake)entity.getInteraction()).groupId;
				}
			}
		}
		
		return -1;
	}
	
	public static List<EntityMobHomelandEnderman> getByHomelandRole(EntityMobHomelandEnderman source, HomelandRole role){
		List<EntityMobHomelandEnderman> all = source.worldObj.getEntitiesWithinAABB(EntityMobHomelandEnderman.class,source.boundingBox.expand(260D,128D,260D));
		List<EntityMobHomelandEnderman> filtered = new ArrayList<>();
		
		for(EntityMobHomelandEnderman enderman:all){
			if (enderman.getHomelandRole() == role)filtered.add(enderman);
		}
		
		return filtered;
	}

	public static List<EntityMobHomelandEnderman> getByGroupRole(EntityMobHomelandEnderman source, OvertakeGroupRole role){
		List<EntityMobHomelandEnderman> all = source.worldObj.getEntitiesWithinAABB(EntityMobHomelandEnderman.class,source.boundingBox.expand(260D,128D,260D));
		List<EntityMobHomelandEnderman> filtered = new ArrayList<>();
		
		for(EntityMobHomelandEnderman enderman:all){
			if (enderman.isInSameGroup(source) && enderman.getGroupRole() == role)filtered.add(enderman);
		}
		
		return filtered;
	}
	
	private HomelandEndermen(){}
}
