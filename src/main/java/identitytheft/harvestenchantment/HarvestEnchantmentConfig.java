package identitytheft.harvestenchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.midnightdust.lib.config.MidnightConfig;
import identitytheft.harvestenchantment.util.BlockHelper;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HarvestEnchantmentConfig extends MidnightConfig {
	public static final String HARVESTABLES = "harvestables";
	public static final String ENCHANTMENT = "enchantment";

	@Entry(category = HARVESTABLES) public static boolean AUTO_CONFIG_MODS = true;
	@Entry(category = HARVESTABLES) public static List<String> HARVESTABLE_CROPS = Lists.newArrayList(
			"minecraft:wheat[age=7]",
			"minecraft:carrots[age=7]",
			"minecraft:potatoes[age=7]",
			"minecraft:beetroots[age=3]",
			"minecraft:nether_wart[age=3]",
			"minecraft:cocoa[age=2,facing=north],minecraft:cocoa[age=0,facing=north]",
			"minecraft:cocoa[age=2,facing=south],minecraft:cocoa[age=0,facing=south]",
			"minecraft:cocoa[age=2,facing=east],minecraft:cocoa[age=0,facing=east]",
			"minecraft:cocoa[age=2,facing=west],minecraft:cocoa[age=0,facing=west]"
	);

	@Entry(category = ENCHANTMENT) public static boolean DAMAGE_TOOL = false;
	@Entry(category = ENCHANTMENT, min = 1, max = 5) public static int MAX_ENCHANTMENT_LEVEL = 3;
	@Entry(category = ENCHANTMENT, min = 1, max = 3) public static int HARVEST_RANGE_PER_LEVEL = 1;

	private static final Map<BlockState, BlockState> CROPS = Maps.newHashMap();
	private static final Set<Block> RIGHT_CLICK_BLOCKS = Sets.newHashSet();

	public static void initConfig() {
		MidnightConfig.init(HarvestEnchantment.MOD_ID, HarvestEnchantmentConfig.class);

		BooleanProperty upper = BooleanProperty.of("upper");

		CROPS.clear();
		RIGHT_CLICK_BLOCKS.clear();

		if (HarvestEnchantmentConfig.AUTO_CONFIG_MODS) {
			for (Block block : Registries.BLOCK) {
				if (!BlockHelper.isVanilla(block)) {
					if (block instanceof CropBlock cropBlock) {
						BlockState cropBlockstate = cropBlock.getDefaultState();
						BlockState maxAgeCropBlockstate = cropBlock.withAge(cropBlock.getMaxAge());

						if (cropBlockstate.contains(upper)) {
							cropBlockstate = cropBlockstate.with(upper, true);
							maxAgeCropBlockstate = maxAgeCropBlockstate.with(upper, true);
						}

						if (BlockHelper.isBottomBlock(block)) continue;

						CROPS.put(maxAgeCropBlockstate, cropBlockstate);
					} else if ((block instanceof PlantBlock || block instanceof AbstractPlantPartBlock)
							&& block instanceof Fertilizable) {
						RIGHT_CLICK_BLOCKS.add(block);
					}
				}
			}
		}

		for (String cropKey : HARVESTABLE_CROPS) {
			BlockState initial;
			BlockState result;
			String[] parts = BlockHelper.parseBlockString(cropKey);

			initial = BlockHelper.fromString(cropKey);

			if (initial.getBlock() != Blocks.AIR) {
				if (parts.length > 1) {
					result = BlockHelper.fromString(parts[1]);
				}
				else {
					result = initial.getBlock().getDefaultState();
				}

				CROPS.put(initial, result);
			}
		}
	}

	public static Map<BlockState, BlockState> getCrops() {
		return CROPS;
	}

	public static Set<Block> getRightClickBlocks() {
		return RIGHT_CLICK_BLOCKS;
	}
}