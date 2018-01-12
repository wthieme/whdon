package nl.whitedove.washetdroogofniet;

import java.util.Comparator;

public class StatsWindComparator implements Comparator<StatistiekWind> {
    static final StatsWindComparator instance=new StatsWindComparator();

    public int compare(StatistiekWind left, StatistiekWind right) {
        return Double.compare(left.getWindDir().getValue(), right.getWindDir().getValue());
    }
}
