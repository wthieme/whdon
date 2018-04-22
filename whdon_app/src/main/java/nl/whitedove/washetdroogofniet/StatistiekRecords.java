package nl.whitedove.washetdroogofniet;

import org.joda.time.DateTime;

class StatistiekRecords {

    private DateTime MaxWindDatum;
    private int maxWind;
    private WeerHelper.WindDirection maxWindRichting;
    private DateTime MinTempDatum;
    private int minTemp;
    private DateTime MaxTempDatum;
    private int maxTemp;
    private DateTime natsteMaand;
    private float percentNat;
    private DateTime droogsteMaand;
    private float percentDroog;
    private DateTime langstePeriodeDroogVanaf;
    private DateTime langstePeriodeDroogTm;
    private DateTime langstePeriodeNatVanaf;
    private DateTime langstePeriodeNatTm;

    DateTime getMaxWindDatum() {
        return MaxWindDatum;
    }

    void setMaxWindDatum(DateTime maxWindDatum) {
        MaxWindDatum = maxWindDatum;
    }

    int getMaxWind() {
        return maxWind;
    }

    void setMaxWind(int maxWind) {
        this.maxWind = maxWind;
    }

    WeerHelper.WindDirection getMaxWindRichting() {
        return maxWindRichting;
    }

    void setMaxWindRichting(WeerHelper.WindDirection maxWindRichting) {
        this.maxWindRichting = maxWindRichting;
    }

    DateTime getMinTempDatum() {
        return MinTempDatum;
    }

    void setMinTempDatum(DateTime minTempDatum) {
        MinTempDatum = minTempDatum;
    }

    int getMinTemp() {
        return minTemp;
    }

    void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    DateTime getMaxTempDatum() {
        return MaxTempDatum;
    }

    void setMaxTempDatum(DateTime maxTempDatum) {
        MaxTempDatum = maxTempDatum;
    }

    int getMaxTemp() {
        return maxTemp;
    }

    void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    DateTime getNatsteMaand() {
        return natsteMaand;
    }

    void setNatsteMaand(DateTime natsteMaand) {
        this.natsteMaand = natsteMaand;
    }

    float getPercentNat() {
        return percentNat;
    }

    void setPercentNat(float percentNat) {
        this.percentNat = percentNat;
    }

    DateTime getDroogsteMaand() {
        return droogsteMaand;
    }

    void setDroogsteMaand(DateTime droogsteMaand) {
        this.droogsteMaand = droogsteMaand;
    }

    float getPercentDroog() {
        return percentDroog;
    }

    void setPercentDroog(float percentDroog) {
        this.percentDroog = percentDroog;
    }

    DateTime getLangstePeriodeDroogVanaf() {
        return langstePeriodeDroogVanaf;
    }

    void setLangstePeriodeDroogVanaf(DateTime langstePeriodeDroogVanaf) {
        this.langstePeriodeDroogVanaf = langstePeriodeDroogVanaf;
    }

    DateTime getLangstePeriodeDroogTm() {
        return langstePeriodeDroogTm;
    }

    void setLangstePeriodeDroogTm(DateTime langstePeriodeDroogTm) {
        this.langstePeriodeDroogTm = langstePeriodeDroogTm;
    }

    DateTime getLangstePeriodeNatVanaf() {
        return langstePeriodeNatVanaf;
    }

    void setLangstePeriodeNatVanaf(DateTime langstePeriodeNatVanaf) {
        this.langstePeriodeNatVanaf = langstePeriodeNatVanaf;
    }

    DateTime getLangstePeriodeNatTm() {
        return langstePeriodeNatTm;
    }

    void setLangstePeriodeNatTm(DateTime langstePeriodeNatTm) {
        this.langstePeriodeNatTm = langstePeriodeNatTm;
    }

}
