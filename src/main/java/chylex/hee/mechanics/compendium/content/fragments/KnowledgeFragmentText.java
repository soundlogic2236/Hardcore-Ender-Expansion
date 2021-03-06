package chylex.hee.mechanics.compendium.content.fragments;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import chylex.hee.gui.GuiEnderCompendium;
import chylex.hee.mechanics.compendium.content.KnowledgeFragment;
import chylex.hee.mechanics.misc.Baconizer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class KnowledgeFragmentText extends KnowledgeFragment{
	public static byte smoothRenderingMode = 0;
	
	public KnowledgeFragmentText(int globalID){
		super(globalID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getHeight(GuiEnderCompendium gui, boolean isUnlocked){
		boolean origFont = gui.mc.fontRenderer.getUnicodeFlag();
		gui.mc.fontRenderer.setUnicodeFlag(true);
		int h = gui.mc.fontRenderer.listFormattedStringToWidth(getString(true),GuiEnderCompendium.guiPageWidth-10).size()*gui.mc.fontRenderer.FONT_HEIGHT;
		gui.mc.fontRenderer.setUnicodeFlag(origFont);
		return h;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean onClick(GuiEnderCompendium gui, int x, int y, int mouseX, int mouseY, int buttonId, boolean isUnlocked){
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onRender(GuiEnderCompendium gui, int x, int y, int mouseX, int mouseY, boolean isUnlocked){
		renderString(getString(isUnlocked),x+1,y,GuiEnderCompendium.guiPageWidth-10,gui);
	}
	
	protected String getString(boolean isUnlocked){
		String content = I18n.format("ec.reg."+globalID);
		return isUnlocked ? Baconizer.sentence(content) : StringUtils.repeat('?',content.length());
	}
	
	public static void renderString(String str, int x, int y, GuiEnderCompendium gui){
		renderString(str,x,y,9999,gui);
	}
	
	public static void renderString(String str, int x, int y, int maxWidth, GuiEnderCompendium gui){
		renderString(str,x,y,maxWidth,255<<24,240<<24,gui);
	}
	
	public static void renderString(String str, int x, int y, int normalColor, int smoothColor, GuiEnderCompendium gui){
		renderString(str,x,y,9999,normalColor,smoothColor,gui);
	}
	
	public static void renderString(String str, int x, int y, int maxWidth, int normalColor, int smoothColor, GuiEnderCompendium gui){
		boolean origFont = gui.mc.fontRenderer.getUnicodeFlag();
		gui.mc.fontRenderer.setUnicodeFlag(true);
		
		if (smoothRenderingMode > 0){
			gui.mc.fontRenderer.drawSplitString(str,x,y,maxWidth,smoothColor);
			
			GL11.glTranslatef(-0.2F,0F,0F);
			gui.mc.fontRenderer.drawSplitString(str,x,y,maxWidth,smoothColor);
			GL11.glTranslatef(0.2F,0F,0F);
			
			GL11.glTranslatef(0F,smoothRenderingMode == 1 ? -0.2F : 0.2F,0F);
			gui.mc.fontRenderer.drawSplitString(str,x,y,maxWidth,smoothColor);
			GL11.glTranslatef(0F,smoothRenderingMode == 1 ? 0.2F : -0.2F,0F);
		}
		else gui.mc.fontRenderer.drawSplitString(str,x,y,maxWidth,normalColor);
		
		gui.mc.fontRenderer.setUnicodeFlag(origFont);
	}
}
