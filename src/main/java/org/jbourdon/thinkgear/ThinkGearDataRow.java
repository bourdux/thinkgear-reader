package org.jbourdon.thinkgear;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ThinkGearDataRow {

    private static final Logger LOGGER = LogManager.getLogger(ThinkGearDataRow.class);

    private static final byte EXCODE = 0x55;

    private final int poorSignalQuality;
    private final int attention;
    private final int meditation;

    private final int delta;
    private final int theta;
    private final int lowAlpha;
    private final int highAlpha;
    private final int lowBeta;
    private final int highBeta;
    private final int lowGamma;
    private final int midGamma;

    public int getAttention() {
        return attention;
    }

    public int getMeditation() {
        return meditation;
    }

    public int getDelta() {
        return delta;
    }

    public int getTheta() {
        return theta;
    }

    public int getLowAlpha() {
        return lowAlpha;
    }

    public int getHighAlpha() {
        return highAlpha;
    }

    public int getLowBeta() {
        return lowBeta;
    }

    public int getHighBeta() {
        return highBeta;
    }

    public int getLowGamma() {
        return lowGamma;
    }

    public int getMidGamma() {
        return midGamma;
    }

    public ThinkGearDataRow(int poorSignalQuality, int attention, int meditation, int delta, int theta, int lowAlpha, int highAlpha, int lowBeta, int highBeta, int lowGamma, int midGamma) {
        this.poorSignalQuality = poorSignalQuality;
        this.attention = attention;
        this.meditation = meditation;
        this.delta = delta;
        this.theta = theta;
        this.lowAlpha = lowAlpha;
        this.highAlpha = highAlpha;
        this.lowBeta = lowBeta;
        this.highBeta = highBeta;
        this.lowGamma = lowGamma;
        this.midGamma = midGamma;
    }

    public static ThinkGearDataRow fromBuffer(ByteBuffer byteBuffer) {
        byte[] payload = byteBuffer.array();
        int bytesParsed = 0;
        byte code = 0;
        int length = 0;
        int extendedCodeLevel = 0;

        /* Data row values */
        int poorSignalQuality = 0;
        int attention = 0;
        int meditation = 0;
        int delta = 0;
        int theta = 0;
        int lowAlpha = 0;
        int highAlpha = 0;
        int lowBeta = 0;
        int highBeta = 0;
        int lowGamma = 0;
        int midGamma = 0;

        try {
        while(bytesParsed < payload.length) {
            /* Parse the extendedCodeLevel, code, and length */
            while(payload[bytesParsed] == EXCODE) {
                extendedCodeLevel ++;
                bytesParsed ++;
            }
            code = payload[bytesParsed++];
            if (code == 0x80) {
                length = payload[bytesParsed++];
            } else {
                length = 1;
            }

            ThinkGearPayloadCode payloadCode = ThinkGearPayloadCode.fromByte(code);
/*
            if (length == 1) {
                length = payloadCode.getLength();
            }
*/
            LOGGER.debug(String.format("EXCODE level: %d CODE: 0x%02X %s length: %d",
                    extendedCodeLevel, code, payloadCode, length) );
            switch (payloadCode) {
                case POOR_SIGNAL_QUALITY:
                    poorSignalQuality = payload[bytesParsed] & 0xFF;
                    LOGGER.debug("Poor signal quality parsed: {}", poorSignalQuality);
                    break;
                case ATTENTION:
                    attention = payload[bytesParsed] & 0xFF;
                    LOGGER.debug("Attention parsed: {}", attention);
                    break;
                case MEDITATION:
                    meditation = payload[bytesParsed] & 0xFF;
                    LOGGER.debug("Meditation parsed: {}", meditation);
                    break;
                case ASIC_EEG_POWER:
                    delta = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed delta: {}", delta);
                    theta = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed theta: {}", theta);
                    lowAlpha = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed low alpha: {}", lowAlpha);
                    highAlpha = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed high alpha: {}", highAlpha);
                    lowBeta = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed low beta: {}", lowBeta);
                    highBeta = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed high beta: {}", highBeta);
                    lowGamma = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed low gamma: {}", lowGamma);
                    midGamma = convertToBigEndianInteger(payload[bytesParsed++], payload[bytesParsed++], payload[bytesParsed++]);
                    LOGGER.debug("Parsed mid gamma: {}", midGamma);
                    break;

                    default: {
                        StringBuffer sb = new StringBuffer();
                        for(int i=0; i<length; i++ ) {
                            sb.append(String.format(" %02X", payload[bytesParsed+i] & 0xFF ));
                        }
                        LOGGER.debug("Other value {}", sb);
                    }

            }

            /* Increment the bytesParsed by the length of the Data Value */
            bytesParsed += length;
        }
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Payload corrupted", e);
        }

        return new ThinkGearDataRow(poorSignalQuality, attention, meditation, delta, theta, lowAlpha, highAlpha, lowBeta, highBeta, lowGamma, midGamma);
    }

    private static int convertToBigEndianInteger(byte high, byte mid, byte low) {
        ByteBuffer writebb = ByteBuffer.allocate(4);
        writebb.put((byte)0x00).put(high).put(mid).put(low);
        writebb.order(ByteOrder.BIG_ENDIAN);
        ByteBuffer bb = ByteBuffer.wrap(writebb.array());
        return (int)(bb.getInt() & 0x00000000ffffffffL);
    }

    @Override
    public String toString() {
        return "ThinkGearDataRow{" +
                "poorSignalQuality=" + poorSignalQuality +
                ", attention=" + attention +
                ", meditation=" + meditation +
                ", delta=" + delta +
                ", theta=" + theta +
                ", lowAlpha=" + lowAlpha +
                ", highAlpha=" + highAlpha +
                ", lowBeta=" + lowBeta +
                ", highBeta=" + highBeta +
                ", lowGamma=" + lowGamma +
                ", midGamma=" + midGamma +
                '}';
    }
}
