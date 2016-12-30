package nl.whitedove.washetdroogofniet.backend;

public class Melding {

    private String locatie;
    private Long datum;
    private Boolean nat;
    private Boolean droog;
    private String id;
    private String error;

    public String getLocatie() {
        return locatie;
    }

    public void setLocatie(String locatie) {
        this.locatie = locatie;
    }

    public Long getDatum() {
        return datum;
    }

    public void setDatum(Long datum) {
        this.datum = datum;
    }

    public Boolean getNat() {
        return nat;
    }

    public void setNat(Boolean nat) {
        this.nat = nat;
    }

    public Boolean getDroog() {
        return droog;
    }

    public void setDroog(Boolean droog) {
        this.droog = droog;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
