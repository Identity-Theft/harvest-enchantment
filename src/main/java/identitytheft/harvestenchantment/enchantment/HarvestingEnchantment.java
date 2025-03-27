package identitytheft.harvestenchantment.enchantment;

import identitytheft.harvestenchantment.HarvestEnchantmentConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

public class HarvestingEnchantment extends Enchantment {
	protected HarvestingEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
		super(weight, EnchantmentTarget.DIGGER, slotTypes);
	}

	@Override
	public int getMinPower(int level) {
		return 1 + 10 * (level - 1);
	}

	@Override
	public int getMaxPower(int level) {
		return super.getMinPower(level) + 50;
	}

	@Override
	public int getMaxLevel() {
		return HarvestEnchantmentConfig.MAX_ENCHANTMENT_LEVEL;
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return stack.isIn(ItemTags.HOES);
	}
}
