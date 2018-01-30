package com.moko.support.entity;


import java.io.Serializable;

public enum SlotEnum implements Serializable {
    SLOT_1(0, "SLOT1"),
    SLOT_2(1, "SLOT2"),
    SLOT_3(2, "SLOT3"),
    SLOT_4(3, "SLOT4"),
    SLOT_5(4, "SLOT5");
    private String title;
    private int slot;

    SlotEnum(int slot, String title) {
        this.slot = slot;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getSlot() {
        return slot;
    }
}
