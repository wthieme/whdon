package nl.whitedove.washetdroogofniet;

import org.joda.time.DateTime;

class StatistiekAantalGebruikers {

    private DateTime datum;
    private int aantalGebruikers;

    DateTime getDatum() {
        return datum;
    }

    void setDatum(DateTime datum) {
        this.datum = datum;
    }

    int getAantalGebruikers() {
        return aantalGebruikers;
    }

    void setAantalGebruikers(int aantalGebruikers) {
        this.aantalGebruikers = aantalGebruikers;
    }

}
