package identitytheft.harvestenchantment.enchantment;

import identitytheft.harvestenchantment.HarvestEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;


public class ModEnchantments {
	public static Enchantment HARVESTING;

	private static Enchantment register(String id, Enchantment enchantment) {
		return Registry.register(Registries.ENCHANTMENT, Identifier.of(HarvestEnchantment.MOD_ID, id), enchantment);
	}

	public static void registerAll()
	{
		HARVESTING = register("harvesting", new HarvestingEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
	}
}
