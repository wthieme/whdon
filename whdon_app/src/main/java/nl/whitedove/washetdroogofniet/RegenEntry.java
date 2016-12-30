package nl.whitedove.washetdroogofniet;

class RegenEntry {

    private String Tijd;
    private int Regen;

    RegenEntry(String tijd, int regen) {
        this.Tijd = tijd;
        this.Regen = regen;
    }

    RegenEntry() {
    }

    int getRegen() {
        return Regen;
    }

    void setRegen(int regen) {
        this.Regen = regen;
    }

    String getTijd() {
        return Tijd;
    }

    void setTijd(String tijd) {
        this.Tijd = tijd;
    }

}
