package de.dafuqs.pigment.REI;

import de.dafuqs.pigment.recipe.anvil_crushing.AnvilCrushingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.Collections;
import java.util.List;

public class AnvilCrushingRecipeDisplay<R extends AnvilCrushingRecipe> implements Display {

	private final List<EntryIngredient> inputs;
	private final EntryIngredient output;

	public final float experience;
	public final float crushedItemsPerPointOfDamage;

	public AnvilCrushingRecipeDisplay(AnvilCrushingRecipe recipe) {

		this.inputs = recipe.getIngredients().stream().map(EntryIngredients::ofIngredient).toList();
		this.output = EntryIngredients.of(recipe.getOutput());

		this.experience = recipe.getExperience();
		this.crushedItemsPerPointOfDamage = recipe.getCrushedItemsPerPointOfDamage();
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return inputs;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(output);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return AnvilCrushingCategory.ID;
	}

}