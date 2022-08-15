package io.github.fisher2911.hmccosmetics.packet.wrappers;

import io.github.fisher2911.hmccosmetics.user.Equipment;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;

import java.util.List;

public class WrapperPlayServerEntityEquipment extends AbstractPacket {
    public static final PacketType TYPE =
            PacketType.Play.Server.ENTITY_EQUIPMENT;
    private List<Equipment> equipment;

    public WrapperPlayServerEntityEquipment() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerEntityEquipment(PacketContainer packet) {
        super(packet, TYPE);
    }

    public void read() {

    }

    public WrapperPlayServerEntityEquipment(PacketContainer packet, int entityId, List<Equipment> equipment) {
        super(packet, TYPE);
        setEntityID(entityId);
        this.equipment = equipment;
    }

    /**
     * Retrieve Entity ID.
     * <p>
     * Notes: entity's ID
     *
     * @return The current Entity ID
     */
    public int getEntityID() {
        return handle.getIntegers().read(0);
    }

    /**
     * Set Entity ID.
     *
     * @param value - new value.
     */
    public void setEntityID(int value) {
        handle.getIntegers().write(0, value);
    }

    /**
     * Retrieve the entity of the painting that will be spawned.
     *
     * @param world - the current world of the entity.
     * @return The spawned entity.
     */
    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    /**
     * Retrieve the entity of the painting that will be spawned.
     *
     * @param event - the packet event.
     * @return The spawned entity.
     */
    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    public ItemSlot getSlot() {
        return handle.getItemSlots().read(0);
    }

    public void setSlot(ItemSlot value) {
        if (handle.getItemSlots().getValues().size() > 0) {
            handle.getItemSlots().write(0, value);
        }
    }

    /**
     * Retrieve Item.
     * <p>
     * Notes: item in slot format
     *
     * @return The current Item
     */
    public ItemStack getItem() {
        return handle.getItemModifier().read(0);
    }

    /**
     * Set Item.
     *
     * @param value - new value.
     */
    public void setItem(ItemStack value) {
        if (handle.getItemModifier().getValues().size() > 0) {
            handle.getItemModifier().write(0, value);
        }
    }
}
