package us.thezircon.play.autopickup.utils;

import lombok.Getter;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;

@Getter
public class TallCrops {

    private final HashSet<Material> verticalReq = new HashSet<>(List.of(Material.KELP, Material.KELP_PLANT,
            Material.BAMBOO, Material.BAMBOO_SAPLING)
    );

    private final HashSet<Material> verticalReqDown = new HashSet<>(List.of(
            Material.TWISTING_VINES, Material.TWISTING_VINES_PLANT, Material.WEEPING_VINES, Material.WEEPING_VINES_PLANT)
    );

    public static Material checkAltType(Material material) {
        if (material == Material.BAMBOO_SAPLING) {
            return Material.BAMBOO;
        }
        return material;
    }
}
