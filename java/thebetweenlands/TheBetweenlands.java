package thebetweenlands;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import thebetweenlands.blocks.BLBlockRegistry;
import thebetweenlands.blocks.BLFluidRegistry;
import thebetweenlands.command.server.CommandTickSpeed;
import thebetweenlands.commands.CommandToggleEvent;
import thebetweenlands.entities.BLEntityRegistry;
import thebetweenlands.event.entity.MiscEntitySyncHandler;
import thebetweenlands.event.entity.PowerRingHandler;
import thebetweenlands.event.player.*;
import thebetweenlands.event.world.EnvironmentEventHandler;
import thebetweenlands.items.BLItemRegistry;
import thebetweenlands.lib.ModInfo;
import thebetweenlands.network.base.IPacket;
import thebetweenlands.network.base.SidedPacketHandler;
import thebetweenlands.network.base.impl.CommonPacketProxy;
import thebetweenlands.network.base.impl.IDPacketObjectSerializer;
import thebetweenlands.network.message.MessageSyncEnvironmentEvent;
import thebetweenlands.network.message.MessageSyncPlayerDecay;
import thebetweenlands.network.packets.*;
import thebetweenlands.proxy.CommonProxy;
import thebetweenlands.recipes.RecipeHandler;
import thebetweenlands.utils.PotionHelper;
import thebetweenlands.utils.confighandler.ConfigHandler;
import thebetweenlands.world.WorldProviderBetweenlands;
import thebetweenlands.world.biomes.base.BLBiomeRegistry;
import thebetweenlands.world.feature.structure.WorldGenDruidCircle;
import thebetweenlands.world.teleporter.TeleporterHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION, guiFactory = ModInfo.CONFIG_GUI)
public class TheBetweenlands
{
	@SidedProxy(modId = ModInfo.ID, clientSide = ModInfo.CLIENTPROXY_LOCATION, serverSide = ModInfo.COMMONPROXY_LOCATION)
	public static CommonProxy proxy;

	@Instance(ModInfo.ID)
	public static TheBetweenlands instance;

	/// Network ///
	public static SimpleNetworkWrapper networkWrapper;
	public static final SidedPacketHandler sidedPacketHandler = new SidedPacketHandler();
	public static final IDPacketObjectSerializer packetRegistry = new IDPacketObjectSerializer();
	@SidedProxy(modId = ModInfo.ID, clientSide = ModInfo.CLIENTPACKETPROXY_LOCATION, serverSide = ModInfo.COMMONPACKETPROXY_LOCATION)
	public static CommonPacketProxy packetProxy;
    public static File dir;
    private static byte nextPacketId = 0;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		//Configuration File
		ConfigHandler.INSTANCE.loadConfig(event);
        dir = event.getModConfigurationDirectory();

		//BL Registry
		BLFluidRegistry.init();
		BLBlockRegistry.init();
		BLBiomeRegistry.init();
		BLItemRegistry.init();
		BLEntityRegistry.init();

		GameRegistry.registerWorldGenerator(new WorldGenDruidCircle(), 0);

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);

		DimensionManager.registerProviderType(ModInfo.DIMENSION_ID, WorldProviderBetweenlands.class, true);
		DimensionManager.registerDimension(ModInfo.DIMENSION_ID, ModInfo.DIMENSION_ID);

		//Message Registry
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.CHANNEL);
        networkWrapper.registerMessage(MessageSyncPlayerDecay.class, MessageSyncPlayerDecay.class, 2, Side.CLIENT);
        networkWrapper.registerMessage(MessageSyncPlayerDecay.class, MessageSyncPlayerDecay.class, 3, Side.SERVER);
        networkWrapper.registerMessage(MessageSyncEnvironmentEvent.class, MessageSyncEnvironmentEvent.class, 4, Side.CLIENT);

        sidedPacketHandler.setProxy(packetProxy).setNetworkWrapper(networkWrapper, 20, 21).setPacketSerializer(packetRegistry);

        //Packet Registry
        registerPacket(PacketDruidAltarProgress.class);
        registerPacket(PacketDruidTeleportParticle.class);
        registerPacket(PacketSnailHatchParticle.class);
        registerPacket(PacketTickspeed.class);
        registerPacket(PacketRevengeTarget.class);
        registerPacket(PacketAttackTarget.class);

	}

	private static void registerPacket(Class<? extends IPacket> packetClass) {
        packetRegistry.registerPacket(packetClass, nextPacketId++);
	}

	@EventHandler
	@SuppressWarnings("unchecked")
	public void init(FMLInitializationEvent event) {
		// Remove all the door recipes.
		// This is needed otherwise our doors will not be craftable due to the
		// recipe ordering
		List<IRecipe> doorRecipes = new ArrayList<IRecipe>();
		for (IRecipe recipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
			if (recipe != null) {
				ItemStack stack = recipe.getRecipeOutput();
				if (stack != null && stack.getItem() == Items.wooden_door)
					doorRecipes.add(recipe);
			}
		for (IRecipe recipe : doorRecipes)
			CraftingManager.getInstance().getRecipeList().remove(recipe);

		proxy.registerTileEntities();
		proxy.preInit();

		PotionHelper.initPotionArray();
		PotionHelper.registerPotions();
		System.out.println("boogaaa" + PotionHelper.petrify.id);

		FMLCommonHandler.instance().bus().register(ConfigHandler.INSTANCE);
		FMLCommonHandler.instance().bus().register(DecayEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(BLFluidRegistry.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new OctineArmorHandler());
		MinecraftForge.EVENT_BUS.register(new TorchPlaceEventHandler());
		MinecraftForge.EVENT_BUS.register(DecayEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(RottenFoodHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(BLItemRegistry.weedwoodBow);
		MinecraftForge.EVENT_BUS.register(new PlayerPortalHandler());
		MinecraftForge.EVENT_BUS.register(new PowerRingHandler());
		FMLCommonHandler.instance().bus().register(EnvironmentEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(EnvironmentEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new BonemealEventHandler());
		MinecraftForge.EVENT_BUS.register(new SiltCrabClipHandler());
		MinecraftForge.EVENT_BUS.register(new MiscEntitySyncHandler());

		RecipeHandler.init();
		TeleporterHandler.init();
		// Add the other door recipes back
		CraftingManager.getInstance().getRecipeList().addAll(doorRecipes);
	}

	@EventHandler
	@SuppressWarnings("unchecked")
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandToggleEvent());
		if (ConfigHandler.DEBUG) {
			event.registerServerCommand(new CommandTickSpeed());
		}
	}
}
