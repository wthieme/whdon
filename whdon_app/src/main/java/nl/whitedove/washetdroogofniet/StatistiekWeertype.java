package nl.whitedove.washetdroogofniet;

class StatistiekWeertype {

    private WeerHelper.WeerType weerType;
    private String weerTypeOmschrijving;
    private int aantal;
    private float percentage;

    WeerHelper.WeerType getWeerType() {
        return weerType;
    }

    void setWeerType(WeerHelper.WeerType weerType) {
        this.weerType = weerType;
    }

    String getWeerTypeOmschrijving() {
        return weerTypeOmschrijving;
    }

    void setWeerTypeOmschrijving(String weerTypeOmschrijving) {
        this.weerTypeOmschrijving = weerTypeOmschrijving;
    }

    int getAantal() {
        return aantal;
    }

    void setAantal(int aantal) {
        this.aantal = aantal;
    }

    float getPercentage() {
        return percentage;
    }

    void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
