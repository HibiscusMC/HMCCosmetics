package com.hibiscusmc.hmccosmetics.hooks.resourcepack;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.nexomc.nexo.api.events.resourcepack.NexoPostPackGenerateEvent;
import com.nexomc.nexo.utils.ResourcePackUtilsKt;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.lojosho.hibiscuscommons.hooks.Hook;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import team.unnamed.creative.atlas.Atlas;
import team.unnamed.creative.atlas.AtlasSource;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.item.Item;
import team.unnamed.creative.item.ItemModel;
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

    @EventHandler
    public void onPack(NexoPostPackGenerateEvent event) {
        Key dyedShading = Key.key("hmccosmetics", "dyed_shading");
        for (Cosmetic cosmetic : HMCCosmeticsAPI.getAllCosmetics()) {
            ItemModel cosmeticItemModel = ResourcePackUtilsKt.itemOrVanilla(event.getResourcePack(), cosmetic.getItem().getData(DataComponentTypes.ITEM_MODEL)).model();
            ItemModel conditionalModel = ItemModel.conditional(
                    ItemBooleanProperty.hasComponent("dyed_color", true),
                    ItemModel.composite(
                            ItemModel.reference(dyedShading, List.of(TintSource.dye(16777215))),
                            ItemModel.reference(cosmetic.getItem().getData(DataComponentTypes.ITEM_MODEL))
                    ),
                    cosmeticItemModel
            );

            Key itemKey = Key.key("hmccosmetics", cosmetic.getId() + "_shading");
            event.getResourcePack().item(Item.item(itemKey, conditionalModel));
        }

        ClassLoader clazzLoader = HMCCosmeticsPlugin.getInstance().getClass().getClassLoader();
        Texture shadeTexture = Texture.texture(Key.key("hmccosmetics", "dyed_shading.png"), Writable.resource(clazzLoader, "menus/shade.png"));
        event.getResourcePack().texture(shadeTexture);

        Optional.ofNullable(event.getResourcePack().atlas(Atlas.BLOCKS)).ifPresentOrElse(
            atlas -> event.getResourcePack().atlas(atlas.toBuilder().addSource(AtlasSource.single(dyedShading)).build()),
            () -> event.getResourcePack().atlas(Atlas.atlas(Atlas.BLOCKS, AtlasSource.single(dyedShading)))
        );

        ModelTextures shadeTextures = ModelTextures.builder().addLayer(ModelTexture.ofKey(dyedShading)).build();
        Model shadeModel = Model.model().key(dyedShading).parent(Model.ITEM_GENERATED).textures(shadeTextures).build();
        event.getResourcePack().model(shadeModel);
    }
}
