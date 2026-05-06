package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.voidconversion.VoidConversionRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class VoidConversionCategory implements IRecipeCategory<VoidConversionRecipe> {
    private static final ResourceLocation ARROWS = FTBSkies2AeroCompanion.id("textures/gui/jei_void_conversion.png");

    private static final int WIDTH = 162;
    private static final int HEIGHT = 56;
    private static final int INPUT_X = 8;
    private static final int OUTPUT_X = 136;
    private static final int SLOT_Y = 6;
    private static final int ARROWS_X = 65;
    private static final int ARROWS_Y = 7;

    private final IDrawable arrows;
    private final IDrawable icon;

    public VoidConversionCategory(IGuiHelper helper) {
        this.arrows = helper.drawableBuilder(ARROWS, 0, 0, 32, 18).setTextureSize(32, 18).build();
        this.icon = helper.createDrawableItemStack(new ItemStack(Items.COARSE_DIRT));
    }

    @Override
    public RecipeType<VoidConversionRecipe> getRecipeType() {
        return JEIPlugin.VOID_CONVERSION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ftbskies2aerocompanion.void_conversion.title");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VoidConversionRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
                .addItemStack(new ItemStack(recipe.input()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
                .addItemStack(new ItemStack(recipe.output()));
    }

    @Override
    public void draw(VoidConversionRecipe recipe, IRecipeSlotsView view, GuiGraphics g, double mouseX, double mouseY) {
        arrows.draw(g, ARROWS_X, ARROWS_Y);

        Font font = Minecraft.getInstance().font;
        Level level = Minecraft.getInstance().level;
        int y = level != null ? level.getMinBuildHeight() : -64;
        Component yLabel = Component.translatable("jei.ftbskies2aerocompanion.void_conversion.y_level", y)
                .withStyle(ChatFormatting.DARK_GRAY);
        int textWidth = font.width(yLabel);
        g.drawString(font, yLabel, (WIDTH - textWidth) / 2, 38, 0x404040, false);
    }
}
