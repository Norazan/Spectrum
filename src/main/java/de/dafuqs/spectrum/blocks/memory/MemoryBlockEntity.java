package de.dafuqs.spectrum.blocks.memory;

import de.dafuqs.spectrum.interfaces.PlayerOwned;
import de.dafuqs.spectrum.networking.SpectrumS2CPacketSender;
import de.dafuqs.spectrum.progression.SpectrumAdvancementCriteria;
import de.dafuqs.spectrum.registries.SpectrumBlockEntityRegistry;
import de.dafuqs.spectrum.registries.SpectrumBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class MemoryBlockEntity extends BlockEntity implements PlayerOwned {
	
	protected ItemStack memoryItemStack = ItemStack.EMPTY; // zero or negative values: never hatch
	
	protected UUID ownerUUID;
	
	public MemoryBlockEntity(BlockPos pos, BlockState state) {
		super(SpectrumBlockEntityRegistry.MEMORY, pos, state);
	}
	
	public void setData(LivingEntity livingEntity, @NotNull ItemStack creatureSpawnItemStack) {
		if(livingEntity instanceof PlayerEntity playerEntity) {
			setOwner(playerEntity);
		}
		if(creatureSpawnItemStack.getItem() instanceof MemoryItem) {
			this.memoryItemStack = creatureSpawnItemStack;
		}
	}
	
	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		
		if(nbt.contains("MemoryItem", NbtElement.COMPOUND_TYPE)) {
			NbtCompound creatureSpawnCompound = nbt.getCompound("MemoryItem");
			this.memoryItemStack = ItemStack.fromNbt(creatureSpawnCompound);
		}
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if(this.memoryItemStack != null) {
			NbtCompound creatureSpawnCompound = new NbtCompound();
			memoryItemStack.writeNbt(creatureSpawnCompound);
			nbt.put("MemoryItem", creatureSpawnCompound);
		}
	}
	
	public void advanceManifesting(ServerWorld world, BlockPos blockPos) {
		int ticksToManifest = MemoryItem.getTicksToManifest(this.memoryItemStack.getNbt());
		if(ticksToManifest > 0) {
			int additionalManifestAdvanceSteps = getManifestAdvanceSteps(world, blockPos);
			if(additionalManifestAdvanceSteps > 0) {
				int newTicksToManifest = ticksToManifest - additionalManifestAdvanceSteps;
				if (newTicksToManifest <= 0) {
					this.manifest(world, blockPos);
				} else {
					Optional<EntityType<?>> entityTypeOptional = MemoryItem.getEntityType(this.memoryItemStack.getNbt());
					if (entityTypeOptional.isPresent()) {
						MemoryItem.setTicksToManifest(this.memoryItemStack, newTicksToManifest);
						SpectrumS2CPacketSender.playMemoryManifestingParticles(world, blockPos, entityTypeOptional.get(), 3);
						world.playSound(null, this.pos, SoundEvents.ENTITY_TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F); // TODO: fitting sound
					}
				}
			}
		}
	}
	
	public void manifest(@NotNull ServerWorld world, BlockPos blockPos) {
		world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
		Optional<Entity> hatchedEntity = hatchEntity(world, blockPos, this.memoryItemStack);
		
		if(hatchedEntity.isPresent()) {
			SpectrumS2CPacketSender.playMemoryManifestingParticles(world, blockPos, hatchedEntity.get().getType(), 10);
			
			if(hatchedEntity.get() instanceof MobEntity hatchedMobEntity) {
				hatchedMobEntity.playAmbientSound();
				hatchedMobEntity.playSpawnEffects();
			}
			
			triggerManifestingAdvancementCriterion(hatchedEntity.get());
		}
	}
	
	protected void triggerManifestingAdvancementCriterion(Entity hatchedEntity) {
		PlayerEntity owner = PlayerOwned.getPlayerEntityIfOnline(world, this.ownerUUID);
		if(owner instanceof ServerPlayerEntity serverPlayerEntity) {
			SpectrumAdvancementCriteria.MEMORY_MANIFESTING.trigger(serverPlayerEntity, hatchedEntity);
		}
	}
	
	@Contract("_ -> new")
	public static @NotNull Pair<Integer, Integer> getEggColorsForEntity(EntityType entityType) {
		SpawnEggItem spawnEggItem = SpawnEggItem.forEntity(entityType);
		if(spawnEggItem != null) {
			return new Pair<>(spawnEggItem.getColor(0), spawnEggItem.getColor(1));
		}
		return new Pair<>(0x222222, 0xDDDDDD);
	}
	
	protected Optional<Entity> hatchEntity(ServerWorld world, BlockPos blockPos, ItemStack itemStack) {
		if(this.memoryItemStack.getItem() instanceof MemoryItem) {
			Optional<EntityType<?>> entityType = MemoryItem.getEntityType(memoryItemStack.getNbt());
			if(entityType.isPresent()) {
				// alignPosition: center the mob in the center of the blockPos
				Entity entity = entityType.get().spawnFromItemStack(world, memoryItemStack, null, blockPos, SpawnReason.SPAWN_EGG, true, false);
				if(entity != null) {
					if (entity instanceof MobEntity mobEntity) {
						mobEntity.setBaby(true);
						if (itemStack.hasCustomName()) {
							mobEntity.setCustomName(itemStack.getName());
						}
					}
					
					world.spawnEntityAndPassengers(entity);
					return Optional.of(entity);
				}
			}
		}
		return Optional.empty();
	}
	
	public static int getManifestAdvanceSteps(@NotNull World world, @NotNull BlockPos blockPos) {
		BlockState belowBlockState = world.getBlockState(blockPos.down());
		if(belowBlockState.isIn(SpectrumBlockTags.MEMORY_NEVER_MANIFESTERS)) {
			return 0;
		} else if(belowBlockState.isIn(SpectrumBlockTags.MEMORY_VERY_FAST_MANIFESTERS)) {
			return 8;
		} else if(belowBlockState.isIn(SpectrumBlockTags.MEMORY_FAST_MANIFESTERS)) {
			return 3;
		} else {
			return 1;
		}
	}
	
	@Override
	public UUID getOwnerUUID() {
		return this.ownerUUID;
	}
	
	@Override
	public void setOwner(@NotNull PlayerEntity playerEntity) {
		this.ownerUUID = playerEntity.getUuid();
	}
	
}
