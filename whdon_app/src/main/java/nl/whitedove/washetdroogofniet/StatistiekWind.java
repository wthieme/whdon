package nl.whitedove.washetdroogofniet;

class StatistiekWind {

    private WeerHelper.WindDirection WindDir;
    private String windOmschrijving;
    private int aantal;
    private float windSpeed;
    private float percentage;

    public String getWindOmschrijving() {
        return windOmschrijving;
    }

    public void setWindOmschrijving(String windOmschrijving) {
        this.windOmschrijving = windOmschrijving;
    }

    public int getAantal() {
        return aantal;
    }

    public void setAantal(int aantal) {
        this.aantal = aantal;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public WeerHelper.WindDirection getWindDir() {
        return WindDir;
    }

    public void setWindDir(WeerHelper.WindDirection windDir) {
        WindDir = windDir;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
