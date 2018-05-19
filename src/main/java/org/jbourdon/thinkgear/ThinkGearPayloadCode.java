package org.jbourdon.thinkgear;

public enum ThinkGearPayloadCode {

    POOR_SIGNAL_QUALITY(1),
    HEART_RATE(1),
    ATTENTION(1),
    MEDITATION(1),
    EIGHT_BIT_RAW(1),
    RAW_MARKER(1),
    RAW_WAVE_VALUE(2),
    EEG_POWER(32),
    ASIC_EEG_POWER(25),
    RRINTERVAL(2),
    UNKNOWN(1);


    private int length = 1;

    ThinkGearPayloadCode(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }


    public static ThinkGearPayloadCode fromByte(byte code) {
        switch (code & 0xFF) {
            case 0x02:
                return POOR_SIGNAL_QUALITY;
            case 0x03:
                return HEART_RATE;
            case 0x04:
                return ATTENTION;
            case 0x05:
                return MEDITATION;
            case 0x06:
                return EIGHT_BIT_RAW;
            case 0x07:
                return RAW_MARKER;
            case 0x80:
                return RAW_WAVE_VALUE;
            case 0x81:
                return EEG_POWER;
            case 0x83:
                return ASIC_EEG_POWER;
            case 0x86:
                return RRINTERVAL;
                default:
                    return UNKNOWN;
        }
    }



}
