package nl.whitedove.washetdroogofniet;

import org.joda.time.DateTime;

class Statistiek1Dag {

    private DateTime Datum;
    private int AantalDroog;
    private int AantalNat;
    private float MinTemperatuur;
    private float MaxTemperatuur;

    DateTime getDatum() {
        return Datum;
    }

    void setDatum(DateTime datum) {
        this.Datum = datum;
    }

    int getAantalDroog() {
        return AantalDroog;
    }

    void setAantalDroog(int aantalDroog) {
        this.AantalDroog = aantalDroog;
    }

    int getAantalNat() {
        return AantalNat;
    }

    void setAantalNat(int aantalNat) {
        this.AantalNat = aantalNat;
    }

    float getMinTemperatuur() {
        return MinTemperatuur;
    }

    void setMinTemperatuur(float minTemperatuur) {
        MinTemperatuur = minTemperatuur;
    }

    float getMaxTemperatuur() {
        return MaxTemperatuur;
    }

    void setMaxTemperatuur(float maxTemperatuur) {
        MaxTemperatuur = maxTemperatuur;
    }
}
