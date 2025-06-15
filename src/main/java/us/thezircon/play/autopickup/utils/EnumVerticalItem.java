package us.thezircon.play.autopickup.utils;

import lombok.Getter;
import org.bukkit.Material;

public enum EnumVerticalItem {

    BAMBOO(1),
    KELP(1),
    KELP_PLANT(1),
    CACTUS(1),
    SUGAR_CANE(1),
    TWISTING_VINES(1),
    TWISTING_VINES_PLANT(1),
    BIG_DRIPLEAF_STEM(1),

    WEEPING_VINES(-1),
    WEEPING_VINES_PLANT(-1),
    CAVE_VINES(-1),
    CAVE_VINES_PLANT(-1),
    BIG_DRIPLEAF(-1);


    @Getter
    private final int growthDirection;

    EnumVerticalItem(int growthDirection) {
        this.growthDirection = growthDirection;
    }


    public static boolean isMultiBlock(Material material) {
        try {
            EnumVerticalItem.valueOf(material.name());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public static int getGrowthDirection(Material type) {
        EnumVerticalItem plantType = EnumVerticalItem.fromMaterial(type);
        return plantType != null ? plantType.getGrowthDirection() : 0;
    }

    public static EnumVerticalItem fromMaterial(Material material) {
        try {
            return EnumVerticalItem.valueOf(material.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean RequiresVerticalSupport(Material type)
    {
        return getGrowthDirection(type) != 0;
    }

}
