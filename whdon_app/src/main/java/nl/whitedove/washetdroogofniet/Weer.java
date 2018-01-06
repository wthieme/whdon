package nl.whitedove.washetdroogofniet;

class Weer {

    private int graden;
    private int wind;
    private String plaats;
    private String icon;
    private WeerHelper.WindDirection windDir;

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

    WeerHelper.WindDirection getWindDir() {
        return windDir;
    }

    void setWindDir(int windRichting) {
        if (windRichting == -1) {
            this.windDir = WeerHelper.WindDirection.Onbekend;
        } else if (windRichting > 0 && windRichting <= 22.5) {
            this.windDir = WeerHelper.WindDirection.Noord;
        } else if (windRichting > 22.5 && windRichting <= 67.5) {
            this.windDir = WeerHelper.WindDirection.NoordOost;
        } else if (windRichting > 67.5 && windRichting <= 112.5) {
            this.windDir = WeerHelper.WindDirection.Oost;
        } else if (windRichting > 112.5 && windRichting <= 157.5) {
            this.windDir = WeerHelper.WindDirection.ZuidOost;
        } else if (windRichting > 157.5 && windRichting <= 202.5) {
            this.windDir = WeerHelper.WindDirection.Zuid;
        } else if (windRichting > 202.5 && windRichting <= 247.5) {
            this.windDir = WeerHelper.WindDirection.ZuidWest;
        } else if (windRichting > 247.5 && windRichting <= 292.5) {
            this.windDir = WeerHelper.WindDirection.West;
        } else if (windRichting > 292.5 && windRichting <= 337.5) {
            this.windDir = WeerHelper.WindDirection.NoordWest;
        } else if (windRichting > 337.5 && windRichting <= 360) {
            this.windDir = WeerHelper.WindDirection.Noord;
        }
    }
}
