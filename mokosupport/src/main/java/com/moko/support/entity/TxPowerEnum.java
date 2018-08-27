package com.moko.support.entity;


import java.io.Serializable;

public enum TxPowerEnum implements Serializable {
    NEGATIVE_40(-40),
    NEGATIVE_20(-20),
    NEGATIVE_16(-16),
    NEGATIVE_12(-12),
    NEGATIVE_8(-8),
    NEGATIVE_4(-4),
    NEGATIVE_0(0),
    POSITIVE_3(3),
    POSITIVE_4(4);

    private int txPower;

    TxPowerEnum(int txPower) {
        this.txPower = txPower;
    }

    public static TxPowerEnum fromOrdinal(int ordinal) {
        for (TxPowerEnum txPowerEnum : TxPowerEnum.values()) {
            if (txPowerEnum.ordinal() == ordinal) {
                return txPowerEnum;
            }
        }
        return null;
    }
    public static TxPowerEnum fromTxPower(int txPower) {
        for (TxPowerEnum txPowerEnum : TxPowerEnum.values()) {
            if (txPowerEnum.getTxPower() == txPower) {
                return txPowerEnum;
            }
        }
        return null;
    }

    public int getTxPower() {
        return txPower;
    }
}
