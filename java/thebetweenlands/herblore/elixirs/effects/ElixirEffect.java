package thebetweenlands.herblore.elixirs.effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import thebetweenlands.utils.PotionHelper;

public class ElixirEffect {
	private static class ElixirAttributeModifier {
		private final IAttribute attribute;
		private final String uuid;
		private final double modifier;
		private final int operation;
		private ElixirAttributeModifier(IAttribute attribute, String uuid, double modifier, int operation) {
			this.attribute = attribute;
			this.uuid = uuid;
			this.modifier = modifier;
			this.operation = operation;
		}
	}

	private static class ElixirPotionEffect extends Potion {
		private final ElixirEffect effect;
		private final ResourceLocation icon;

		protected ElixirPotionEffect(ElixirEffect effect, String name, int color, ResourceLocation icon) {
			super(effect.potionID, false, color);
			this.setPotionName(name);
			this.effect = effect;
			this.icon = icon;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean hasStatusIcon() {
			return this.icon != null;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
			if(this.icon != null) {
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				Minecraft.getMinecraft().renderEngine.bindTexture(this.icon);
				Tessellator tessellator = Tessellator.instance;
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV(x+6, y+6, 0, 0, 0);
				tessellator.addVertexWithUV(x+6, y+6+20, 0, 0, 1);
				tessellator.addVertexWithUV(x+6+20, y+6+20, 0, 1, 1);
				tessellator.addVertexWithUV(x+6+20, y+6, 0, 1, 0);
				tessellator.draw();
			}
		}

		@Override
		public boolean isReady(int ticks, int strength) {
			return this.id == this.effect.potionID ? this.effect.isReady(ticks, strength) : false;
		}

		@Override
		public void performEffect(EntityLivingBase entity, int strength) {
			if(this.id == this.effect.potionID) this.effect.performEffect(entity, strength);
		}

		@Override
		public void affectEntity(EntityLivingBase attacker, EntityLivingBase target, int strength, double distance) { 
			if(this.id == this.effect.potionID) this.effect.affectEntity(attacker, target, strength, distance);
		}

		@Override
		public double func_111183_a(int strength, AttributeModifier attributeModifier) {
			if(this.id == this.effect.potionID) return this.effect.getAttributeModifier(attributeModifier, strength);
			return super.func_111183_a(strength, attributeModifier);
		}
	}

	public PotionEffect createEffect(int duration, int strength) {
		return new PotionEffect(this.potionID, duration, strength);
	}

	private List<ElixirAttributeModifier> elixirAttributeModifiers = new ArrayList<ElixirAttributeModifier>();
	private ElixirPotionEffect potionEffect;
	private final String effectName;
	private int potionID;
	private final int effectID;
	private final ResourceLocation icon;
	private final int color;

	public ElixirEffect(int id, String name) {
		this(id, name, null, 0x00000000);
	}

	public ElixirEffect(int id, String name, ResourceLocation icon) {
		this(id, name, icon, 0x00000000);
	}

	public ElixirEffect(int id, String name, int color) {
		this(id, name, null, color);
	}

	public ElixirEffect(int id, String name, ResourceLocation icon, int color) {
		this.effectID = id;
		this.effectName = name;
		this.icon = icon;
		this.color = color;
	}

	public void registerPotion() {
		this.potionID = PotionHelper.getFreePotionId();
		this.potionEffect = new ElixirPotionEffect(this, this.effectName, this.color, this.icon);
		for(ElixirAttributeModifier modifier : this.elixirAttributeModifiers) {
			this.potionEffect.func_111184_a(modifier.attribute, modifier.uuid, modifier.modifier, modifier.operation);
		}
	}

	public int getID() {
		return this.effectID;
	}

	public String getEffectName() {
		return this.effectName;
	}

	public ResourceLocation getIcon() {
		return this.potionEffect.icon;
	}

	/**
	 * Whether this effect should be applied this tick
	 */
	protected boolean isReady(int ticks, int strength) {
		return true;
	}

	/**
	 * Effect over time
	 */
	protected void performEffect(EntityLivingBase entity, int strength) { }

	/**
	 * Instant effect
	 */
	protected void affectEntity(EntityLivingBase attacker, EntityLivingBase target, int strength, double distance) { }

	/**
	 * Calculates the modifier from the attribute and elixir strength
	 * @param attributeModifier
	 * @param strength
	 * @return
	 */
	protected double getAttributeModifier(AttributeModifier attributeModifier, int strength) {
		return attributeModifier.getAmount() * (double)(strength + 1);
	}

	/**
	 * Adds an entity attribute modifier that is applied when the potion is active.
	 * @param attribute
	 * @param uuid
	 * @param modifier
	 * @param operation
	 * @return
	 */
	public ElixirEffect addAttributeModifier(IAttribute attribute, String uuid, double modifier, int operation) {
		if(this.potionEffect != null) {
			this.potionEffect.func_111184_a(attribute, uuid, modifier, operation);
		} else {
			this.elixirAttributeModifiers.add(new ElixirAttributeModifier(attribute, uuid, modifier, operation));
		}
		return this;
	}

	public boolean isActive(EntityLivingBase entity) {
		Collection<PotionEffect> activePotions = entity.getActivePotionEffects();
		for(PotionEffect effect : activePotions) {
			if(effect.getPotionID() == this.potionID) {
				return true;
			}
		}
		return false;
	}

	public int getDuration(EntityLivingBase entity) {
		Collection<PotionEffect> activePotions = entity.getActivePotionEffects();
		for(PotionEffect effect : activePotions) {
			if(effect.getPotionID() == this.potionID) {
				return effect.getDuration();
			}
		}
		return -1;
	}

	public int getStrength(EntityLivingBase entity) {
		Collection<PotionEffect> activePotions = entity.getActivePotionEffects();
		for(PotionEffect effect : activePotions) {
			if(effect.getPotionID() == this.potionID) {
				return effect.getAmplifier();
			}
		}
		return -1;
	}
}
