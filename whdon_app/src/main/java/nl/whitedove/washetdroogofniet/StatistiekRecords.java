package nl.whitedove.washetdroogofniet;

import org.joda.time.DateTime;

class StatistiekRecords {

    private DateTime MaxWindSpeedDate;
    private float maxWindSpeed;
    private DateTime MinTempDate;
    private float minTemp;
    private DateTime MaxTempDate;
    private float maxTemp;
    private DateTime wettestMonth;
    private float percentWet;

    public DateTime getMaxWindSpeedDate() {
        return MaxWindSpeedDate;
    }

    public void setMaxWindSpeedDate(DateTime maxWindSpeedDate) {
        MaxWindSpeedDate = maxWindSpeedDate;
    }

    public float getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setMaxWindSpeed(float maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }

    public DateTime getMinTempDate() {
        return MinTempDate;
    }

    public void setMinTempDate(DateTime minTempDate) {
        MinTempDate = minTempDate;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(float minTemp) {
        this.minTemp = minTemp;
    }

    public DateTime getMaxTempDate() {
        return MaxTempDate;
    }

    public void setMaxTempDate(DateTime maxTempDate) {
        MaxTempDate = maxTempDate;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(float maxTemp) {
        this.maxTemp = maxTemp;
    }

    public DateTime getWettestMonth() {
        return wettestMonth;
    }

    public void setWettestMonth(DateTime wettestMonth) {
        this.wettestMonth = wettestMonth;
    }

    public float getPercentWet() {
        return percentWet;
    }

    public void setPercentWet(float percentWet) {
        this.percentWet = percentWet;
    }

    public DateTime getDriestMonth() {
        return driestMonth;
    }

    public void setDriestMonth(DateTime driestMonth) {
        this.driestMonth = driestMonth;
    }

    public float getPercentDry() {
        return percentDry;
    }

    public void setPercentDry(float percentDry) {
        this.percentDry = percentDry;
    }

    public int getLongestStreakDry() {
        return longestStreakDry;
    }

    public void setLongestStreakDry(int longestStreakDry) {
        this.longestStreakDry = longestStreakDry;
    }

    public DateTime getLongestStreakDryFrom() {
        return longestStreakDryFrom;
    }

    public void setLongestStreakDryFrom(DateTime longestStreakDryFrom) {
        this.longestStreakDryFrom = longestStreakDryFrom;
    }

    public DateTime getLongestStreakDryTill() {
        return longestStreakDryTill;
    }

    public void setLongestStreakDryTill(DateTime longestStreakDryTill) {
        this.longestStreakDryTill = longestStreakDryTill;
    }

    public int getLongestStreakWet() {
        return longestStreakWet;
    }

    public void setLongestStreakWet(int longestStreakWet) {
        this.longestStreakWet = longestStreakWet;
    }

    public DateTime getLongestStreakWetFrom() {
        return longestStreakWetFrom;
    }

    public void setLongestStreakWetFrom(DateTime longestStreakWetFrom) {
        this.longestStreakWetFrom = longestStreakWetFrom;
    }

    public DateTime getLongestStreakWetTill() {
        return longestStreakWetTill;
    }

    public void setLongestStreakWetTill(DateTime longestStreakWetTill) {
        this.longestStreakWetTill = longestStreakWetTill;
    }

    private DateTime driestMonth;
    private float percentDry;
    private int longestStreakDry;
    private DateTime longestStreakDryFrom;
    private DateTime longestStreakDryTill;
    private int longestStreakWet;
    private DateTime longestStreakWetFrom;
    private DateTime longestStreakWetTill;

}

