package org.jbourdon.thinkgear;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;

public class DatagramReader {

    private static final Logger LOGGER= LogManager.getLogger(DatagramReader.class);

    private String deviceName = "/dev/tty.juliens_brain-DevB";
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;
    private static final int MAX_PLENGTH = 170;

    public void testRead() {
        boolean interrupted = false;
        try(FileInputStream inputStream = new FileInputStream(deviceName)) {
            while(!interrupted) {
                int bit = inputStream.read();
                LOGGER.debug("%02x ",inputStream.read());
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Can't find serial port {}", deviceName, e);
        } catch (IOException e) {
            LOGGER.error("Can't connect to serial port ", deviceName, e);
        }
    }

    public void testRead2() {
        try(FileInputStream inputStream = new FileInputStream(deviceName)) {
            boolean interrupted = false;
            while (!interrupted) {
                int nextByte = inputStream.read();
                if (nextByte == 0xAA) {
                   nextByte = inputStream.read();
                    if (nextByte == 0xAA) {
                        int pLength = inputStream.read();
                        if (pLength > MAX_PLENGTH) {
                            LOGGER.error("Packet Length is too high: " + pLength);
                        } else {
                            LOGGER.debug("Sync: plength " + pLength);
                        }
                        ByteBuffer bb = ByteBuffer.allocate(pLength);
                        int checksum = 0;
                        for (int i = 0; i< pLength; i++) {
                            nextByte = inputStream.read();
                            bb.put((byte) nextByte);
                            checksum += nextByte;
                        }
                        int packetChecksum = inputStream.read();
                        boolean checkSumOK = (~checksum & 0xFF) == packetChecksum;
                        if(checkSumOK) {
                            ThinkGearDataRow thinkGearDataRow = ThinkGearDataRow.fromBuffer(bb);
                            LOGGER.info("Parsed row: {}", thinkGearDataRow);
                        }
                        LOGGER.debug("Checksum pass " + checkSumOK);

                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("I/O error", e);
        }
    }
}
