package com.plugin.httptours;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.tours.TourLoaderPlugin;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Plugin(service="TourLoader",name="httptours")
@PluginDescription(title="Http Tours", description="Load Tours from an http source")
public class HttpTours implements TourLoaderPlugin {
    Logger LOG = LoggerFactory.getLogger(HttpTours.class);
    private static final HashMap<String,Object> EMPTY_MANIFEST = new HashMap<>();
    static {
        EMPTY_MANIFEST.put("tours",new HashMap());
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    static  {
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    private final OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new AgentInterceptor()).build();

    @PluginProperty(name="tourEndpoint",title = "Tour Endpoint",description = "The base endpoint from which to load the tour manifest and the tours")
    String tourEndpoint = null;
    @PluginProperty(name="tourManifestName",title="Tour Manifest Name",description = "The name of the tour manifest",defaultValue = "tour-manifest.json")
    String tourManifestName = null;
    @PluginProperty(name="toursSubpath",title="Tours Sub Path",description = "The sub path in which all tours are located. The url format to access tours will be $tourEndpoint/$toursSubpath/$tourKey",defaultValue = "tours")
    String toursSubpath = null;

    @Override
    public Map getTourManifest() {
        if(tourEndpoint == null) {
            LOG.info("HttpTours requires a tourEndpoint property to be configured");
            return EMPTY_MANIFEST;
        }
        Response response = null;
        try {
            String tourManifestUrl = constructTourManifestUrl();
            Request rq = newRq().url(tourManifestUrl).build();
            LOG.debug("Loading tour manifest: " + tourManifestUrl);
            response = client.newCall(rq).execute();
            if(response.isSuccessful()) {
                return mapper.readValue(response.body().byteStream(), TreeMap.class);
            } else {
                LOG.error("Unable to load tour manifest at: " + (tourManifestUrl));
                LOG.error("Message from tour manifest endpoint: " + response.body().string());
            }

        } catch(Exception ex) {
            LOG.error("Failed to load tour manifest.", ex);
        } finally {
            if(response != null) response.body().close();
        }
        return EMPTY_MANIFEST;
    }

    private String constructTourManifestUrl() {
        StringBuilder tourUrl = new StringBuilder(tourEndpoint);
        if(!tourEndpoint.endsWith("/")) tourUrl.append("/");
        tourUrl.append(tourManifestName);
        return tourUrl.toString();
    }

    @Override
    public Map getTour(final String tourId) {
        Response response = null;
        try {
            String tourKey = tourId.endsWith(".json") ? tourId : tourId+".json";
            String tourUrl = constructTourUrl(tourKey);
            LOG.debug(tourUrl);
            Request rq = newRq().url(tourUrl).build();
            response = client.newCall(rq).execute();
            if(response.isSuccessful()) {
                return mapper.readValue(response.body().byteStream(),TreeMap.class);
            } else {
                LOG.error("Unable to load tour at: " + tourUrl);
                LOG.error("Message from tour endpoint: " + response.body().string());
            }

        } catch(Exception ex) {
            LOG.error("Unable to load tour",ex);
        } finally {
            if(response != null) response.body().close();
        }
        return new TreeMap();
    }

    private String constructTourUrl(final String tourKey) {
        StringBuilder tourUrl = new StringBuilder(tourEndpoint);
        if(!tourEndpoint.endsWith("/") && !toursSubpath.startsWith("/")) tourUrl.append("/");
        tourUrl.append(toursSubpath);
        tourUrl.append("/");
        tourUrl.append(tourKey);
        return tourUrl.toString();
    }

    private Request.Builder newRq() {
        return new Request.Builder()
                .method("GET", null);
    }
}