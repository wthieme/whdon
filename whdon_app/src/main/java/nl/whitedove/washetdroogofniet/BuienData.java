package nl.whitedove.washetdroogofniet;

import java.util.ArrayList;

class BuienData {

    private ArrayList<RegenEntry> regenData;
    private Boolean NoData;

    ArrayList<RegenEntry> getRegenData() {
        return regenData;
    }

    void setRegenData(ArrayList<RegenEntry> regenData) {
        this.regenData = regenData;
    }

    Boolean getNoData() {
        return NoData;
    }

    void setNoData(Boolean noData) {
        NoData = noData;
    }
}
