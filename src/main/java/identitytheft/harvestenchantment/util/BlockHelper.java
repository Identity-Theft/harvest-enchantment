package identitytheft.harvestenchantment.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import identitytheft.harvestenchantment.HarvestEnchantmentConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;


public class BlockHelper {

    public static boolean isVanilla(Block block) {
        return Registries.BLOCK.getKey(block).get().getValue().getNamespace().equals("minecraft");
    }

    public static boolean isBottomBlock(Block block) {
        return Registries.BLOCK.getKey(block).get().getValue().getPath().contains("_bottom");
    }

    public static String[] parseBlockString(String blockString) {
        boolean inBracket = false;

        for (int i = 0; i < blockString.length(); i++) {
            char c = blockString.charAt(i);

            if (c == '[') {
                inBracket = true;
            }
            else if (c == ']') {
                inBracket = false;
            }
            else if (c == ',' && !inBracket) {
                return new String[] {
                    blockString.substring(0, i),
                    blockString.substring(i + 1)
                };
            }
        }

        return new String[] { blockString };
    }

    public static BlockState fromString(String key) {
        try {
            BlockArgumentParser.BlockResult result =
                    BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), new StringReader(key), false);

            return result.blockState();
        } catch (CommandSyntaxException e) {
            return Blocks.AIR.getDefaultState();
        }
    }

    public static BlockState getToolModifiedState(BlockState state, ItemUsageContext context) {
        Block block = state.getBlock();
        if (block == Blocks.ROOTED_DIRT) {
            return Blocks.DIRT.getDefaultState();
        } else if ((block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.DIRT || block == Blocks.COARSE_DIRT) && context.getWorld().getBlockState(context.getBlockPos().up()).isAir())
            return block == Blocks.COARSE_DIRT ? Blocks.DIRT.getDefaultState() : Blocks.FARMLAND.getDefaultState();

        return null;
    }

    public static InteractionType getInteractionTypeForBlock(BlockState state, boolean canRightClick) {
        if (canRightClick && HarvestEnchantmentConfig.getRightClickBlocks().contains(state.getBlock())) {
            return InteractionType.CLICK;
        }
        else if (HarvestEnchantmentConfig.getCrops().containsKey(state)) {
            return InteractionType.HARVEST;
        }

        return InteractionType.NONE;
    }

    public enum InteractionType {
        NONE, CLICK, HARVEST;
    }
}
