package nl.whitedove.washetdroogofniet;

class Statistiek1Maand {

    private int Maand;
    private int AantalDroog;
    private int AantalNat;
    private float MinTemperatuur;
    private float MaxTemperatuur;

    int getMaand() {
        return Maand;
    }

    void setMaand(int maand) {
        this.Maand = maand;
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
        this.MinTemperatuur = minTemperatuur;
    }

    float getMaxTemperatuur() {
        return MaxTemperatuur;
    }

    void setMaxTemperatuur(float maxTemperatuur) {
        this.MaxTemperatuur = maxTemperatuur;
    }
}
