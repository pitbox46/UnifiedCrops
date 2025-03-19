package github.pitbox46.unifiedcrops;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    public static ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static ModConfigSpec.ConfigValue<List<? extends String>> CROP_MODS = B.push("general")
            .comment("A list of mods to check for duplicates.\n" +
                    "The first mod will become the default item if possible")
            .defineListAllowEmpty(
                    "crop_mods",
                    List.of(
                            "pamhc2crops",
                            "farmersdelight",
                            "sushigocrafting",
                            "herbsandharvest"
                    ),
                    () -> "mymod",
                    o -> o instanceof String
            );
    public static ModConfigSpec SERVER = B.pop().build();
}
