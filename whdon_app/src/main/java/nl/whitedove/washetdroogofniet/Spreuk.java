package nl.whitedove.washetdroogofniet;

class Spreuk {

    private int maand;
    private int dag;
    private String spreuk;

    Spreuk(int maand, int dag, String spreuk) {
        this.dag = dag;
        this.maand = maand;
        this.spreuk = spreuk;
    }

    int getDag() {
        return dag;
    }

    String getSpreuk() {
        return spreuk;
    }
}
