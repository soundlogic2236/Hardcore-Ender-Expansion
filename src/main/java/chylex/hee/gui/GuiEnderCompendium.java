package chylex.hee.gui;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.stack.array.TByteArrayStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import chylex.hee.gui.helpers.AnimatedFloat;
import chylex.hee.gui.helpers.AnimatedFloat.Easing;
import chylex.hee.gui.helpers.GuiEndPortalRenderer;
import chylex.hee.gui.helpers.GuiItemRenderHelper;
import chylex.hee.gui.helpers.GuiItemRenderHelper.ITooltipRenderer;
import chylex.hee.item.ItemList;
import chylex.hee.mechanics.compendium.content.KnowledgeCategories;
import chylex.hee.mechanics.compendium.content.KnowledgeRegistrations;
import chylex.hee.mechanics.compendium.content.objects.IKnowledgeObjectInstance;
import chylex.hee.mechanics.compendium.content.type.KnowledgeCategory;
import chylex.hee.mechanics.compendium.content.type.KnowledgeFragment;
import chylex.hee.mechanics.compendium.content.type.KnowledgeObject;
import chylex.hee.mechanics.compendium.player.PlayerCompendiumData;
import chylex.hee.mechanics.compendium.render.CategoryDisplayElement;
import chylex.hee.mechanics.compendium.render.ObjectDisplayElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnderCompendium extends GuiScreen implements ITooltipRenderer{
	public static final int guiPageTexWidth = 152, guiPageTexHeight = 226, guiObjectTopY = 400;
	
	public static final RenderItem renderItem = new RenderItem();
	public static final ResourceLocation texPage = new ResourceLocation("hardcoreenderexpansion:textures/gui/knowledge_book.png");
	public static final ResourceLocation texBack = new ResourceLocation("hardcoreenderexpansion:textures/gui/ender_compendium_back.png");
	private static final ItemStack knowledgeFragmentIS = new ItemStack(ItemList.knowledge_fragment);
	
	private static float ptt(float value, float prevValue, float partialTickTime){
		return prevValue+(value-prevValue)*partialTickTime;
	}
	
	private final PlayerCompendiumData compendiumData;
	
	private GuiEndPortalRenderer portalRenderer;
	private List<AnimatedFloat> animationList = new ArrayList<>();
	private AnimatedFloat offsetX, offsetY, portalScale;
	private float prevOffsetX, prevOffsetY, prevPortalScale;
	private int prevMouseX, prevMouseY;
	
	private List<CategoryDisplayElement> categoryElements = new ArrayList<>();
	private List<ObjectDisplayElement> objectElements = new ArrayList<>();
	private boolean hasHighlightedCategory = false;
	private KnowledgeObject<? extends IKnowledgeObjectInstance<?>> currentObject = null;
	private TByteObjectHashMap<Set<KnowledgeFragment>> currentObjectPages = new TByteObjectHashMap<>(5);
	
	private byte pageIndexCurrent;
	private TByteArrayStack pageIndexPrev = new TByteArrayStack(5);
	private GuiButton[] pageArrows = new GuiButton[2];
	
	public GuiEnderCompendium(PlayerCompendiumData compendiumData){
		this.compendiumData = compendiumData;
		animationList.add(offsetX = new AnimatedFloat(Easing.CUBIC));
		animationList.add(offsetY = new AnimatedFloat(Easing.CUBIC));
		animationList.add(portalScale = new AnimatedFloat(Easing.LINEAR));
		
		int alphaDelay = 10;
		for(KnowledgeCategory category:KnowledgeCategories.categoryList)categoryElements.add(new CategoryDisplayElement(category,alphaDelay += 2));
		
		portalScale.startAnimation(2.5F,1F,0.6F);
	}
	
	@Override
	public void initGui(){
		this.portalRenderer = new GuiEndPortalRenderer(this,width-48,height-48,0);
		
		buttonList.add(new GuiButton(0,(width>>1)-120,height-48+21,98,20,I18n.format("gui.back")));
		buttonList.add(new GuiButton(1,(width>>1)-10,height-48+21,20,20,"?"));
		buttonList.add(new GuiButton(2,(width>>1)+22,height-48+21,98,20,I18n.format("gui.done")));
		
		offsetX.set(width>>1);
		offsetY.set(height>>1);
	}
	
	@Override
	protected void actionPerformed(GuiButton button){
		if (!(button.enabled && button.visible))return;
		
		if (button.id == 0){
			if (hasHighlightedCategory){
				offsetY.startAnimation(offsetY.value(),height>>1);
				hasHighlightedCategory = false;
			}
			else{
				mc.displayGuiScreen((GuiScreen)null);
				mc.setIngameFocus();
			}
		}
		else if (button.id == 1)showObject(KnowledgeRegistrations.HELP);
		else if (button.id == 2){
			mc.displayGuiScreen((GuiScreen)null);
			mc.setIngameFocus();
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int buttonId){
		if (buttonId == 1)actionPerformed((GuiButton)buttonList.get(0));
		else if (buttonId == 0){
			if (!hasHighlightedCategory){
				for(CategoryDisplayElement element:categoryElements){
					if (element.isMouseOver(mouseX,mouseY,(int)offsetX.value(),(int)offsetY.value())){
						for(KnowledgeObject object:element.category.getAllObjects())objectElements.add(new ObjectDisplayElement(object));
						offsetY.startAnimation(offsetY.value(),-guiObjectTopY);
						hasHighlightedCategory = true;
						return;
					}
				}
			}
			else{
				int offX = (int)offsetX.value()-(width>>1), offY = (int)offsetY.value()+guiObjectTopY;
				
				for(ObjectDisplayElement element:objectElements){
					if (element.isMouseOver(mouseX,mouseY,offX,offY)){
						showObject(element.object);
					}
				}
			}
		}
		
		super.mouseClicked(mouseX,mouseY,buttonId);
	}
	
	@Override
	protected void keyTyped(char key, int keyCode){
		if (keyCode == 1)actionPerformed((GuiButton)buttonList.get(0));
	}
	
	@Override
	public void updateScreen(){
		prevOffsetX = offsetX.value();
		prevOffsetY = offsetY.value();
		prevPortalScale = portalScale.value();
		
		for(AnimatedFloat animation:animationList)animation.update(0.05F);
		for(CategoryDisplayElement element:categoryElements)element.update();
	}
	
	private void showObject(KnowledgeObject<? extends IKnowledgeObjectInstance<?>> object){
		if (currentObject != null){
			
		}
		
		currentObject = object;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTickTime){
		drawDefaultBackground();
		GL11.glDepthFunc(GL11.GL_GEQUAL);
		GL11.glPushMatrix();
		GL11.glTranslatef(0F,0F,-200F);
		portalRenderer.draw(ptt(offsetX.value(),prevOffsetX,partialTickTime),-ptt(offsetY.value(),prevOffsetY,partialTickTime),ptt(portalScale.value(),prevPortalScale,partialTickTime));
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_CULL_FACE);
		renderScreen(mouseX,mouseY,partialTickTime);
		GL11.glPopMatrix();
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		super.drawScreen(mouseX,mouseY,partialTickTime);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	private void renderScreen(int mouseX, int mouseY, float partialTickTime){
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glColor4f(1F,1F,1F,1F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);

		renderBackgroundGUI();
		
		float offX = ptt(offsetX.value(),prevOffsetX,partialTickTime);
		float offY = ptt(offsetY.value(),prevOffsetY,partialTickTime);
		
		GL11.glPushMatrix();
		GL11.glTranslatef(offX,offY,0F);
		for(CategoryDisplayElement element:categoryElements)element.render(this,0,0,partialTickTime);
		RenderHelper.disableStandardItemLighting();
		GL11.glPopMatrix();
		
		for(CategoryDisplayElement element:categoryElements){
			if (element.isMouseOver(mouseX,mouseY,(int)offX,(int)offY)){
				GuiItemRenderHelper.drawTooltip(this,fontRendererObj,mouseX,mouseY,element.category.getTooltip());
			}
		}
		
		offX = ptt(offsetX.value(),prevOffsetX,partialTickTime)-(width>>1);
		offY = ptt(offsetY.value(),prevOffsetY,partialTickTime)+guiObjectTopY;

		GL11.glPushMatrix();
		GL11.glTranslatef(offX,offY,0F);
		for(ObjectDisplayElement element:objectElements)element.render(this,compendiumData,0,0);
		RenderHelper.disableStandardItemLighting();
		GL11.glPopMatrix();
		
		for(ObjectDisplayElement element:objectElements){
			if (element.isMouseOver(mouseX,mouseY,(int)offX,(int)offY)){
				String tooltip = element.object.getTooltip();
				//if (!compendiumData.hasDiscoveredObject(element.object))tooltip += "\n\nNot discovered yet.";
				GuiItemRenderHelper.drawTooltip(this,fontRendererObj,mouseX,mouseY,tooltip);
			}
		}
		
		if (currentObject == KnowledgeRegistrations.HELP){
			renderPaperBackground(width>>1,height>>1);
		}
		else if (currentObject != null){
			renderPaperBackground((width>>1)+(width>>2),height>>1);
		}

		renderFragmentCount((width>>1)-25,24);
		
		prevMouseX = mouseX;
		prevMouseY = mouseY;
	}
	
	private void renderBackgroundGUI(){
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		RenderHelper.disableStandardItemLighting();
		mc.getTextureManager().bindTexture(texBack);
		
		int d = 24;
		
		for(int a = 0, amt = ((width-d*2)>>2)-1; a < amt; a++){
			drawTexturedModalRect(d+8+4*a,d-16,50,0,4,24);
			drawTexturedModalRect(d+8+4*a,height-d-8,50,25,4,24);
		}
		
		for(int a = 0, amt = ((height-d*2)>>2)-1; a < amt; a++){
			drawTexturedModalRect(d-16,d+8+4*a,206,0,24,4);
			drawTexturedModalRect(width-d-8,d+8+4*a,232,0,24,4);
		}
		
		drawTexturedModalRect(d-16,d-16,0,0,24,24);
		drawTexturedModalRect(width-d-8,d-16,25,0,24,24);
		drawTexturedModalRect(d-16,height-d-8,0,25,24,24);
		drawTexturedModalRect(width-d-8,height-d-8,25,25,24,24);
		
		String title = "Hardcore Ender Expansion - Ender Compendium";
		fontRendererObj.drawString(title,(width>>1)-(fontRendererObj.getStringWidth(title)>>1),14,4210752);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	private void renderFragmentCount(int x, int y){
		GL11.glColor4f(1F,1F,1F,1F);
		mc.getTextureManager().bindTexture(texBack);
		drawTexturedModalRect(x,y,56,0,56,20);
		
		RenderHelper.enableGUIStandardItemLighting();
		renderItem.renderItemIntoGUI(fontRendererObj,mc.getTextureManager(),knowledgeFragmentIS,x+3,y+1);
		
		String pointAmount = String.valueOf(compendiumData.getPoints());
		fontRendererObj.drawString(pointAmount,x+50-fontRendererObj.getStringWidth(pointAmount),y+6,4210752);
	}
	
	private void renderPaperBackground(int x, int y){
		GL11.glColor4f(1F,1F,1F,1F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderHelper.disableStandardItemLighting();
		
		mc.getTextureManager().bindTexture(texPage);
		drawTexturedModalRect(x-(guiPageTexWidth>>1),y-(guiPageTexHeight>>1),0,0,guiPageTexWidth,guiPageTexHeight);
		
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}

	@Override
	public void setZLevel(float newZLevel){
		this.zLevel = newZLevel;
	}

	@Override
	public void callDrawGradientRect(int x1, int y1, int x2, int y2, int color1, int color2){
		drawGradientRect(x1,y1,x2,y2,color1,color2);
	}
}
