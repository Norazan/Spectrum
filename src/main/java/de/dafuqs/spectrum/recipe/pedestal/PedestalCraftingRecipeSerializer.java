package de.dafuqs.spectrum.recipe.pedestal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.dafuqs.spectrum.SpectrumCommon;
import de.dafuqs.spectrum.enums.GemstoneColor;
import de.dafuqs.spectrum.enums.PedestalRecipeTier;
import de.dafuqs.spectrum.mixin.AccessorShapedRecipe;
import de.dafuqs.spectrum.registries.SpectrumDefaultEnchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.Level;

import java.util.*;

public class PedestalCraftingRecipeSerializer<T extends PedestalCraftingRecipe> implements RecipeSerializer<T> {

    public final PedestalCraftingRecipeSerializer.RecipeFactory<T> recipeFactory;

    public PedestalCraftingRecipeSerializer(PedestalCraftingRecipeSerializer.RecipeFactory<T> recipeFactory) {
        this.recipeFactory = recipeFactory;
    }

    @Override
    public T read(Identifier identifier, JsonObject jsonObject) {
        String group = JsonHelper.getString(jsonObject, "group", "");
        Map<String, Ingredient> map = AccessorShapedRecipe.invokeReadSymbols(JsonHelper.getObject(jsonObject, "key"));
        String[] strings = AccessorShapedRecipe.invokeRemovePadding(AccessorShapedRecipe.invokeGetPattern(JsonHelper.getArray(jsonObject, "pattern")));
        int width = strings[0].length();
        int height = strings.length;
        DefaultedList<Ingredient> craftingInputs = AccessorShapedRecipe.invokeCreatePatternMatrix(strings, map, width, height);
        ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));

        if(SpectrumDefaultEnchantments.hasDefaultEnchants(output.getItem())) {
            SpectrumDefaultEnchantments.DefaultEnchantment enchantData = SpectrumDefaultEnchantments.getDefaultEnchantment(output.getItem());
            output.addEnchantment(enchantData.enchantment, enchantData.level);
        }

        PedestalRecipeTier tier = PedestalRecipeTier.valueOf(JsonHelper.getString(jsonObject, "tier", "basic").toUpperCase(Locale.ROOT));
        float experience = JsonHelper.getFloat(jsonObject, "experience", 0);
        int craftingTime = JsonHelper.getInt(jsonObject, "time", 200);

        HashMap<GemstoneColor, Integer> gemInputs = new HashMap<>();
        if(JsonHelper.hasPrimitive(jsonObject, "cyan")) {
            int amount = JsonHelper.getInt(jsonObject, "cyan", 0);
            gemInputs.put(GemstoneColor.CYAN, amount);
        }
        if(JsonHelper.hasPrimitive(jsonObject, "magenta")) {
            int amount = JsonHelper.getInt(jsonObject, "magenta", 0);
            gemInputs.put(GemstoneColor.MAGENTA, amount);
        }
        if(JsonHelper.hasPrimitive(jsonObject, "yellow")) {
            int amount = JsonHelper.getInt(jsonObject, "yellow", 0);
            gemInputs.put(GemstoneColor.YELLOW, amount);
        }
        if(JsonHelper.hasPrimitive(jsonObject, "white")) {
            int amount = JsonHelper.getInt(jsonObject, "white", 0);
            gemInputs.put(GemstoneColor.WHITE, amount);
        }
        if(JsonHelper.hasPrimitive(jsonObject, "black")) {
            int amount = JsonHelper.getInt(jsonObject, "black", 0);
            gemInputs.put(GemstoneColor.BLACK, amount);
        }

        boolean showToastOnUnlock = JsonHelper.getBoolean(jsonObject, "show_toast_on_unlock", true);

        List<Identifier> requiredAdvancementIdentifiers = new ArrayList<>();
        if(JsonHelper.hasArray(jsonObject, "required_advancements")) {
            JsonArray requiredAdvancementsArray = JsonHelper.getArray(jsonObject, "required_advancements");
            for(int i = 0; i < requiredAdvancementsArray.size(); i++) {
                Identifier requiredAdvancementIdentifier = Identifier.tryParse(requiredAdvancementsArray.get(i).getAsString());
                if(SpectrumCommon.minecraftServer != null && SpectrumCommon.minecraftServer.getAdvancementLoader().get(requiredAdvancementIdentifier) == null) {
                    SpectrumCommon.log(Level.ERROR, "Recipe " + identifier + " is set to require advancement " + requiredAdvancementIdentifier + ", but it does not exist!");
                } else {
                    requiredAdvancementIdentifiers.add(requiredAdvancementIdentifier);
                }
            }
        }


        return this.recipeFactory.create(identifier, group, tier, width, height, craftingInputs, gemInputs, output, experience, craftingTime, requiredAdvancementIdentifiers, showToastOnUnlock);
    }

    @Override
    public T read(Identifier identifier, PacketByteBuf packetByteBuf) {
        int width = packetByteBuf.readVarInt();
        int height = packetByteBuf.readVarInt();
        String group = packetByteBuf.readString(32767);
        DefaultedList<Ingredient> craftingInputs = DefaultedList.ofSize(width * height, Ingredient.EMPTY);

        for(int k = 0; k < craftingInputs.size(); ++k) {
            craftingInputs.set(k, Ingredient.fromPacket(packetByteBuf));
        }
        ItemStack output = packetByteBuf.readItemStack();

        PedestalRecipeTier tier = PedestalRecipeTier.values()[packetByteBuf.readVarInt()];

        int magenta = packetByteBuf.readVarInt();
        int cyan = packetByteBuf.readVarInt();
        int yellow = packetByteBuf.readVarInt();
        int black = packetByteBuf.readVarInt();
        int white = packetByteBuf.readVarInt();

        float experience = packetByteBuf.readFloat();
        int craftingTime = packetByteBuf.readVarInt();

        int requiredAdvancementAmount = packetByteBuf.readInt();
        List<Identifier> requiredAdvancementIdentifiers = new ArrayList<>();
        for(int i = 0; i < requiredAdvancementAmount; i++) {
            requiredAdvancementIdentifiers.add(packetByteBuf.readIdentifier());
        }
        boolean showToastOnUnlock = packetByteBuf.readBoolean();

        HashMap<GemstoneColor, Integer> gemInputs = new HashMap<>();
        if(magenta > 0) { gemInputs.put(GemstoneColor.MAGENTA, magenta); }
        if(cyan > 0   ) { gemInputs.put(GemstoneColor.CYAN, cyan); }
        if(yellow > 0 ) { gemInputs.put(GemstoneColor.YELLOW, yellow); }
        if(black > 0  ) { gemInputs.put(GemstoneColor.BLACK, black); }
        if(white > 0  ) { gemInputs.put(GemstoneColor.WHITE, white); }

        return this.recipeFactory.create(identifier, group, tier, width, height, craftingInputs, gemInputs, output, experience, craftingTime, requiredAdvancementIdentifiers, showToastOnUnlock);
    }

    @Override
    public void write(PacketByteBuf packetByteBuf, T pedestalRecipe) {
        packetByteBuf.writeVarInt(pedestalRecipe.width);
        packetByteBuf.writeVarInt(pedestalRecipe.height);
        packetByteBuf.writeString(pedestalRecipe.group);

        for (Ingredient ingredient : pedestalRecipe.craftingInputs) {
            ingredient.write(packetByteBuf);
        }

        packetByteBuf.writeItemStack(pedestalRecipe.output);

        packetByteBuf.writeInt(pedestalRecipe.tier.ordinal());
        packetByteBuf.writeInt(pedestalRecipe.getGemstonePowderAmount(GemstoneColor.MAGENTA));
        packetByteBuf.writeInt(pedestalRecipe.getGemstonePowderAmount(GemstoneColor.CYAN));
        packetByteBuf.writeInt(pedestalRecipe.getGemstonePowderAmount(GemstoneColor.YELLOW));
        packetByteBuf.writeInt(pedestalRecipe.getGemstonePowderAmount(GemstoneColor.BLACK));
        packetByteBuf.writeInt(pedestalRecipe.getGemstonePowderAmount(GemstoneColor.WHITE));

        packetByteBuf.writeFloat(pedestalRecipe.experience);
        packetByteBuf.writeInt(pedestalRecipe.craftingTime);

        packetByteBuf.writeInt(pedestalRecipe.requiredAdvancementIdentifiers.size());
        for(int i = 0; i < pedestalRecipe.requiredAdvancementIdentifiers.size(); i++) {
            packetByteBuf.writeIdentifier(pedestalRecipe.requiredAdvancementIdentifiers.get(i));
        }
        packetByteBuf.writeBoolean(pedestalRecipe.showToastOnUnlock);
    }

    public interface RecipeFactory<T extends PedestalCraftingRecipe> {
        T create(Identifier id, String group, PedestalRecipeTier tier, int width, int height, DefaultedList<Ingredient> craftingInputs, HashMap<GemstoneColor, Integer> gemInputs, ItemStack output, float experience, int craftingTime, List<Identifier> requiredAdvancementIdentifiers, boolean showToastOnUnlock);
    }

}