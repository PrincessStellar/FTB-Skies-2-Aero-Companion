package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

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
import net.minecraft.world.item.ItemStack;

public class OreGeneratorCategory implements IRecipeCategory<OreGeneratorRecipe> {
    private static final int SLOT_SIZE = 18;
    private static final int GRID_X = 4;
    private static final int GRID_Y = 26;

    private final IDrawable icon;
    private final ItemStack catalyst;

    public OreGeneratorCategory(IGuiHelper helper) {
        this.catalyst = MekanismLasersOreGen.oreGeneratorStack();
        this.icon = helper.createDrawableItemStack(this.catalyst);
    }

    @Override
    public RecipeType<OreGeneratorRecipe> getRecipeType() {
        return JEIPlugin.ORE_GENERATOR_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ftbskies2aerocompanion.ore_generator.title");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return GRID_X + MekanismLasersOreGen.COLUMNS * SLOT_SIZE + 4;
    }

    @Override
    public int getHeight() {
        return GRID_Y + MekanismLasersOreGen.ROWS * SLOT_SIZE + 4;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, OreGeneratorRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 4, 4)
                .addItemStack(catalyst);

        int col = 0;
        int row = 0;
        for (OreGeneratorEntry entry : recipe.ores()) {
            if (row >= MekanismLasersOreGen.ROWS) {
                break;
            }
            int x = GRID_X + col * SLOT_SIZE;
            int y = GRID_Y + row * SLOT_SIZE;
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(entry.stack())
                    .addRichTooltipCallback((slot, tooltip) -> appendTooltip(tooltip, entry));
            col++;
            if (col >= MekanismLasersOreGen.COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private static void appendTooltip(ITooltipBuilder tooltip, OreGeneratorEntry entry) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("jei.ftbskies2aerocompanion.ore_generator.chance",
                        String.format("%.2f", entry.chance() * 100.0))
                .withStyle(ChatFormatting.GRAY));
    }
}
