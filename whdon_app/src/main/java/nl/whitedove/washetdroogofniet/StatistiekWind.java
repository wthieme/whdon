package nl.whitedove.washetdroogofniet;

class StatistiekWind {

    private WeerHelper.WindDirection WindDir;
    private String windOmschrijving;
    private int aantal;
    private float avgWindSpeed;
    private float maxWindSpeed;
    private float percentage;

    String getWindOmschrijving() {
        return windOmschrijving;
    }

    void setWindOmschrijving(String windOmschrijving) {
        this.windOmschrijving = windOmschrijving;
    }

    int getAantal() {
        return aantal;
    }

    void setAantal(int aantal) {
        this.aantal = aantal;
    }

    WeerHelper.WindDirection getWindDir() {
        return WindDir;
    }

    void setWindDir(WeerHelper.WindDirection windDir) {
        WindDir = windDir;
    }

    float getPercentage() {
        return percentage;
    }

    void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    float getMaxWindSpeed() {
        return maxWindSpeed;
    }

    void setMaxWindSpeed(float maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }

    float getAvgWindSpeed() {
        return avgWindSpeed;
    }

    void setAvgWindSpeed(float avgWindSpeed) {
        this.avgWindSpeed = avgWindSpeed;
    }
}
