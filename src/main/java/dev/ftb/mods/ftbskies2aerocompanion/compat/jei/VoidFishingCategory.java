package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class VoidFishingCategory implements IRecipeCategory<VoidFishingRecipe> {
    static final int COLUMNS = 8;
    static final int ROWS = 4;
    static final int SLOT_SIZE = 18;
    static final int GRID_X = 4;
    static final int GRID_Y = 26;

    private final IDrawable icon;

    public VoidFishingCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.VOID_FISHING_ROD.get()));
    }

    @Override
    public RecipeType<VoidFishingRecipe> getRecipeType() {
        return JEIPlugin.VOID_FISHING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ftbskies2aerocompanion.void_fishing.title");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 4 + COLUMNS * SLOT_SIZE + 4;
    }

    @Override
    public int getHeight() {
        return GRID_Y + ROWS * SLOT_SIZE + 4;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VoidFishingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 4)
                .addItemStack(new ItemStack(ModItems.VOID_FISHING_ROD.get()));

        int col = 0;
        int row = 0;
        for (VoidFishingDrop drop : recipe.drops()) {
            if (row >= ROWS) {
                break;
            }
            int x = GRID_X + col * SLOT_SIZE;
            int y = GRID_Y + row * SLOT_SIZE;
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(drop.stack())
                    .addRichTooltipCallback((slot, tooltip) -> appendTooltip(tooltip, drop));
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private static void appendTooltip(ITooltipBuilder tooltip, VoidFishingDrop drop) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("jei.ftbskies2aerocompanion.void_fishing.pool", drop.pool())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("jei.ftbskies2aerocompanion.void_fishing.weight", drop.weight())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jei.ftbskies2aerocompanion.void_fishing.chance",
                        String.format("%.2f", drop.chance() * 100.0))
                .withStyle(ChatFormatting.GRAY));
        if (!drop.biomes().isEmpty()) {
            tooltip.add(Component.translatable("jei.ftbskies2aerocompanion.void_fishing.biomes")
                    .withStyle(ChatFormatting.GOLD));
            for (ResourceLocation biome : drop.biomes()) {
                tooltip.add(Component.literal(" • ")
                        .append(Component.translatable(biome.toLanguageKey("biome")))
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }
}
