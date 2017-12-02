package nl.whitedove.washetdroogofniet;

class StatistiekWeertype {

    private WeerHelper.WeerType weerType;
    private String weerTypeOschrijving;
    private int aantal;
    private float percentage;

    WeerHelper.WeerType getWeerType() {
        return weerType;
    }

    void setWeerType(WeerHelper.WeerType weerType) {
        this.weerType = weerType;
    }

    String getWeerTypeOschrijving() {
        return weerTypeOschrijving;
    }

    void setWeerTypeOschrijving(String weerTypeOschrijving) {
        this.weerTypeOschrijving = weerTypeOschrijving;
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
