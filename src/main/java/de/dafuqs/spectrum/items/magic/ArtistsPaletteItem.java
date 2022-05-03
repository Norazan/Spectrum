package de.dafuqs.spectrum.items.magic;

import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.energy.CappedElementalPigmentEnergyStorageItem;
import de.dafuqs.spectrum.energy.storage.ArtistsPaletteEnergyStorage;
import de.dafuqs.spectrum.energy.storage.IndividualAndTotalCappedElementalPigmentEnergyStorage;
import de.dafuqs.spectrum.energy.storage.PigmentEnergyStorage;
import de.dafuqs.spectrum.energy.storage.PigmentPaletteEnergyStorage;
import de.dafuqs.spectrum.items.trinkets.SpectrumTrinketItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArtistsPaletteItem extends SpectrumTrinketItem implements CappedElementalPigmentEnergyStorageItem {
	
	private final long maxEnergyTotal;
	private final long maxEnergyPerColor;
	
	public ArtistsPaletteItem(Settings settings, long maxEnergyTotal, long maxEnergyPerColor) {
		super(settings, new Identifier(SpectrumCommon.MOD_ID, "progression/unlock_artists_palette"));
		this.maxEnergyTotal = maxEnergyTotal;
		this.maxEnergyPerColor = maxEnergyPerColor;
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(new TranslatableText("item.spectrum.artists_palette.tooltip", this.maxEnergyPerColor));
		
		getEnergyStorage(stack).addTooltip(world, tooltip, context);
	}
	
	@Override
	public ArtistsPaletteEnergyStorage getEnergyStorage(ItemStack itemStack) {
		NbtCompound compound = itemStack.getNbt();
		if(compound != null && compound.contains("EnergyStore")) {
			return ArtistsPaletteEnergyStorage.fromNbt(compound.getCompound("EnergyStore"));
		}
		return new ArtistsPaletteEnergyStorage(this.maxEnergyTotal, this.maxEnergyPerColor);
	}
	
	@Override
	public void setEnergyStorage(ItemStack itemStack, IndividualAndTotalCappedElementalPigmentEnergyStorage storage) {
		NbtCompound compound = itemStack.getOrCreateNbt();
		compound.put("EnergyStore", storage.toNbt());
	}
	
}