package us.thezircon.play.autopickup.utils;

import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

public enum EnumVerticalItem {

    BAMBOO(Material.BAMBOO, 1),
    KELP(Material.KELP, 1),
    KELP_PLANT(Material.KELP_PLANT, 1),
    CACTUS(Material.CACTUS, 1),
    SUGAR_CANE(Material.SUGAR_CANE, 1),
    TWISTING_VINES(Material.TWISTING_VINES, 1),
    TWISTING_VINES_PLANT(Material.TWISTING_VINES_PLANT, 1),
    BIG_DRIPLEAF_STEM(Material.BIG_DRIPLEAF_STEM, 1),

    BIG_DRIPLEAF(Material.BIG_DRIPLEAF, -1),
    WEEPING_VINES(Material.WEEPING_VINES, -1),
    WEEPING_VINES_PLANT(Material.WEEPING_VINES_PLANT, -1),
    CAVE_VINES(Material.CAVE_VINES, -1),
    CAVE_VINES_PLANT(Material.CAVE_VINES_PLANT, -1);

    private final Material material;
    private final int growthDirection;

    private static final Map<Material, Integer> MATERIAL_TO_ENUM = new EnumMap<>(Material.class);

    static {
        for (EnumVerticalItem item : values()) {
            MATERIAL_TO_ENUM.put(item.material, item.growthDirection);
        }
    }

    EnumVerticalItem(Material material, int growthDirection) {
        this.material = material;
        this.growthDirection = growthDirection;
    }

    public static int getGrowthDirection(Material material) {
        return MATERIAL_TO_ENUM.getOrDefault(material, 0);
    }
}
