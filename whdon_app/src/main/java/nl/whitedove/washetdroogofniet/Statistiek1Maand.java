package nl.whitedove.washetdroogofniet;

class Statistiek1Maand {

    private int maand;
    private int aantalDroog;
    private int aantalNat;
    private int aantalTemperatuur;
    private int somTemperatuur;

    int getMaand() {
        return maand;
    }

    void setMaand(int maand) {
        this.maand = maand;
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

    int getAantalTemperatuur() {
        return aantalTemperatuur;
    }

    void setAantalTemperatuur(int aantalTemperatuur) {
        this.aantalTemperatuur = aantalTemperatuur;
    }

    int getSomTemperatuur() {
        return somTemperatuur;
    }

    void setSomTemperatuur(int somTemperatuur) {
        this.somTemperatuur = somTemperatuur;
    }
}
