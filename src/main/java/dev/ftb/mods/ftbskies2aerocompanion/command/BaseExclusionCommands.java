package dev.ftb.mods.ftbskies2aerocompanion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.BaseExclusionConfig;
import dev.ftb.mods.ftbskies2aerocompanion.basebuffer.TeamBaseGrid;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "ftbskies2aerocompanion")
public final class BaseExclusionCommands {
    private BaseExclusionCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("ftbskies2aerocompanion")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("predictbases")
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 10000))
                                .executes(ctx -> {
                                    int count = IntegerArgumentType.getInteger(ctx, "count");
                                    StringBuilder sb = new StringBuilder("First " + count + " predicted base regions (regionX, regionZ -> centerX, centerZ):\n");
                                    int size = BaseExclusionConfig.BASE_SIZE_REGIONS.get();
                                    int half = (size * TeamBaseGrid.REGION_BLOCKS) / 2;
                                    for (int i = 0; i < count; i++) {
                                        int[] r = TeamBaseGrid.nthBaseRegion(i);
                                        int cx = r[0] * TeamBaseGrid.REGION_BLOCKS + half;
                                        int cz = r[1] * TeamBaseGrid.REGION_BLOCKS + half;
                                        sb.append(i).append(": region(").append(r[0]).append(",").append(r[1])
                                                .append(") -> block(").append(cx).append(",").append(cz).append(")\n");
                                    }
                                    ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                                    return 1;
                                })))
                .then(Commands.literal("checkexclusion")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                            int z = IntegerArgumentType.getInteger(ctx, "z");
                                            int radius = BaseExclusionConfig.EXCLUSION_RADIUS.get();
                                            boolean excluded = TeamBaseGrid.isWithinBaseExclusion(x, z, radius);
                                            String msg = "(" + x + "," + z + ") radius=" + radius + " -> " + (excluded ? "EXCLUDED" : "allowed");
                                            ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                                            return 1;
                                        })))));
    }
}
