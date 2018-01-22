package nl.whitedove.washetdroogofniet.backend;

public class Melding {

    private String locatie;
    private Long datum;
    private Boolean nat;
    private Boolean droog;
    private String id;
    private String error;
    private Long temperatuur;
    private Long weerType;
    private Long windSpeed;
    private Long windDir;
    private String land;

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

    public Long getTemperatuur() {
        return temperatuur;
    }

    public void setTemperatuur(Long temperatuur) {
        this.temperatuur = temperatuur;
    }

    public Long getWeerType() {
        return weerType;
    }

    public void setWeerType(Long weerType) {
        this.weerType = weerType;
    }

    public Long getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Long windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Long getWindDir() {
        return windDir;
    }

    public void setWindDir(Long windDir) {
        this.windDir = windDir;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }
}
