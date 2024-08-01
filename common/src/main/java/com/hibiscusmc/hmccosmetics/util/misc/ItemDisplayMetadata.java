package com.hibiscusmc.hmccosmetics.util.misc;

import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ItemDisplayMetadata {

    public static final Map<Integer, ItemDisplayMetadata> metadataCache = new HashMap<>();

    public Vector3f translation;
    public Vector3f scale;
    public Quaternionf rotationLeft;
    public Quaternionf rotationRight;
    public Display.Billboard billboard;
    public int blockLight;
    public int skyLight;
    public float viewRange;
    public float width;
    public float height;
    public ItemDisplay.ItemDisplayTransform displayTransform;
    public ItemStack itemStack;

    public ItemDisplayMetadata() {
        this.translation = new Vector3f();
        this.scale = new Vector3f(1.0f, 1.0f, 1.0f);
        this.rotationLeft = new Quaternionf();
        this.rotationRight = new Quaternionf();
        this.billboard = Display.Billboard.FIXED;
        this.blockLight = 0;
        this.skyLight = 0;
        this.viewRange = 1.0f;
        this.width = 0.0f;
        this.height = 0.0f;
        this.displayTransform = ItemDisplay.ItemDisplayTransform.NONE;
        this.itemStack = new ItemStack(Material.AIR);
    }

    public ItemDisplayMetadata(ItemDisplayMetadata metadata) {
        this.translation = metadata.translation;
        this.scale = metadata.scale;
        this.rotationLeft = metadata.rotationLeft;
        this.rotationRight = metadata.rotationRight;
        this.billboard = metadata.billboard;
        this.blockLight = metadata.blockLight;
        this.skyLight = metadata.skyLight;
        this.viewRange = metadata.viewRange;
        this.width = metadata.width;
        this.height = metadata.height;
        this.displayTransform = metadata.displayTransform;
        this.itemStack = metadata.itemStack;
    }

    public ItemDisplayMetadata(
            Vector3f translation,
            Vector3f scale,
            Quaternionf rotationLeft,
            Quaternionf rotationRight,
            Display.Billboard billboard,
            int blockLight,
            int skyLight,
            float viewRange,
            float width,
            float height,
            ItemDisplay.ItemDisplayTransform displayTransform,
            ItemStack itemStack
    ) {
        this.translation = translation;
        this.scale = scale;
        this.rotationLeft = rotationLeft;
        this.rotationRight = rotationRight;
        this.billboard = billboard;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.viewRange = viewRange;
        this.width = width;
        this.height = height;
        this.displayTransform = displayTransform;
        this.itemStack = itemStack;
    }
}
