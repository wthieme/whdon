package nl.whitedove.washetdroogofniet;

class Statistiek {

    private String locatie;
    private int aantalNat;
    private int aantalDroog;
    private String id;

    String getLocatie() {
        return locatie;
    }

    void setLocatie(String locatie) {
        this.locatie = locatie;
    }

    int getAantalNat() {
        return aantalNat;
    }

    void setAantalNat(int aantalNat) {
        this.aantalNat = aantalNat;
    }

    int getAantalDroog() {
        return aantalDroog;
    }

    void setAantalDroog(int aantalDroog) {
        this.aantalDroog = aantalDroog;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

