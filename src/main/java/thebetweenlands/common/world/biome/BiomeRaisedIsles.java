package thebetweenlands.common.world.biome;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import thebetweenlands.api.entity.spawning.ICustomSpawnEntry;
import thebetweenlands.common.entity.mobs.EntityAngler;
import thebetweenlands.common.entity.mobs.EntityBlindCaveFish;
import thebetweenlands.common.entity.mobs.EntityBoulderSprite;
import thebetweenlands.common.entity.mobs.EntityChiromaw;
import thebetweenlands.common.entity.mobs.EntityDragonFly;
import thebetweenlands.common.entity.mobs.EntityFirefly;
import thebetweenlands.common.entity.mobs.EntityFrog;
import thebetweenlands.common.entity.mobs.EntityGecko;
import thebetweenlands.common.entity.mobs.EntityLurker;
import thebetweenlands.common.entity.mobs.EntityRootSprite;
import thebetweenlands.common.entity.mobs.EntitySporeling;
import thebetweenlands.common.entity.mobs.EntitySwampHag;
import thebetweenlands.common.entity.mobs.EntityWight;
import thebetweenlands.common.lib.ModInfo;
import thebetweenlands.common.registries.BlockRegistry;
import thebetweenlands.common.world.WorldProviderBetweenlands;
import thebetweenlands.common.world.biome.spawning.spawners.BetweenstoneCaveSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.CaveSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.GreeblingSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.SporelingSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.SurfaceSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.SwampHagCaveSpawnEntry;
import thebetweenlands.common.world.gen.biome.decorator.BiomeDecoratorCoarseIslands;
import thebetweenlands.common.world.gen.biome.feature.AlgaeFeature;
import thebetweenlands.common.world.gen.biome.feature.CoarseIslandsFeature;

public class BiomeRaisedIsles extends BiomeBetweenlands {

	public BiomeRaisedIsles() {
		super(new ResourceLocation(ModInfo.ID, "raised_isles"), 
				new BiomeProperties("Raised Isles")
				.setBaseHeight(WorldProviderBetweenlands.LAYER_HEIGHT - 5)
				.setHeightVariation(4.0F)
				.setWaterColor(0x1b3944)
				.setTemperature(0.8F)
				.setRainfall(0.9F));
		this.setWeight(16);
		this.getBiomeGenerator().setDecorator(new BiomeDecoratorCoarseIslands(this))
		.addFeature(new CoarseIslandsFeature())
		.addFeature(new AlgaeFeature());
		this.setFoliageColors(-1, 0xA8A800);
		this.setSecondaryFoliageColors(-1, 0xa87800);
	}

	@Override
	public void addTypes() {
		BiomeDictionary.addTypes(this, Type.SWAMP, Type.WET, Type.WATER);
	}

	@Override
	protected void addSpawnEntries(List<ICustomSpawnEntry> entries) {
		super.addSpawnEntries(entries);

		entries.add(new SurfaceSpawnEntry(0, EntityDragonFly.class, EntityDragonFly::new, (short) 36).setCanSpawnOnWater(true).setGroupSize(1, 3).setSpawnCheckRadius(64.0D).setSpawningInterval(400));
		entries.add(new SurfaceSpawnEntry(1, EntityFirefly.class, EntityFirefly::new, (short) 60).setCanSpawnOnWater(true).setSpawnCheckRadius(32.0D));
		entries.add(new SurfaceSpawnEntry(2, EntityGecko.class, EntityGecko::new, (short) 52).setGroupSize(1, 3).setSpawnCheckRadius(32.0D).setSpawningInterval(600));
		entries.add(new SurfaceSpawnEntry(3, EntityFrog.class, EntityFrog::new, (short) 30).setCanSpawnOnWater(true).setGroupSize(1, 3).setSpawnCheckRadius(32.0D).setSpawningInterval(100));
		entries.add(new CaveSpawnEntry(4, EntityBlindCaveFish.class, EntityBlindCaveFish::new, (short) 30).setCanSpawnInWater(true).setGroupSize(3, 5).setSpawnCheckRadius(32.0D));
		entries.add(new SporelingSpawnEntry(5, EntitySporeling.class, EntitySporeling::new, (short) 80).setGroupSize(2, 5).setSpawnCheckRadius(32.0D));
		entries.add(new SurfaceSpawnEntry(16, EntityRootSprite.class, EntityRootSprite::new, (short) 70).setSurfacePredicate(state -> state.getBlock() == BlockRegistry.GIANT_ROOT).setGroupSize(1, 3).setSpawnCheckRadius(32.0D).setSpawningInterval(200));
		entries.add(new GreeblingSpawnEntry(17, (short) 20).setGroupSize(1, 3).setSpawnCheckRadius(64.0D).setGroupSpawnRadius(4).setSpawningInterval(24000));
		
		entries.add(new SurfaceSpawnEntry(6, EntityLurker.class, EntityLurker::new, (short) 32).setCanSpawnInWater(true).setHostile(true).setSpawnCheckRadius(16.0D));
		entries.add(new SurfaceSpawnEntry(7, EntityAngler.class, EntityAngler::new, (short) 42).setCanSpawnInWater(true).setHostile(true).setGroupSize(1, 3));
		entries.add(new CaveSpawnEntry(8, EntityAngler.class, EntityAngler::new, (short) 45).setCanSpawnInWater(true).setHostile(true).setGroupSize(1, 3));
		entries.add(new SurfaceSpawnEntry(9, EntitySwampHag.class, EntitySwampHag::new, (short) 100).setHostile(true));
		entries.add(new SwampHagCaveSpawnEntry(10, (short) 120).setHostile(true).setSpawnCheckRadius(24.0D).setGroupSize(1, 3));
		entries.add(new SurfaceSpawnEntry(11, EntityWight.class, EntityWight::new, (short) 8).setHostile(true).setSpawnCheckRadius(64.0D).setSpawnCheckRangeY(16.0D).setSpawningInterval(6000));
		entries.add(new CaveSpawnEntry(12, EntityWight.class, EntityWight::new, (short) 20).setHostile(true).setSpawnCheckRadius(64.0D));
		entries.add(new SurfaceSpawnEntry(13, EntityChiromaw.class, EntityChiromaw::new, (short) 40).setHostile(true).setSpawnCheckRadius(30.0D));
		entries.add(new CaveSpawnEntry(14, EntityChiromaw.class, EntityChiromaw::new, (short) 60).setHostile(true).setSpawnCheckRadius(20.0D).setGroupSize(1, 3));
		entries.add(new BetweenstoneCaveSpawnEntry(15, EntityBoulderSprite.class, EntityBoulderSprite::new, (short) 60).setHostile(true).setSpawnCheckRadius(16.0D).setSpawnCheckRangeY(8));
	}
}
