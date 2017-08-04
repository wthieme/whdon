/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package nl.whitedove.washetdroogofniet.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

@Api(
        name = "whdonApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.washetdroogofniet.whitedove.nl",
                ownerName = "backend.washetdroogofniet.whitedove.nl"
        )
)
public class WhdonEndpoint {
    private final static String MELDING = "Melding";
    private final static String DATUM = "datum";
    private final static String DROOG = "droog";
    private final static String NAT = "nat";
    private final static String ID = "id";
    private final static String LOCATIE = "locatie";

    // Get the Datastore Service
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @ApiMethod(name = "MeldingOpslaan")
    public Melding MeldingOpslaan(Melding melding) {

        if (melding.getLocatie() == null ||
                melding.getLocatie().equalsIgnoreCase("") ||
                melding.getLocatie().equalsIgnoreCase("Onbekend") ||
                melding.getLocatie().equalsIgnoreCase("Nederland")) {
            Melding response = new Melding();
            response.setError("Melding niet mogelijk, locatie onbekend");
            return response;
        }

        DateTime dtNu = DateTime.now();
        Melding laatste = GetLaatsteMelding(melding.getId());
        if (laatste.getLocatie() != null) {
            Long last = laatste.getDatum() + 900000L;
            if (last > dtNu.getMillis()) {
                Melding response = new Melding();
                response.setError("Er is maximaal 1 keer per kwartier een melding mogelijk");
                return response;
            }
        }

        Entity meld = new Entity(MELDING);
        meld.setProperty(DATUM, dtNu.getMillis());
        meld.setProperty(DROOG, melding.getDroog());
        meld.setProperty(ID, melding.getId());
        meld.setProperty(LOCATIE, melding.getLocatie());
        meld.setProperty(NAT, melding.getNat());
        datastore.put(meld);

        Melding response = new Melding();
        response.setError("");
        return response;
    }

    @ApiMethod(name = "GetVersie")
    public Versie GetVersie() {
        Versie response = new Versie();
        response.setNaam("Was Het Droog Of Niet API");
        response.setversie("8.0");
        return response;
    }

    private Melding GetLaatsteMelding(String id) {

        Query.Filter idFilter = new Query.FilterPredicate(ID, Query.FilterOperator.EQUAL, id);
        Query qLaatste = new Query(MELDING)
                .setFilter(idFilter)
                .addSort(DATUM, Query.SortDirection.DESCENDING);

        PreparedQuery pq = datastore.prepare(qLaatste);
        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(1));

        Melding melding = new Melding();
        melding.setId(id);
        if (result.size() == 0) {
            melding.setError("Geen meldingen");
            return melding;
        }
        Entity laatste = result.get(0);
        melding.setLocatie((String) laatste.getProperty(LOCATIE));
        melding.setDatum((Long) laatste.getProperty(DATUM));
        melding.setDroog((Boolean) laatste.getProperty(DROOG));
        melding.setNat((Boolean) laatste.getProperty(NAT));
        return melding;
    }

    @ApiMethod(name = "GetAlleMeldingenVanaf")
    public ArrayList<Melding> GetAlleMeldingenVanaf(@Named(DATUM) Long datumVanaf) {

        ArrayList<Melding> response = new ArrayList<>();
        Query.Filter datumFilter = new Query.FilterPredicate(DATUM, Query.FilterOperator.GREATER_THAN_OR_EQUAL, datumVanaf);
        Query qMeldingen = new Query(MELDING).setFilter(datumFilter);

        List<Entity> meldingen = datastore.prepare(qMeldingen).asList(FetchOptions.Builder.withChunkSize(100));

        for (Entity rMeld : meldingen) {
            Melding melding = new Melding();
            melding.setError("");
            melding.setDroog((Boolean) rMeld.getProperty(DROOG));
            melding.setLocatie((String) rMeld.getProperty(LOCATIE));
            melding.setId((String) rMeld.getProperty(ID));
            melding.setDatum((long) rMeld.getProperty(DATUM));
            melding.setNat((Boolean) rMeld.getProperty(NAT));
            response.add(melding);
        }

        return response;
    }
}