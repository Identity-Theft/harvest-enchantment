package identitytheft.harvestenchantment;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.midnightdust.lib.config.MidnightConfig;
import identitytheft.harvestenchantment.enchantment.ModEnchantments;
import identitytheft.harvestenchantment.event.HarvestEventHandler;
import identitytheft.harvestenchantment.util.BlockHelper;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HarvestEnchantment implements ModInitializer {
	public static final String MOD_ID = "harvest-enchantment";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Start Harvest Enchantment!");

		HarvestEnchantmentConfig.initConfig();
		ModEnchantments.registerAll();
		UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> HarvestEventHandler.rightClickBlock(player, hand, hitResult.getBlockPos(), hitResult).getInteractionResult()));
	}
}