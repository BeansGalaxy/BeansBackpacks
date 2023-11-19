package com.beansgalaxy.galaxybackpacks.client;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TrimHelper extends ArmorTrim {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<TrimHelper> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group((ArmorTrimMaterial.ENTRY_CODEC.fieldOf("material")).forGetter(TrimHelper::getMaterial),
    (ArmorTrimPattern.ENTRY_CODEC.fieldOf("pattern")).forGetter(TrimHelper::getPattern)).apply(instance, TrimHelper::new));

    private final RegistryEntry<ArmorTrimMaterial> material;
    private final Function<ArmorMaterial, Identifier> backpackTexture;

    public TrimHelper(RegistryEntry<ArmorTrimMaterial> material, RegistryEntry<ArmorTrimPattern> pattern) {
        super(material, pattern);
        this.material = material;
        this.backpackTexture = Util.memoize(armorMaterial -> {
            Identifier identifier = pattern.value().assetId();
            String string = this.getMaterialAssetNameFor(armorMaterial);
            return identifier.withPath(path -> "galaxybackpacks/trims/" + path + "_" + string);
        });
    }

    private String getMaterialAssetNameFor(ArmorMaterial armorMaterial) {
        Map<ArmorMaterials, String> map = this.material.value().overrideArmorMaterials();
        return armorMaterial instanceof ArmorMaterials && map.containsKey(armorMaterial) ? map.get(armorMaterial) : this.material.value().assetName();
    }

    public static Optional<TrimHelper> getBackpackTrim(DynamicRegistryManager registryManager, NbtCompound tag) {
        if (!tag.isEmpty()) {
            TrimHelper b$trim = CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, registryManager), tag).resultOrPartial(LOGGER::error).orElse(null);
            return Optional.ofNullable(b$trim);
        } else return Optional.empty();
    }

    public Identifier backpackTexture(ArmorMaterial p_268143_) {
        return this.backpackTexture.apply(p_268143_);
    }
}
