package dev.ftb.mods.ftbskies2aerocompanion.voidconversion;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.Optional;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class VoidConversionHandler {
    private static final int SPAWN_DEPTH_BELOW_FLOOR = 20;
    private static final float REBOUND_FALL_OFFSET = 43.0f;
    private static final float REBOUND_DIVISOR = 50.0f;

    private VoidConversionHandler() {}

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof ItemEntity itemEntity)) return;
        if (entity.getY() > level.getMinBuildHeight()) return;

        ItemStack input = itemEntity.getItem();
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<RecipeHolder<VoidConversionRecipe>> match = level.getRecipeManager()
                .getRecipeFor(ModRecipes.VOID_CONVERSION_TYPE.get(), recipeInput, level);
        if (match.isEmpty()) return;

        ItemStack output = match.get().value().assemble(recipeInput, level.registryAccess());
        double spawnY = level.getMinBuildHeight() - SPAWN_DEPTH_BELOW_FLOOR;
        ItemEntity result = new ItemEntity(level, entity.getX(), spawnY, entity.getZ(), output);
        result.setDeltaMovement(new Vec3(0.0, (entity.fallDistance - REBOUND_FALL_OFFSET) / REBOUND_DIVISOR, 0.0));
        result.setNoGravity(true);
        result.setGlowingTag(true);
        level.addFreshEntity(result);
    }
}
