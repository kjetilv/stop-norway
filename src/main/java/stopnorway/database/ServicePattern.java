package stopnorway.database;

import stopnorway.entur.ScheduledStopPoint;
import stopnorway.util.Accept;

import javax.net.ssl.SSLParameters;
import java.io.Serializable;
import java.util.Collection;

public class ServicePattern implements Serializable {

    private final String name;

    private final Collection<ScheduledStopPoint> stopPoints;

    private final Collection<ServiceLeg> serviceLegs;

    public ServicePattern(String name,
                          Collection<ScheduledStopPoint> stopPoints,
                          Collection<ServiceLeg> serviceLegs) {
        this.name = name;
        this.stopPoints = Accept.list(stopPoints);
        this.serviceLegs = Accept.list(serviceLegs);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + " serviceLegs=" + serviceLegs.size() + "]";
    }
}
