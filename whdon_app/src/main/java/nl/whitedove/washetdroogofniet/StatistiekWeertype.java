package nl.whitedove.washetdroogofniet;

class StatistiekWeertype {

    private WeerHelper.WeerType weerType;
    private String weerTypeOschrijving;
    private int aantal;

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
}
