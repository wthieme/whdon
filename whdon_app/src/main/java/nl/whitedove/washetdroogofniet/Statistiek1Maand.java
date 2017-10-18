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

    public int getAantalTemperatuur() {
        return aantalTemperatuur;
    }

    public void setAantalTemperatuur(int aantalTemperatuur) {
        this.aantalTemperatuur = aantalTemperatuur;
    }

    public int getSomTemperatuur() {
        return somTemperatuur;
    }

    public void setSomTemperatuur(int somTemperatuur) {
        this.somTemperatuur = somTemperatuur;
    }
}
