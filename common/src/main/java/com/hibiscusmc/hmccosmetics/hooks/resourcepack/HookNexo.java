package com.hibiscusmc.hmccosmetics.hooks.resourcepack;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.nexomc.nexo.api.events.resourcepack.NexoPrePackGenerateEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.lojosho.hibiscuscommons.hooks.Hook;
import net.kyori.adventure.key.Key;
import org.bukkit.event.EventHandler;
import team.unnamed.creative.base.Writable;
import team.unnamed.creative.item.Item;
import team.unnamed.creative.item.ItemModel;
import team.unnamed.creative.item.property.ItemBooleanProperty;
import team.unnamed.creative.item.tint.TintSource;
import team.unnamed.creative.model.Model;
import team.unnamed.creative.model.ModelTexture;
import team.unnamed.creative.model.ModelTextures;
import team.unnamed.creative.texture.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class HookNexo extends Hook {
    public HookNexo() {
        super("Nexo");
        setActive(true);
    }

    /*
    {
        "model": {
            "type": "composite",
            "models": [
                {
                    "type": "condition",
                    "property": "has_component",
                    "component": "dyed_color",
                    "ignore_default": "true",
                    "on_true": {
                        "type": "model",
                        "model": "hmccosmetics:dyeable",
                        "tints":[
                          {
                            "type":"dye","default":16777215
                          }
                        ]
                    },
                    "on_false": {
                        "type": "empty"
                    }
                },
                {
                  "type": "model",
                  "model": "X"
                }
            ]
        }
     */
    @EventHandler
    public void onPack(NexoPrePackGenerateEvent event) {
        Key dyedShading = Key.key("hmccosmetics", "dyed_shading");
        for (Cosmetic cosmetic : HMCCosmeticsAPI.getAllCosmetics()) {

            ItemModel conditionalModel = ItemModel.conditional(
                    ItemBooleanProperty.hasComponent("dyed_color", true),
                    ItemModel.composite(
                            ItemModel.reference(dyedShading, List.of(TintSource.dye(16777215))),
                            ItemModel.reference(cosmetic.getItem().getData(DataComponentTypes.ITEM_MODEL))
                    ),
                    ItemModel.reference(cosmetic.getItem().getData(DataComponentTypes.ITEM_MODEL))
            );

            Key itemKey = Key.key("hmccosmetics", cosmetic.getId() + "_shading");
            event.getResourcePack().item(Item.item(itemKey, conditionalModel));
        }

        ClassLoader clazzLoader = HMCCosmeticsPlugin.getInstance().getClass().getClassLoader();
        Texture shadeTexture = Texture.texture(dyedShading, Writable.resource(clazzLoader, "menus/shade.png"));
        event.getResourcePack().texture(shadeTexture);

        ModelTextures shadeTextures = ModelTextures.builder().addLayer(ModelTexture.ofKey(dyedShading)).build();
        Model shadeModel = Model.model().key(dyedShading).parent(Model.ITEM_GENERATED).textures(shadeTextures).build();
        event.getResourcePack().model(shadeModel);
    }
}
