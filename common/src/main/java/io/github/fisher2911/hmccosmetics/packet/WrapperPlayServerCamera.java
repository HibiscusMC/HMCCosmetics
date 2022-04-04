package io.github.fisher2911.hmccosmetics.packet;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerCamera extends PacketWrapper<WrapperPlayServerCamera> {

    private int entityID;

    public WrapperPlayServerCamera(final int entityID) {
        super(PacketType.Play.Server.CAMERA);
        this.entityID = entityID;
    }

    @Override
    public void read() {
        this.entityID = this.readVarInt();
    }

    @Override
    public void copy(WrapperPlayServerCamera wrapper) {
        this.entityID = wrapper.entityID;
    }

    public void write() {
        this.writeVarInt(this.entityID);
    }

    public int getEntityId() {
        return this.entityID;
    }

    public void setEntityIds(int entityID) {
        this.entityID = entityID;
    }

}
