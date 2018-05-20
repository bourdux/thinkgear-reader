package org.jbourdon.thinkgear.consumer;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.jbourdon.thinkgear.ThinkGearDataRow;

import java.util.concurrent.TimeUnit;

public class InfluxConsumer implements AutoCloseable {

    private final InfluxDB influxDB;

    public InfluxConsumer() {
        this.influxDB = InfluxDBFactory.connect("http://localhost:32768", "root", "root");
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        influxDB.createDatabase("juliensbrain");
        influxDB.setDatabase("juliensbrain");
        influxDB.createRetentionPolicy("default", "juliensbrain", "30d", "30m", 2, true);
        influxDB.setRetentionPolicy("default");
        influxDB.enableBatch(100, 200, TimeUnit.MILLISECONDS);

    }

    public void testInflux() {

    }

    public void insertRow(ThinkGearDataRow row) {
        influxDB.write(Point.measurement("wave")
        .time(row.getTimestamp(), TimeUnit.MILLISECONDS)
                .addField("meditation", row.getMeditation())
                .addField("attention", row.getAttention())
        .build());
        influxDB.write(Point.measurement("eeg")
                .time(row.getTimestamp(), TimeUnit.MILLISECONDS)
                .addField("theta", row.getTheta())
                .addField("delta", row.getDelta())
                .addField("lowalpha", row.getLowAlpha())
                .addField("highalpha", row.getHighAlpha())
                .addField("lowbeta", row.getLowBeta())
                .addField("highbeta", row.getHighBeta())
                .addField("lowgamma",row.getLowGamma())
                .addField("midgamma", row.getMidGamma())
                .build());

    }

    @Override
    public void close() throws Exception {
        influxDB.close();
    }
}
