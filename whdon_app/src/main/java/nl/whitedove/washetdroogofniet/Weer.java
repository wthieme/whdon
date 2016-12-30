package nl.whitedove.washetdroogofniet;

class Weer {

    private int graden;
    private int wind;
    private String plaats;
    private String icon;
    private int windRichting;

    int getGraden() {
        return graden;
    }

    void setGraden(int graden) {
        this.graden = graden;
    }

    int getWind() {
        return wind;
    }

    void setWind(int wind) {
        this.wind = wind;
    }

    String getPlaats() {
        return plaats;
    }

    void setPlaats(String plaats) {
        this.plaats = plaats;
    }

    String getIcon() {
        return icon;
    }

    void setIcon(String icon) {
        this.icon = icon;
    }

    int getWindRichting() {
        return windRichting;
    }

    void setWindRichting(int windRichting) {
        this.windRichting = windRichting;
    }

}
