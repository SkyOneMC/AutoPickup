package us.thezircon.play.autopickup.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import us.thezircon.play.autopickup.AutoPickup;

import java.util.*;

import static us.thezircon.play.autopickup.AutoPickup.smeltRecipeCache;

public class AutoSmeltUtils {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    public static final List<Material> IGNORE_MATERIALS = List.of(Material.COAL_ORE, Material.REDSTONE_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE, Material.NETHER_QUARTZ_ORE);

    public static void loadFurnaceRecipes(Map<Material, FurnaceRecipe> smeltRecipeCache) {
        smeltRecipeCache.clear();

        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                RecipeChoice inputChoice = furnaceRecipe.getInputChoice();

                if (inputChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
                    for (Material mat : materialChoice.getChoices()) {
                        smeltRecipeCache.putIfAbsent(mat, furnaceRecipe);
                    }
                }
            }
        }
    }

    public static ItemStack smelt(ItemStack itemStack, Player player) {
        if (IGNORE_MATERIALS.contains(itemStack.getType())
                || PLUGIN.getConfigManager().getAutoSmeltBlacklist().contains(itemStack.getType().toString())) {
            return itemStack.clone();
        }

        if (Tag.LOGS_THAT_BURN.isTagged(itemStack.getType())) {
            // No XP given for logs smelted into charcoal (can adjust if desired)
            return new ItemStack(Material.CHARCOAL, itemStack.getAmount());
        }

        FurnaceRecipe recipe = smeltRecipeCache.get(itemStack.getType());
        if (recipe != null) {
            ItemStack result = recipe.getResult().clone();
            result.setAmount(itemStack.getAmount());
            player.giveExp((int) (recipe.getExperience() * itemStack.getAmount()));
            return result;
        }

        return itemStack.clone();
    }

}
