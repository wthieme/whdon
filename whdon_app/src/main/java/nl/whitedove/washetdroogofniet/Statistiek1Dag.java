package nl.whitedove.washetdroogofniet;

import org.joda.time.DateTime;

class Statistiek1Dag {

    private DateTime datum;
    private int aantalDroog;
    private int aantalNat;

    DateTime getDatum() {
        return datum;
    }

    void setDatum(DateTime datum) {
        this.datum = datum;
    }

    int getAantalDroog() {
        return aantalDroog;
    }

    void setAantalDroog(int aantalDroog) {
        this.aantalDroog = aantalDroog;
    }

    int getAantalNat() {
        return aantalNat;
    }

    void setAantalNat(int aantalNat) {
        this.aantalNat = aantalNat;
    }

}
