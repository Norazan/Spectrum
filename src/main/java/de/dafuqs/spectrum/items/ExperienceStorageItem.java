package de.dafuqs.spectrum.items;

import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.helpers.Support;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.random.Random;

public interface ExperienceStorageItem {
	
	/**
	 * Returns the amount of experience this item stack has stored
	 *
	 * @param itemStack The item stack
	 * @return The amount of stored experience
	 */
	static int getStoredExperience(ItemStack itemStack) {
		NbtCompound nbtCompound = itemStack.getNbt();
		if (nbtCompound == null || !nbtCompound.contains("stored_experience", NbtElement.INT_TYPE)) {
			return 0;
		} else {
			return nbtCompound.getInt("stored_experience");
		}
	}
	
	/**
	 * Adds amount experience to an ExperienceProviderItem stack.
	 * If the experience would be overflowing return the amount of experience that could not be stored
	 * @param itemStack The item stack
	 * @param amount    The amount of experience to store
	 * @param random    A random
	 * @return The overflow amount that could not be stored
	 */
	static int addStoredExperience(ItemStack itemStack, float amount, Random random) {
		if (amount > 0 && itemStack.getItem() instanceof ExperienceStorageItem experienceStorageItem) {
			int maxStorage = experienceStorageItem.getMaxStoredExperience(itemStack);
			
			int intAmount = Support.getIntFromDecimalWithChance(amount, random);
			
			NbtCompound nbtCompound = itemStack.getOrCreateNbt();
			if (!nbtCompound.contains("stored_experience", NbtElement.INT_TYPE)) {
				nbtCompound.putInt("stored_experience", intAmount);
				itemStack.setNbt(nbtCompound);
				return 0;
			} else {
				int existingStoredExperience = nbtCompound.getInt("stored_experience");
				int experienceOverflow = maxStorage - intAmount - existingStoredExperience;
				
				if (experienceOverflow < 0) {
					nbtCompound.putInt("stored_experience", maxStorage);
					itemStack.setNbt(nbtCompound);
					return -experienceOverflow;
				} else {
					nbtCompound.putInt("stored_experience", existingStoredExperience + intAmount);
					itemStack.setNbt(nbtCompound);
					return 0;
				}
			}
		} else {
			SpectrumCommon.logError("Tried to add stored Experience to a non-ExperienceStorageItem item: " + itemStack.getItem().getName().getString());
			return 0;
		}
	}
	
	/**
	 * Adds amount experience to an ExperienceProviderItem stack.
	 * If the experience would be overflowing return the amount of experience that could not be stored
	 *
	 * @param itemStack The item stack
	 * @param amount    The amount of experience to store
	 * @return The overflow amount that could not be stored
	 */
	static int addStoredExperience(ItemStack itemStack, int amount) {
		if (amount > 0 && itemStack.getItem() instanceof ExperienceStorageItem experienceStorageItem) {
			int maxStorage = experienceStorageItem.getMaxStoredExperience(itemStack);
			
			NbtCompound nbtCompound = itemStack.getOrCreateNbt();
			if (!nbtCompound.contains("stored_experience", NbtElement.INT_TYPE)) {
				nbtCompound.putInt("stored_experience", amount);
				itemStack.setNbt(nbtCompound);
				return 0;
			} else {
				int existingStoredExperience = nbtCompound.getInt("stored_experience");
				int experienceOverflow = maxStorage - amount - existingStoredExperience;
				
				if (experienceOverflow < 0) {
					nbtCompound.putInt("stored_experience", maxStorage);
					itemStack.setNbt(nbtCompound);
					return -experienceOverflow;
				} else {
					nbtCompound.putInt("stored_experience", existingStoredExperience + amount);
					itemStack.setNbt(nbtCompound);
					return 0;
				}
			}
		} else {
			SpectrumCommon.logError("Tried to add stored Experience to a non-ExperienceStorageItem item: " + itemStack.getItem().getName().getString());
			return 0;
		}
	}
	
	/**
	 * Removes amount experience from an ExperienceProviderItem stack.
	 * If there is not enough experience that could be removed do nothing and return false
	 *
	 * @param itemStack The item stack
	 * @param amount    The amount of experience to remove
	 * @return If there was enough experience that could be removed
	 */
	static boolean removeStoredExperience(ItemStack itemStack, int amount) {
		if (itemStack.getItem() instanceof ExperienceStorageItem) {
			NbtCompound nbtCompound = itemStack.getNbt();
			if (nbtCompound == null || !nbtCompound.contains("stored_experience", NbtElement.INT_TYPE)) {
				return false;
			} else {
				int existingStoredExperience = nbtCompound.getInt("stored_experience");
				if (existingStoredExperience < amount) {
					return false;
				} else {
					nbtCompound.putInt("stored_experience", existingStoredExperience - amount);
					itemStack.setNbt(nbtCompound);
					return true;
				}
			}
		} else {
			SpectrumCommon.logError("Tried to remove stored Experience from a non-ExperienceStorageItem: " + itemStack.getItem().getName().getString());
			return false;
		}
	}
	
	int getMaxStoredExperience(ItemStack itemStack);
	
}
