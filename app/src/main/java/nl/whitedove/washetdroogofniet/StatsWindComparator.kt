package nl.whitedove.washetdroogofniet

import java.util.Comparator

class StatsWindComparator : Comparator<StatistiekWind> {

    override fun compare(left: StatistiekWind, right: StatistiekWind): Int {
        return java.lang.Double.compare(left.windDir.value.toDouble(), right.windDir.value.toDouble())
    }

    companion object {
        internal val instance = StatsWindComparator()
    }
}
