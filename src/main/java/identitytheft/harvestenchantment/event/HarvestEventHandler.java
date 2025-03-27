/**
 * This class was derived from Quark Mod and written by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github: https://github.com/Vazkii/Quark
 *
 * https://github.com/VazkiiMods/Quark/blob/master/src/main/java/org/violetmoon/quark/content/tweaks/module/SimpleHarvestModule.java
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 */
package identitytheft.harvestenchantment.event;

import identitytheft.harvestenchantment.HarvestEnchantmentConfig;
import identitytheft.harvestenchantment.enchantment.ModEnchantments;
import identitytheft.harvestenchantment.util.BlockHelper;
import identitytheft.harvestenchantment.util.ToolHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;

import org.jetbrains.annotations.Nullable;

public class HarvestEventHandler {

	private static boolean isHarvesting = false;

	public static ClickResult rightClickBlock(PlayerEntity player, Hand hand, BlockPos pos, BlockHitResult hitResult) {
		if (player.getWorld().isClient() || isHarvesting) return ClickResult.pass();

		isHarvesting = true;

		ClickResult result = getClickResult(player, hand, pos, hitResult);

		isHarvesting = false;

		return result;
	}

	private static ClickResult getClickResult(PlayerEntity player, Hand hand, BlockPos pos, BlockHitResult hitResult) {
		if (player == null || hand == null || player.isSpectator()) return ClickResult.pass();
		if (hitResult.getType() != HitResult.Type.BLOCK || !hitResult.getBlockPos().equals(pos)) return ClickResult.pass();

		World world = player.getWorld();
		BlockState blockState = world.getBlockState(pos);
		BlockState modifiedState = BlockHelper.getToolModifiedState(blockState, new ItemUsageContext(player, hand, hitResult));

		if (modifiedState != null) return ClickResult.pass();

		ItemStack heldStack = player.getStackInHand(hand);
		boolean isHoe = ToolHelper.isHoe(heldStack);

		if (!isHoe) return ClickResult.pass();

		BlockState above = world.getBlockState(pos.up());
		int range = HarvestEnchantmentConfig.HARVEST_RANGE_PER_LEVEL * EnchantmentHelper.getLevel(ModEnchantments.HARVESTING, heldStack);

		if (BlockHelper.getInteractionTypeForBlock(blockState, true) == BlockHelper.InteractionType.NONE
				&& BlockHelper.getInteractionTypeForBlock(above, true ) == BlockHelper.InteractionType.NONE) {
			return ClickResult.pass();
		}

		boolean harvested = false;

		for (int x = 1 - range; x < range; x++) {
			for (int z = 1 - range; z < range; z++) {
				BlockPos shiftPos = pos.subtract(new Vec3i(-x, 0, -z));

				if (!tryHarvest(world, shiftPos, player, hand, range > 1)) {
					shiftPos = shiftPos.up();

					if (tryHarvest(world, shiftPos, player, hand, range > 1)) {
						harvested = true;
					}
				}
				else {
					harvested = true;
				}
			}
		}

		if (!harvested) return ClickResult.pass();

		return ClickResult.interrupt();
	}

	private static boolean tryHarvest(World world, BlockPos pos, @Nullable LivingEntity entity, @Nullable Hand hand, boolean canReach) {
		if (entity instanceof PlayerEntity player && (!world.canPlayerModifyAt(player, pos))) {
			return false;
		}

		BlockState blockState = world.getBlockState(pos);
		BlockHelper.InteractionType interactionType = BlockHelper.getInteractionTypeForBlock(blockState, canReach);

		if (interactionType != BlockHelper.InteractionType.NONE) {
			if (interactionType == BlockHelper.InteractionType.HARVEST) {
				if (entity instanceof PlayerEntity) {
					return harvestAndReplant(world, pos, blockState, entity, hand);
				}
			}
			else if (interactionType == BlockHelper.InteractionType.CLICK && entity instanceof PlayerEntity) {
				BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, true);

				if (hand == null) hand = Hand.MAIN_HAND;

				if (entity instanceof ServerPlayerEntity sp) {
					return sp.interactionManager.interactBlock(sp, sp.getWorld(), sp.getStackInHand(hand), hand, hitResult).isAccepted();
				}
			}
		}

		return false;
	}

	private static boolean harvestAndReplant(World world, BlockPos pos, BlockState blockState, LivingEntity entity, Hand hand) {
		BlockState cropBlockState = HarvestEnchantmentConfig.getCrops().get(blockState);
		BlockState above = world.getBlockState(pos.up());

		if (above.getBlock() instanceof CropBlock) {
			cropBlockState = HarvestEnchantmentConfig.getCrops().get(above);
		}

		if (cropBlockState == null) return false;

		if (world instanceof ServerWorld serverWorld) {
			ItemStack copy;
			ItemStack heldStack = null;

			if (entity == null || hand == null) {
				copy = new ItemStack(Items.STICK);
			}
			else {
				heldStack = entity.getStackInHand(hand);
				copy = entity.getStackInHand(hand).copy();
			}

			MutableBoolean hasTaken = new MutableBoolean(false);
			Item blockItem = blockState.getBlock().asItem();
			Block.getDroppedStacks(blockState, serverWorld, pos, world.getBlockEntity(pos), entity, copy).forEach((stack) -> {
				if (stack.getItem() == blockItem && !hasTaken.getValue()) {
					stack.decrement(1);
					hasTaken.setValue(true);
				}

				if (!stack.isEmpty()) {
					Block.dropStack(world, pos, stack);
				}
			});

			blockState.onStacksDropped(serverWorld, pos, copy, false);

			world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(cropBlockState));
			world.setBlockState(pos, cropBlockState);
			world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(entity, blockState));

			if (heldStack != null && !world.isClient() && HarvestEnchantmentConfig.DAMAGE_TOOL) {
				heldStack.damage(1, entity, (p) -> p.sendToolBreakStatus(Hand.MAIN_HAND));
			}
		}

		return true;
	}

}
