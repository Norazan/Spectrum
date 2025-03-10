package de.dafuqs.spectrum.progression.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import de.dafuqs.spectrum.SpectrumCommon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RevelationToast implements Toast {
	
	private final Identifier TEXTURE = new Identifier(SpectrumCommon.MOD_ID, "textures/gui/toasts.png");
	private final ItemStack itemStack;
	private final SoundEvent soundEvent;
	private boolean soundPlayed;
	
	public RevelationToast(ItemStack itemStack, SoundEvent soundEvent) {
		this.itemStack = itemStack;
		this.soundEvent = soundEvent;
		this.soundPlayed = false;
	}
	
	public static void showRevelationToast(MinecraftClient client, ItemStack itemStack, SoundEvent soundEvent) {
		client.getToastManager().add(new RevelationToast(itemStack, soundEvent));
	}
	
	@Override
	public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
		Text title = Text.translatable("spectrum.toast.revelation.title");
		Text text = Text.translatable("spectrum.toast.revelation.text");
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());
		
		List<OrderedText> wrappedText = manager.getClient().textRenderer.wrapLines(text, 125);
		List<OrderedText> wrappedTitle = manager.getClient().textRenderer.wrapLines(title, 125);
		int l;
		if (startTime < 2500L) {
			l = MathHelper.floor(MathHelper.clamp((float) (2500L - startTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
			int halfHeight = this.getHeight() / 2;
			int titleSize = wrappedTitle.size();
			int m = halfHeight - titleSize * 9 / 2;
			
			for (Iterator<OrderedText> var12 = wrappedTitle.iterator(); var12.hasNext(); m += 9) {
				OrderedText orderedText = var12.next();
				manager.getClient().textRenderer.draw(matrices, orderedText, 30.0F, (float) m, 3289650 | l);
			}
		} else {
			l = MathHelper.floor(MathHelper.clamp((float) (startTime - 2500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
			int halfHeight = this.getHeight() / 2;
			int textSize = wrappedText.size();
			int m = halfHeight - textSize * 9 / 2;
			
			for (Iterator<OrderedText> var12 = wrappedText.iterator(); var12.hasNext(); m += 9) {
				OrderedText orderedText = var12.next();
				manager.getClient().textRenderer.draw(matrices, orderedText, 30.0F, (float) m, l);
			}
		}
		
		if (!this.soundPlayed && startTime > 0L) {
			this.soundPlayed = true;
			if (this.soundEvent != null) {
				manager.getClient().getSoundManager().play(PositionedSoundInstance.master(this.soundEvent, 1.0F, 0.6F));
			}
		}
		
		manager.getClient().getItemRenderer().renderInGui(itemStack, 8, 8);
		return startTime >= 5000L ? Visibility.HIDE : Visibility.SHOW;
	}
	
}
