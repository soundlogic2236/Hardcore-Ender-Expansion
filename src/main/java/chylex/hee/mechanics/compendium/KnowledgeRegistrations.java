package chylex.hee.mechanics.compendium;
import net.minecraft.item.ItemStack;
import chylex.hee.block.BlockList;
import chylex.hee.item.ItemList;
import chylex.hee.mechanics.compendium.content.KnowledgeFragment;
import chylex.hee.mechanics.compendium.content.KnowledgeFragmentCrafting;
import chylex.hee.mechanics.compendium.content.KnowledgeFragmentEnhancement;
import chylex.hee.mechanics.compendium.content.KnowledgeFragmentText;
import chylex.hee.mechanics.compendium.content.KnowledgeObject;
import chylex.hee.mechanics.compendium.objects.ObjectDummy;
import chylex.hee.mechanics.compendium.objects.ObjectItem;
import chylex.hee.mechanics.enhancements.types.TNTEnhancements;

public final class KnowledgeRegistrations{
	public static final KnowledgeObject<ObjectDummy>
		HELP = new KnowledgeObject<>(new ObjectDummy());
	
	public static final KnowledgeObject<?>
		ADVENTURERS_DIARY = new KnowledgeObject<>(new ObjectItem(ItemList.adventurers_diary)),
		ENDERMAN_HEAD = new KnowledgeObject<>(new ObjectItem(ItemList.enderman_head)),
		MUSIC_DISKS = new KnowledgeObject<>(new ObjectItem(ItemList.music_disk));
	
	public static void initialize(){
		HELP.setFragments(new KnowledgeFragment[]{
			new KnowledgeFragmentText(0).setContents("Welcome to the Ender Compendium, the source of all knowledge about the End!"),
			new KnowledgeFragmentText(1).setContents("The Compendium is divided into phases, clicking them reveals new blocks, items and mobs you can find in that phase."),
			new KnowledgeFragmentText(2).setContents("In order to reveal information about these objects, first you have to either discover them, or spend a specified amount of Knowledge Points."),
			new KnowledgeFragmentText(3).setContents("Then you can spend your points on individual Knowledge Fragments."),
			new KnowledgeFragmentText(4).setContents("Note that discovering objects also unlocks some of their fragments and gives you points, whereas buying the object does neither."),
			new KnowledgeFragmentText(5).setContents("Knowledge Fragments also exist as items found in dungeons and traded by villagers. Using them gives you points or unlocks random fragments."),
			new KnowledgeFragmentText(6).setContents("You can use right mouse button instead of the Back button for easier use of the Compendium.")
		});
		
		KnowledgeCategories.OVERWORLD.addKnowledgeObjects(new KnowledgeObject[]{
			ADVENTURERS_DIARY.setPos(0,0).setUnlockPrice(20).setFragments(new KnowledgeFragment[]{
				new KnowledgeFragmentText(50).setContents("Text fragment").setPrice(10),
				new KnowledgeFragmentCrafting(51).setRecipeFromRegistry(new ItemStack(BlockList.essence_altar)).setPrice(10),
				new KnowledgeFragmentEnhancement(52).setEnhancement(TNTEnhancements.EXTRA_POWER).setPrice(10),
			})
			/*TEST_OBJECT.setPos(0,0).setUnlockPrice(20).setFragments(new KnowledgeFragment[]{
				new KnowledgeFragmentText(1000).setContents("Test fragment").setPrice(5).setUnlockOnDiscovery(),
				new KnowledgeFragmentText(1001).setContents("Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2").setPrice(10),
				new KnowledgeFragmentText(1002).setContents("Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2").setPrice(100),
				new KnowledgeFragmentText(1003).setContents("Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2").setPrice(400),
				new KnowledgeFragmentText(1004).setContents("Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2 Test fragment 2").setPrice(5000)
			})*/
		});
	}
	
	private KnowledgeRegistrations(){}
}