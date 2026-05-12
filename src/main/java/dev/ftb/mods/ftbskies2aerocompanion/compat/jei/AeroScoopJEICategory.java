package dev.ftb.mods.ftbskies2aerocompanion.compat.jei;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopRecipe;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopResult;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
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
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class AeroScoopJEICategory implements IRecipeCategory<AeroScoopRecipe> {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 78;
    private static final int MESH_X = 8;
    private static final int MESH_Y = 6;
    private static final int RESULT_START_X = 50;
    private static final int RESULT_START_Y = 6;
    private static final int RESULT_GAP = 18;
    private static final int RESULTS_PER_ROW = 6;

    private final IDrawable background;
    private final IDrawable icon;

    public AeroScoopJEICategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.AIR_FILTER_ITEM.get()));
    }

    @Override
    public RecipeType<AeroScoopRecipe> getRecipeType() {
        return JEIPlugin.AEROSCOOP_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.title");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
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
    public void setRecipe(IRecipeLayoutBuilder builder, AeroScoopRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, MESH_X, MESH_Y)
                .addIngredients(recipe.mesh());

        int idx = 0;
        for (AeroScoopResult result : recipe.results()) {
            int row = idx / RESULTS_PER_ROW;
            int col = idx % RESULTS_PER_ROW;
            int x = RESULT_START_X + col * RESULT_GAP;
            int y = RESULT_START_Y + row * RESULT_GAP;
            ItemStack baseStack = result.stack().copy();
            baseStack.setCount(Math.max(1, result.count()));
            String chanceLine = formatChance(result.chance());
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(baseStack)
                    .addRichTooltipCallback((view, tooltip) ->
                            tooltip.add(Component.translatable("jei." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.chance", chanceLine)
                                    .withStyle(ChatFormatting.GRAY)));
            idx++;
        }
    }

    @Override
    public void draw(AeroScoopRecipe recipe, IRecipeSlotsView view, GuiGraphics g, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        Component biomeLabel = Component.translatable(
                "jei." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.biome",
                recipe.biome().serialize()
        ).withStyle(ChatFormatting.DARK_GRAY);
        g.drawString(font, biomeLabel, MESH_X, MESH_Y + 22, 0x404040, false);
    }

    private static String formatChance(double chance) {
        if (chance >= 1.0) {
            int guaranteed = (int) Math.floor(chance);
            double bonus = chance - guaranteed;
            if (bonus < 1.0e-6) {
                return guaranteed + "x";
            }
            return guaranteed + "x +" + formatPercent(bonus) + " bonus";
        }
        return formatPercent(chance);
    }

    private static String formatPercent(double frac) {
        double pct = frac * 100.0;
        String s;
        if (pct >= 1.0) {
            s = String.format(Locale.ROOT, "%.2f", pct);
        } else if (pct >= 0.01) {
            s = String.format(Locale.ROOT, "%.4f", pct);
        } else {
            s = String.format(Locale.ROOT, "%.6f", pct);
        }
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            s = s.replaceAll("\\.$", "");
        }
        return s + "%";
    }
}
