package org.jbourdon.thinkgear;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbourdon.thinkgear.consumer.InfluxConsumer;
import org.jbourdon.thinkgear.producer.DatagramWorker;
import org.jbourdon.thinkgear.producer.QueueSpliterator;

import java.util.Spliterator;
import java.util.stream.StreamSupport;

public class App {

    private static final Logger LOGGER= LogManager.getLogger(App.class);


    public static void main(String args[]) {
        DatagramWorker datagramWorker = new DatagramWorker("/dev/tty.juliens_brain-DevB");
        datagramWorker.start();
        Spliterator<ThinkGearDataRow> rowSpliterator = new QueueSpliterator<>(datagramWorker.getDataRowQueue(), 10000);
        try(InfluxConsumer influxConsumer = new InfluxConsumer()) {
            StreamSupport.stream(rowSpliterator, false).forEach(influxConsumer::insertRow);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
