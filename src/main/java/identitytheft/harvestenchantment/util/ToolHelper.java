package identitytheft.harvestenchantment.util;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;


public class ToolHelper {
    public static boolean isHoe(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof HoeItem || stack.isIn(ItemTags.HOES));
    }
}
