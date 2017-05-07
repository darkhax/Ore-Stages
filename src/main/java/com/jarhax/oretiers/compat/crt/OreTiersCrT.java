package com.jarhax.oretiers.compat.crt;

import com.jarhax.oretiers.compat.crt.actions.ActionAddBlacklist;
import com.jarhax.oretiers.compat.crt.actions.ActionAddReplacement;
import com.jarhax.oretiers.compat.crt.actions.ActionAddStage;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.OreTiers.Tiers")
public class OreTiersCrT {

    public static void init () {

        MineTweakerAPI.registerClass(OreTiersCrT.class);
    }

    @ZenMethod
    public static void addStage (String name) {

        MineTweakerAPI.apply(new ActionAddStage(name));
    }

    @ZenMethod
    public static void addReplacement (String name, IIngredient original, IItemStack replacement) {

        final Object internal = original.getInternal();
        final IBlockState replacementState = getStateFromStack((ItemStack) replacement.getInternal());

        if (internal instanceof Block) {
            MineTweakerAPI.apply(new ActionAddReplacement(name, ((Block) internal).getDefaultState(), replacementState));
        }
        else if (internal instanceof ItemStack) {
            MineTweakerAPI.apply(new ActionAddReplacement(name, getStateFromStack((ItemStack) internal), replacementState));
        }
        else if (internal instanceof String) {
            for (final ItemStack stack : OreDictionary.getOres((String) internal)) {
                MineTweakerAPI.apply(new ActionAddReplacement(name, getStateFromStack(stack), replacementState));
            }
        }
    }

    @ZenMethod
    public static void blacklist (IIngredient state) {

        final Object internal = state.getInternal();

        if (internal instanceof Block) {
            MineTweakerAPI.apply(new ActionAddBlacklist(((Block) internal).getDefaultState()));
        }
        else if (internal instanceof ItemStack) {
            MineTweakerAPI.apply(new ActionAddBlacklist(getStateFromStack((ItemStack) internal)));
        }
        else if (internal instanceof String) {
            for (final ItemStack stack : OreDictionary.getOres((String) internal)) {
                MineTweakerAPI.apply(new ActionAddBlacklist(getStateFromStack(stack)));
            }
        }
    }

    private static IBlockState getStateFromStack (ItemStack stack) {

        final Block block = Block.getBlockFromItem(stack.getItem());
        return block != null ? block.getStateFromMeta(stack.getMetadata()) : Blocks.STONE.getDefaultState();
    }
}