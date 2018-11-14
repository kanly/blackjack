package org.pmazzoncini;


import java.util.concurrent.TimeUnit;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public interface Durations {
    FiniteDuration in3Seconds = Duration.create(3, TimeUnit.SECONDS);
    FiniteDuration in5Seconds = Duration.create(5, TimeUnit.SECONDS);
    FiniteDuration in15Seconds = Duration.create(15, TimeUnit.SECONDS);
}
