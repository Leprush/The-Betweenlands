package thebetweenlands.event.debugging;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import thebetweenlands.TheBetweenlands;
import thebetweenlands.client.render.shader.BLShaderGroup;
import thebetweenlands.client.render.shader.ResourceManagerWrapper;
import thebetweenlands.manager.DecayManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DebugHandler {
	public static final DebugHandler INSTANCE = new DebugHandler();

	/////// DEBUG ///////
	private boolean fullBright = false;
	private boolean fastFlight = false;
	public boolean denseFog = false;
	public boolean useShader = false;
	private float lightTable[];
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if(!TheBetweenlands.DEBUG || Minecraft.getMinecraft().theWorld == null) return;
		if(Keyboard.isKeyDown(Keyboard.KEY_F)){
			this.fullBright = !this.fullBright;
			if(this.fullBright) {
				if(this.lightTable == null) {
					this.lightTable = new float[Minecraft.getMinecraft().theWorld.provider.lightBrightnessTable.length];
				}
				for(int i = 0; i < Minecraft.getMinecraft().theWorld.provider.lightBrightnessTable.length; i++) {
					this.lightTable[i] = Minecraft.getMinecraft().theWorld.provider.lightBrightnessTable[i];
				}
				for(int i = 0; i < Minecraft.getMinecraft().theWorld.provider.lightBrightnessTable.length; i++) {
					Minecraft.getMinecraft().theWorld.provider.lightBrightnessTable[i] = 1.0f;
				}
			} else {
				for(int i = 0; i < Minecraft.getMinecraft().theWorld.provider.lightBrightnessTable.length; i++) {
					Minecraft.getMinecraft().theWorld.provider.lightBrightnessTable[i] = this.lightTable[i];
				}
			}
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_C)) {
			this.fastFlight = !this.fastFlight;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_R)) {
			this.denseFog = !this.denseFog;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
			DecayManager.resetDecay(Minecraft.getMinecraft().thePlayer);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			try {
				Minecraft mc = Minecraft.getMinecraft();
				if(mc.entityRenderer.theShaderGroup == null) {
					//mc.entityRenderer.activateNextShader();
					mc.entityRenderer.theShaderGroup = new BLShaderGroup(mc.getTextureManager(), new ResourceManagerWrapper(mc.getResourceManager()), mc.getFramebuffer(), 
							new ResourceLocation("shaders/post/lighting.json"));
					//new ResourceLocation("shaders/post/notch.json"));
					//mc.entityRenderer.theShaderGroup.addShader("thebetweenlands:shaders/overlay", mc.getFramebuffer(), mc.getFramebuffer());
					mc.entityRenderer.theShaderGroup.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
				} else {
					mc.entityRenderer.deactivateShader();
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			//this.useShader = !this.useShader;
		}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTick(TickEvent event) {
		if(!TheBetweenlands.DEBUG || Minecraft.getMinecraft().thePlayer == null) return;
		if(this.fastFlight) {
			Minecraft.getMinecraft().thePlayer.capabilities.setFlySpeed(1.0f);
		} else {
			Minecraft.getMinecraft().thePlayer.capabilities.setFlySpeed(0.1f);
		}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
		if (TheBetweenlands.DEBUG) {
			Minecraft.getMinecraft().fontRenderer.drawString("Debug", 2, 2, 0xFFFFFFFF);
			Minecraft.getMinecraft().fontRenderer.drawString("Decay: " + DecayManager.getDecayLevel(Minecraft.getMinecraft().thePlayer), 2, 10, 0xFFFFFFFF);
			Minecraft.getMinecraft().fontRenderer.drawString("Corruption: " + DecayManager.getCorruptionLevel(Minecraft.getMinecraft().thePlayer), 2, 18, 0xFFFFFFFF);
		}
	}

	//This copies the depth buffer before it is cleared when rendering the hand
	public static Framebuffer depthBuffer;
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event) {
		//Create new framebuffer to hold the depth information
		if(depthBuffer == null) {
			depthBuffer = new Framebuffer(
					Minecraft.getMinecraft().getFramebuffer().framebufferWidth, 
					Minecraft.getMinecraft().getFramebuffer().framebufferHeight, 
					true);
		}
		//Draw to depth buffer
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, depthBuffer.framebufferObject);
		//Read from depth buffer
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, Minecraft.getMinecraft().getFramebuffer().framebufferObject);
		//Blit dat shit
		int width = Minecraft.getMinecraft().getFramebuffer().framebufferWidth;
		int height = Minecraft.getMinecraft().getFramebuffer().framebufferHeight;
		GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, 
				GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, 
				GL11.GL_NEAREST);
		//Bind default framebuffer
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	}
}
