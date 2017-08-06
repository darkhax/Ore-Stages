package com.jarhax.oretiers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import com.jarhax.oretiers.api.OreTiersAPI;
import com.jarhax.oretiers.client.renderer.block.model.BakedModelTiered;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OreTiersEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void onModelBake (ModelBakeEvent event) {

        final Map<IBlockState, ModelResourceLocation> releventModels = new HashMap<>();

        for (final Map.Entry<IBlockState, ModelResourceLocation> entry : event.getModelManager().getBlockModelShapes().getBlockStateMapper().putAllStateModelLocations().entrySet()) {

            if (OreTiersAPI.getRelevantStates().contains(entry.getKey())) {

                releventModels.put(entry.getKey(), entry.getValue());
            }
        }
        
        for (final IBlockState state : OreTiersAPI.getStatesToReplace()) {
            final Tuple<String, IBlockState> stageInfo = OreTiersAPI.getStageInfo(state);
            final IBakedModel originalModel = event.getModelRegistry().getObject(releventModels.get(state));
            final IBakedModel replacementModel = event.getModelRegistry().getObject(releventModels.get(stageInfo.getSecond()));
            event.getModelRegistry().putObject(releventModels.get(state), new BakedModelTiered(stageInfo.getFirst(), originalModel, replacementModel));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBreak (BreakEvent event) {

        final Tuple<String, IBlockState> stageInfo = OreTiersAPI.getStageInfo(event.getState());

        if (stageInfo != null && (event.getPlayer() == null || !OreTiersAPI.hasStage(event.getPlayer(), stageInfo.getFirst()))) {

            event.setExpToDrop(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreakSpeed (BreakSpeed event) {

        final Tuple<String, IBlockState> stageInfo = OreTiersAPI.getStageInfo(event.getState());

        if (stageInfo != null && (event.getEntityPlayer() == null || !OreTiersAPI.hasStage(event.getEntityPlayer(), stageInfo.getFirst()))) {

            event.setNewSpeed(Utilities.getModifiedBreakSpeed(Utilities.getBlockStrengthSafely(event.getOriginalSpeed(), event.getState(), event.getEntityPlayer(), event.getEntityPlayer().world, event.getPos()), event.getState().getBlockHardness(event.getEntityPlayer().world, event.getPos()), Utilities.getCanHarvestSafely(stageInfo.getSecond(), event.getEntityPlayer())));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHarvestCheck (HarvestCheck event) {
        
        System.out.print("Event");
        final Tuple<String, IBlockState> stageInfo = OreTiersAPI.getStageInfo(event.getTargetBlock());

        if (stageInfo != null && (event.getEntityPlayer() == null || !OreTiersAPI.hasStage(event.getEntityPlayer(), stageInfo.getFirst()))) {

            event.setCanHarvest(Utilities.getCanHarvestSafely(stageInfo.getSecond(), event.getEntityPlayer()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockDrops (HarvestDropsEvent event) {

        final Tuple<String, IBlockState> stageInfo = OreTiersAPI.getStageInfo(event.getState());

        if (stageInfo != null && (event.getHarvester() == null || !OreTiersAPI.hasStage(event.getHarvester(), stageInfo.getFirst()))) {

            event.getDrops().clear();
            event.getDrops().addAll(stageInfo.getSecond().getBlock().getDrops(event.getWorld(), event.getPos(), stageInfo.getSecond(), event.getFortuneLevel()));
            event.setDropChance(ForgeEventFactory.fireBlockHarvesting(event.getDrops(), event.getWorld(), event.getPos(), stageInfo.getSecond(), event.getFortuneLevel(), event.getDropChance(), event.isSilkTouching(), event.getHarvester()));
        }
    }
    
    @SubscribeEvent
    public void onOverlayRendered (RenderGameOverlayEvent.Text event) {

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.gameSettings.showDebugInfo && event.getRight() != null) {
            
            for (ListIterator<String> iterator = event.getRight().listIterator(); iterator.hasNext();) { 
                
                String line = iterator.next();
                
                if (OreTiersAPI.REPLACEMENT_IDS.containsKey(line)) {
                    
                    iterator.set(OreTiersAPI.REPLACEMENT_IDS.get(line));
                }
            }
        }
    }
}
