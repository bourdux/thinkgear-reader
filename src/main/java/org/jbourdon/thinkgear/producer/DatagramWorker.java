package org.jbourdon.thinkgear.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbourdon.thinkgear.ThinkGearDataRow;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatagramWorker extends Thread {

    private static final Logger LOGGER= LogManager.getLogger(DatagramWorker.class);

    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;
    private static final int MAX_PLENGTH = 170;
    private static final int QUEUE_MAX_SIZE = 1000;
    private static final int TIMEOUT_SECONDS = 10;

    private final String deviceName;
    private final BlockingQueue<ThinkGearDataRow> dataRowQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    public DatagramWorker() {
        this("/dev/tty.juliens_brain-DevB");
    }

    public DatagramWorker(String deviceName) {
        this.deviceName = deviceName;
    }

    public void run() {
        try(FileInputStream inputStream = new FileInputStream(deviceName)) {
            while (!this.isInterrupted()) {
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
                            //LOGGER.info("Parsed row: {}", thinkGearDataRow);
                            this.insertRowInQueue(thinkGearDataRow);
                        }
                        LOGGER.debug("Checksum pass " + checkSumOK);

                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("I/O error", e);
        }
    }

    private void insertRowInQueue(ThinkGearDataRow row) {
        try {
            if(!dataRowQueue.offer(row, TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.warn("Timeout: could not put the row in the queue, skipping");
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted");
            e.printStackTrace();
        }
    }

    public BlockingQueue<ThinkGearDataRow> getDataRowQueue() {
        return this.dataRowQueue;
    }
}
