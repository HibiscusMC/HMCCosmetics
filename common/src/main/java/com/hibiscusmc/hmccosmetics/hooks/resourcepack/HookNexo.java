package com.hibiscusmc.hmccosmetics.hooks.resourcepack;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.gui.type.ShadingType;
import com.nexomc.nexo.api.events.resourcepack.NexoPostPackGenerateEvent;
import com.nexomc.nexo.api.events.resourcepack.NexoPrePackGenerateEvent;
import com.nexomc.nexo.utils.ResourcePackUtilsKt;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.lojosho.hibiscuscommons.hooks.Hook;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.atlas.Atlas;
import team.unnamed.creative.atlas.AtlasSource;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.item.Item;
import team.unnamed.creative.item.ItemModel;
import team.unnamed.creative.item.RangeDispatchItemModel;
import team.unnamed.creative.item.property.ItemBooleanProperty;
import team.unnamed.creative.item.tint.TintSource;
import team.unnamed.creative.model.Model;
import team.unnamed.creative.model.ModelTexture;
import team.unnamed.creative.model.ModelTextures;
import team.unnamed.creative.texture.Texture;

import java.util.List;
import java.util.Optional;

public class HookNexo extends Hook {
    public HookNexo() {
        super("Nexo");
        setActive(true);
    }

    private Key dyedShading = Key.key("hmccosmetics", "dyed_shading");

    /**
     * We add the Texture and Model for the background shading Model in PrePack so Nexo handles Atlases for us
     * Due to some change in 1.21.11 it is rather annoying to have to handle manually here
     * @param event
     */
    @EventHandler
    public void onPrePack(NexoPrePackGenerateEvent event) {
        if (Settings.getShadingType() != ShadingType.MODERN) return;
        ResourcePack resourcePack = event.getResourcePack();

        ClassLoader clazzLoader = HMCCosmeticsPlugin.getInstance().getClass().getClassLoader();
        Texture shadeTexture = Texture.texture(Key.key("hmccosmetics", "dyed_shading.png"), Writable.resource(clazzLoader, "menus/shade.png"));
        resourcePack.texture(shadeTexture);

        ModelTextures shadeTextures = ModelTextures.builder().addLayer(ModelTexture.ofKey(dyedShading)).build();
        Model shadeModel = Model.model().key(dyedShading).parent(Model.ITEM_GENERATED).textures(shadeTextures).build();
        resourcePack.model(shadeModel);
    }

    /**
     * Inject custom ItemModels used for Shading in menus here.
     * We use NexoPostPack and not PrePack so we ensure the full ItemModel is provided for us
     * @param event
     */
    @EventHandler
    public void onPack(NexoPostPackGenerateEvent event) {
        if (Settings.getShadingType() != ShadingType.MODERN) return;
        ResourcePack resourcePack = event.getResourcePack();

        for (Cosmetic cosmetic : HMCCosmeticsAPI.getAllCosmetics()) {
            ItemStack itemStack = cosmetic.getItem();
            Key itemModelKey = itemStack.getData(DataComponentTypes.ITEM_MODEL);
            if (itemModelKey == null) itemModelKey = itemStack.getType().key();
            ItemModel cosmeticItemModel = ResourcePackUtilsKt.itemOrVanilla(resourcePack, itemModelKey).model();

            // If the Item is using CustomModelData we try and fetch the ItemModel entry within
            // what is most likely a vanilla material Item linking to an ItemModel
            // Then we simply add that RangeDispatchEntry as our ItemModel for the CosmeticItem itself
            int customModelData = (itemStack.hasItemMeta() && itemStack.getItemMeta().hasCustomModelData()) ? itemStack.getItemMeta().getCustomModelData() : 0;
            if (cosmeticItemModel instanceof RangeDispatchItemModel rangeDispatch) {
                RangeDispatchItemModel.Entry entry = rangeDispatch.entries().stream().filter(e -> e.threshold() == customModelData).findFirst().orElse(null);
                if (entry != null) cosmeticItemModel = entry.model();
            }

            ItemModel conditionalModel = ItemModel.conditional(
                    ItemBooleanProperty.hasComponent("dyed_color", true),
                    ItemModel.composite(
                            ItemModel.reference(dyedShading, List.of(TintSource.dye(16777215))),
                            cosmeticItemModel
                    ),
                    cosmeticItemModel
            );

            Key itemKey = Key.key("hmccosmetics", cosmetic.getId() + "_shading");
            resourcePack.item(Item.item(itemKey, conditionalModel));
        }
    }
}
