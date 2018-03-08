package nl.whitedove.washetdroogofniet;

class StatistiekDagVdWeek {

    private int dag;
    private int aantalDroog;
    private int aantalNat;

    int getDag() {
        return dag;
    }

    void setDag(int dag) {
        this.dag = dag;
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
