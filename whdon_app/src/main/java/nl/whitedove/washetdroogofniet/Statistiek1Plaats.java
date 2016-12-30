package nl.whitedove.washetdroogofniet;

 class Statistiek1Plaats {

    private String locatie;
    private int aantalDroog;
    private int aantalNat;
    private Long datumStart;
    private Long datumEnd;

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

     String getLocatie() {
        return locatie;
    }

     void setLocatie(String locatie) {
        this.locatie = locatie;
    }

     Long getDatumStart() {
        return datumStart;
    }

     void setDatumStart(Long datumStart) {
        this.datumStart = datumStart;
    }

     Long getDatumEnd() {
        return datumEnd;
    }

     void setDatumEnd(Long datumEnd) {
        this.datumEnd = datumEnd;
    }

}
