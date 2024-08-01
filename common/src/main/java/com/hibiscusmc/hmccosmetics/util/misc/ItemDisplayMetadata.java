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

    public Vector3f translation;
    public Vector3f scale;
    public Quaternionf rotationLeft;
    public Quaternionf rotationRight;
    public Display.Billboard billboard;
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
        this.skyLight = 15;
        this.viewRange = 1.0f;
        this.width = 0.0f;
        this.height = 0.0f;
        this.displayTransform = ItemDisplay.ItemDisplayTransform.NONE;
        this.itemStack = new ItemStack(Material.AIR);
    }

    public ItemDisplayMetadata setFixed() {
        this.billboard = Display.Billboard.FIXED;
        return this;
    }

    public ItemDisplayMetadata setVertical() {
        this.billboard = Display.Billboard.VERTICAL;
        return this;
    }
}
