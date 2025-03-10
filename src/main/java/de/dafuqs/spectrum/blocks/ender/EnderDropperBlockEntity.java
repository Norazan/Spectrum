package de.dafuqs.spectrum.blocks.ender;

import de.dafuqs.spectrum.interfaces.PlayerOwnedWithName;
import de.dafuqs.spectrum.registries.SpectrumBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class EnderDropperBlockEntity extends BlockEntity implements PlayerOwnedWithName {
	
	private UUID ownerUUID;
	private String ownerName;
	
	public EnderDropperBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(SpectrumBlockEntities.ENDER_DROPPER, blockPos, blockState);
	}
	
	protected Text getContainerName() {
		if (hasOwner()) {
			return Text.translatable("block.spectrum.ender_dropper").append(Text.translatable("container.spectrum.owned_by_player", this.ownerName));
		} else {
			return Text.translatable("block.spectrum.ender_dropper");
		}
	}
	
	public int chooseNonEmptySlot() {
		if (this.hasOwner()) {
			PlayerEntity playerEntity = getOwnerIfOnline();
			if (playerEntity == null) {
				return -1; // player not online => no drop
			} else {
				int i = -1;
				int j = 1;
				
				EnderChestInventory enderInventory = playerEntity.getEnderChestInventory();
				for (int k = 0; k < enderInventory.size(); ++k) {
					if (!(enderInventory.getStack(k)).isEmpty() && world.random.nextInt(j++) == 0) {
						i = k;
					}
				}
				
				return i;
			}
		} else {
			return -1; // no owner
		}
	}
	
	public ItemStack getStack(int slot) {
		PlayerEntity playerEntity = getOwnerIfOnline();
		if(playerEntity != null) {
			EnderChestInventory enderInventory = playerEntity.getEnderChestInventory();
			return enderInventory.getStack(slot);
		}
		return ItemStack.EMPTY;
	}
	
	public void setStack(int slot, ItemStack itemStack) {
		PlayerEntity playerEntity = getOwnerIfOnline();
		if(playerEntity != null) {
			EnderChestInventory enderInventory = playerEntity.getEnderChestInventory();
			enderInventory.setStack(slot, itemStack);
		}
	}
	
	@Override
	public UUID getOwnerUUID() {
		return this.ownerUUID;
	}
	
	@Override
	public String getOwnerName() {
		return this.ownerName;
	}
	
	@Override
	public void setOwner(PlayerEntity playerEntity) {
		this.ownerUUID = playerEntity.getUuid();
		this.ownerName = playerEntity.getName().getString();
	}
	
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		
		if (nbt.contains("OwnerUUID")) {
			this.ownerUUID = nbt.getUuid("OwnerUUID");
		} else {
			this.ownerUUID = null;
		}
		if (nbt.contains("OwnerName")) {
			this.ownerName = nbt.getString("OwnerName");
		} else {
			this.ownerName = null;
		}
	}
	
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		
		if (this.ownerUUID != null) {
			nbt.putUuid("OwnerUUID", this.ownerUUID);
		}
		if (this.ownerName != null) {
			nbt.putString("OwnerName", this.ownerName);
		}
	}
	
}
