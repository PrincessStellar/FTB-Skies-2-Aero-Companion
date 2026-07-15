package dev.ftb.mods.ftbskies2aerocompanion.compat.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import dev.ftb.mods.ftbskies2aerocompanion.item.ModItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AeroScoopScenes {
    private AeroScoopScenes() {}

    public static void aeroScoop(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("aeroscoop", "Scooping the skies with the AeroScoop");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.9f);
        scene.showBasePlate();
        scene.idle(10);

        // Positions match the schematic at src/main/resources/assets/ftbskies2aerocompanion/ponder/aeroscoop.nbt
        BlockPos aeroScoopPos = util.grid().at(1, 2, 2);
        BlockPos bearingPos = util.grid().at(2, 1, 2);
        BlockPos chassisPos = util.grid().at(2, 2, 2);
        BlockPos chestPos = util.grid().at(3, 2, 2);

        scene.world().showSection(util.select().position(aeroScoopPos), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(aeroScoopPos))
                .text("The AeroScoop pulls items out of the air as it moves.");
        scene.idle(80);

        ItemStack mesh = new ItemStack(ModItems.MESHES.get(MeshTier.CLOTH).get());
        scene.overlay().showControls(util.vector().topOf(aeroScoopPos), Pointing.DOWN, 60)
                .rightClick()
                .withItem(mesh);
        scene.idle(10);
        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().centerOf(aeroScoopPos))
                .text("Right-click to install a mesh filter — higher tiers scoop faster and last longer.");
        scene.idle(90);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().topOf(aeroScoopPos))
                .text("By itself, sitting still, no air moves past it...");
        scene.idle(90);

        scene.world().hideSection(util.select().position(aeroScoopPos), Direction.UP);
        scene.idle(10);

        Selection contraptionParts = util.select().fromTo(1, 2, 2, 3, 2, 2)
                .add(util.select().position(bearingPos));
        ElementLink<WorldSectionElement> contraption =
                scene.world().showIndependentSection(contraptionParts, Direction.DOWN);
        scene.world().configureCenterOfRotation(contraption, util.vector().centerOf(bearingPos));
        scene.idle(15);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(bearingPos))
                .text("Mount it on a moving contraption — a bearing, a windmill, or a Sable ship.");
        scene.idle(80);

        scene.world().rotateBearing(bearingPos, 360, 120);
        scene.world().rotateSection(contraption, 0, 360, 0, 120);
        scene.idle(20);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(0.5, 2.5, 2.5))
                .text("As the contraption moves, the AeroScoop fills its internal buffer with random drops.");

        ItemStack stoneEssence = new ItemStack(Items.COBBLESTONE);
        ItemStack dirtEssence = new ItemStack(Items.DIRT);
        ItemStack dust = new ItemStack(Items.GUNPOWDER);
        for (int i = 0; i < 3; i++) {
            scene.world().createItemEntity(
                    util.vector().centerOf(aeroScoopPos.above()),
                    util.vector().of(0, 0.15, 0),
                    i == 0 ? stoneEssence : (i == 1 ? dirtEssence : dust));
            scene.idle(15);
        }
        scene.idle(40);

        scene.overlay().showText(80)
                .attachKeyFrame()
                .placeNearTarget()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(chestPos))
                .text("Any inventory on the contraption — a chest, a vault, a barrel — receives the drops automatically.");

        scene.idle(60);

        scene.world().rotateBearing(bearingPos, 720, 200);
        scene.world().rotateSection(contraption, 0, 720, 0, 200);
        scene.idle(40);
        scene.markAsFinished();
    }
}
