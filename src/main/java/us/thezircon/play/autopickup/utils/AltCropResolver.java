package us.thezircon.play.autopickup.utils;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;

import java.util.Map;

public class AltCropResolver {

    private static final Map<Material, Material> ALT_CROP_MAP = ImmutableMap.ofEntries(
            Map.entry(Material.KELP_PLANT, Material.KELP),
            Map.entry(Material.TWISTING_VINES_PLANT, Material.TWISTING_VINES),
            Map.entry(Material.WEEPING_VINES_PLANT, Material.WEEPING_VINES),
            Map.entry(Material.CAVE_VINES_PLANT, Material.GLOW_BERRIES),
            Map.entry(Material.BIG_DRIPLEAF_STEM, Material.BIG_DRIPLEAF),
            Map.entry(Material.PITCHER_CROP, Material.PITCHER_PLANT),
            Map.entry(Material.TORCHFLOWER_CROP, Material.TORCHFLOWER),
            Map.entry(Material.BEETROOTS, Material.BEETROOT)

    );

    public static Material resolve(Material type) {
        return ALT_CROP_MAP.getOrDefault(type, type);
    }
}