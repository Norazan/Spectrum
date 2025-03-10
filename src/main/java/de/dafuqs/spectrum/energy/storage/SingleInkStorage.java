package de.dafuqs.spectrum.energy.storage;

import de.dafuqs.spectrum.energy.InkStorage;
import de.dafuqs.spectrum.energy.color.InkColor;
import de.dafuqs.spectrum.energy.color.InkColors;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.dafuqs.spectrum.helpers.Support.getShortenedNumberString;

public class SingleInkStorage implements InkStorage {
	
	protected final long maxEnergy;
	protected InkColor storedColor;
	protected long storedEnergy;
	
	/**
	 * Stores a single type of Pigment Energy
	 * Can only be filled with a new type if it is empty
	 **/
	public SingleInkStorage(long maxEnergy) {
		this.maxEnergy = maxEnergy;
		this.storedColor = InkColors.CYAN;
		this.storedEnergy = 0;
	}
	
	public SingleInkStorage(long maxEnergy, InkColor color, long amount) {
		this.maxEnergy = maxEnergy;
		this.storedColor = color;
		this.storedEnergy = amount;
	}
	
	public static @Nullable SingleInkStorage fromNbt(@NotNull NbtCompound compound) {
		if (compound.contains("MaxEnergyTotal", NbtElement.LONG_TYPE)) {
			long maxEnergyTotal = compound.getLong("MaxEnergyTotal");
			InkColor color = InkColor.of(compound.getString("Color"));
			long amount = compound.getLong("Amount");
			return new SingleInkStorage(maxEnergyTotal, color, amount);
		}
		return null;
	}
	
	public InkColor getStoredColor() {
		return storedColor;
	}
	
	@Override
	public boolean accepts(InkColor color) {
		return this.storedEnergy == 0 || this.storedColor == color;
	}
	
	@Override
	public long addEnergy(InkColor color, long amount) {
		if (color == storedColor) {
			long resultingAmount = this.storedEnergy + amount;
			this.storedEnergy = resultingAmount;
			if (resultingAmount > this.maxEnergy) {
				long overflow = this.storedEnergy - this.maxEnergy;
				this.storedEnergy = this.maxEnergy;
				return overflow;
			}
			return 0;
		} else if (this.storedEnergy == 0) {
			this.storedColor = color;
			this.storedEnergy = amount;
		}
		return amount;
	}
	
	@Override
	public boolean requestEnergy(InkColor color, long amount) {
		if (color == this.storedColor && amount >= this.storedEnergy) {
			this.storedEnergy -= amount;
			return true;
		} else {
			return false;
		}
	}
	
	public long drainEnergy(InkColor color, long amount) {
		if (color == this.storedColor) {
			long drainedAmount = Math.min(this.storedEnergy, amount);
			this.storedEnergy -= drainedAmount;
			return drainedAmount;
		} else {
			return 0;
		}
	}
	
	@Override
	public long getEnergy(InkColor color) {
		if (color == this.storedColor) {
			return this.storedEnergy;
		} else {
			return 0;
		}
	}
	
	@Override
	@Deprecated
	public Map<InkColor, Long> getEnergy() {
		return Map.of(this.storedColor, this.storedEnergy);
	}
	
	@Override
	@Deprecated
	public void setEnergy(Map<InkColor, Long> colors, long total) {
		for(Map.Entry<InkColor, Long> color : colors.entrySet()) {
			long value = color.getValue();
			if(value > 0) {
				this.storedColor = color.getKey();
				this.storedEnergy = color.getValue();
			}
		}
	}
	
	@Override
	public long getMaxTotal() {
		return this.maxEnergy;
	}
	
	@Override
	public long getMaxPerColor() {
		return this.maxEnergy;
	}
	
	@Override
	public long getCurrentTotal() {
		return this.storedEnergy;
	}
	
	@Override
	public boolean isEmpty() {
		return this.storedEnergy == 0;
	}
	
	@Override
	public boolean isFull() {
		return this.storedEnergy >= this.maxEnergy;
	}
	
	public NbtCompound toNbt() {
		NbtCompound compound = new NbtCompound();
		compound.putLong("MaxEnergyTotal", this.maxEnergy);
		compound.putString("Color", this.storedColor.toString());
		compound.putLong("Amount", this.storedEnergy);
		return compound;
	}
	
	public void addTooltip(List<Text> tooltip, boolean includeHeader) {
		if(includeHeader) {
			tooltip.add(Text.translatable("item.spectrum.ink_flask.tooltip", getShortenedNumberString(this.maxEnergy)));
		}
		if (this.storedEnergy > 0) {
			tooltip.add(Text.translatable("spectrum.tooltip.ink_powered.bullet." + this.storedColor.toString().toLowerCase(Locale.ROOT), getShortenedNumberString(this.storedEnergy)));
		}
	}
	
	@Override
	public long getRoom(InkColor color) {
		if (this.storedEnergy == 0 || this.storedColor == color) {
			return this.maxEnergy - this.storedEnergy;
		} else {
			return 0;
		}
	}
	
	@Override
	public void fillCompletely() {
		this.storedEnergy = this.maxEnergy;
	}
	
	@Override
	public void clear() {
		this.storedEnergy = 0;
	}
	
	public void convertColor(InkColor color) {
		this.storedColor = color;
	}
	
}