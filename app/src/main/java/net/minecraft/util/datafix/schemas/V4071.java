package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V4071 extends NamespacedSchema {
    public V4071(int p_380159_, Schema p_379344_) {
        super(p_380159_, p_379344_);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema p_379997_) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(p_379997_);
        p_379997_.register(map, "minecraft:creaking", () -> V100.equipment(p_379997_));
        p_379997_.register(map, "minecraft:creaking_transient", () -> V100.equipment(p_379997_));
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema p_379562_) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(p_379562_);
        p_379562_.register(map, "minecraft:creaking_heart", () -> DSL.optionalFields());
        return map;
    }
}
