/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2017-02-15 17:18:02 UTC)
 * on 2017-06-17 at 10:44:19 UTC 
 * Modify at your own risk.
 */

package nl.whitedove.washetdroogofniet.backend.whdonApi;

/**
 * Service definition for WhdonApi (v1).
 *
 * <p>
 * This is an API
 * </p>
 *
 * <p>
 * For more information about this service, see the
 * <a href="" target="_blank">API Documentation</a>
 * </p>
 *
 * <p>
 * This service uses {@link WhdonApiRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class WhdonApi extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.22.0 of the whdonApi library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
  }

  /**
   * The default encoded root URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_ROOT_URL = "https://washetdroogofnietbackend.appspot.com/_ah/api/";

  /**
   * The default encoded service path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_SERVICE_PATH = "whdonApi/v1/";

  /**
   * The default encoded base URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   */
  public static final String DEFAULT_BASE_URL = DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH;

  /**
   * Constructor.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport HTTP transport, which should normally be:
   *        <ul>
   *        <li>Google App Engine:
   *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
   *        <li>Android: {@code newCompatibleTransport} from
   *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
   *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
   *        </li>
   *        </ul>
   * @param jsonFactory JSON factory, which may be:
   *        <ul>
   *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
   *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
   *        <li>Android Honeycomb or higher:
   *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
   *        </ul>
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @since 1.7
   */
  public WhdonApi(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
      com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  /**
   * @param builder builder
   */
  WhdonApi(Builder builder) {
    super(builder);
  }

  @Override
  protected void initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * Create a request for the method "getAlleMeldingenVanaf".
   *
   * This request holds the parameters needed by the whdonApi server.  After setting any optional
   * parameters, call the {@link GetAlleMeldingenVanaf#execute()} method to invoke the remote
   * operation.
   *
   * @param datum
   * @return the request
   */
  public GetAlleMeldingenVanaf getAlleMeldingenVanaf(java.lang.Long datum) throws java.io.IOException {
    GetAlleMeldingenVanaf result = new GetAlleMeldingenVanaf(datum);
    initialize(result);
    return result;
  }

  public class GetAlleMeldingenVanaf extends WhdonApiRequest<nl.whitedove.washetdroogofniet.backend.whdonApi.model.MeldingCollection> {

    private static final String REST_PATH = "GetAlleMeldingenVanaf/{datum}";

    /**
     * Create a request for the method "getAlleMeldingenVanaf".
     *
     * This request holds the parameters needed by the the whdonApi server.  After setting any
     * optional parameters, call the {@link GetAlleMeldingenVanaf#execute()} method to invoke the
     * remote operation. <p> {@link GetAlleMeldingenVanaf#initialize(com.google.api.client.googleapis.
     * services.AbstractGoogleClientRequest)} must be called to initialize this instance immediately
     * after invoking the constructor. </p>
     *
     * @param datum
     * @since 1.13
     */
    protected GetAlleMeldingenVanaf(java.lang.Long datum) {
      super(WhdonApi.this, "POST", REST_PATH, null, nl.whitedove.washetdroogofniet.backend.whdonApi.model.MeldingCollection.class);
      this.datum = com.google.api.client.util.Preconditions.checkNotNull(datum, "Required parameter datum must be specified.");
    }

    @Override
    public GetAlleMeldingenVanaf setAlt(java.lang.String alt) {
      return (GetAlleMeldingenVanaf) super.setAlt(alt);
    }

    @Override
    public GetAlleMeldingenVanaf setFields(java.lang.String fields) {
      return (GetAlleMeldingenVanaf) super.setFields(fields);
    }

    @Override
    public GetAlleMeldingenVanaf setKey(java.lang.String key) {
      return (GetAlleMeldingenVanaf) super.setKey(key);
    }

    @Override
    public GetAlleMeldingenVanaf setOauthToken(java.lang.String oauthToken) {
      return (GetAlleMeldingenVanaf) super.setOauthToken(oauthToken);
    }

    @Override
    public GetAlleMeldingenVanaf setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (GetAlleMeldingenVanaf) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public GetAlleMeldingenVanaf setQuotaUser(java.lang.String quotaUser) {
      return (GetAlleMeldingenVanaf) super.setQuotaUser(quotaUser);
    }

    @Override
    public GetAlleMeldingenVanaf setUserIp(java.lang.String userIp) {
      return (GetAlleMeldingenVanaf) super.setUserIp(userIp);
    }

    @com.google.api.client.util.Key
    private java.lang.Long datum;

    /**

     */
    public java.lang.Long getDatum() {
      return datum;
    }

    public GetAlleMeldingenVanaf setDatum(java.lang.Long datum) {
      this.datum = datum;
      return this;
    }

    @Override
    public GetAlleMeldingenVanaf set(String parameterName, Object value) {
      return (GetAlleMeldingenVanaf) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "getVersie".
   *
   * This request holds the parameters needed by the whdonApi server.  After setting any optional
   * parameters, call the {@link GetVersie#execute()} method to invoke the remote operation.
   *
   * @return the request
   */
  public GetVersie getVersie() throws java.io.IOException {
    GetVersie result = new GetVersie();
    initialize(result);
    return result;
  }

  public class GetVersie extends WhdonApiRequest<nl.whitedove.washetdroogofniet.backend.whdonApi.model.Versie> {

    private static final String REST_PATH = "GetVersie";

    /**
     * Create a request for the method "getVersie".
     *
     * This request holds the parameters needed by the the whdonApi server.  After setting any
     * optional parameters, call the {@link GetVersie#execute()} method to invoke the remote
     * operation. <p> {@link
     * GetVersie#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
     * must be called to initialize this instance immediately after invoking the constructor. </p>
     *
     * @since 1.13
     */
    protected GetVersie() {
      super(WhdonApi.this, "POST", REST_PATH, null, nl.whitedove.washetdroogofniet.backend.whdonApi.model.Versie.class);
    }

    @Override
    public GetVersie setAlt(java.lang.String alt) {
      return (GetVersie) super.setAlt(alt);
    }

    @Override
    public GetVersie setFields(java.lang.String fields) {
      return (GetVersie) super.setFields(fields);
    }

    @Override
    public GetVersie setKey(java.lang.String key) {
      return (GetVersie) super.setKey(key);
    }

    @Override
    public GetVersie setOauthToken(java.lang.String oauthToken) {
      return (GetVersie) super.setOauthToken(oauthToken);
    }

    @Override
    public GetVersie setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (GetVersie) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public GetVersie setQuotaUser(java.lang.String quotaUser) {
      return (GetVersie) super.setQuotaUser(quotaUser);
    }

    @Override
    public GetVersie setUserIp(java.lang.String userIp) {
      return (GetVersie) super.setUserIp(userIp);
    }

    @Override
    public GetVersie set(String parameterName, Object value) {
      return (GetVersie) super.set(parameterName, value);
    }
  }

  /**
   * Create a request for the method "meldingOpslaan".
   *
   * This request holds the parameters needed by the whdonApi server.  After setting any optional
   * parameters, call the {@link MeldingOpslaan#execute()} method to invoke the remote operation.
   *
   * @param content the {@link nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding}
   * @return the request
   */
  public MeldingOpslaan meldingOpslaan(nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding content) throws java.io.IOException {
    MeldingOpslaan result = new MeldingOpslaan(content);
    initialize(result);
    return result;
  }

  public class MeldingOpslaan extends WhdonApiRequest<nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding> {

    private static final String REST_PATH = "MeldingOpslaan";

    /**
     * Create a request for the method "meldingOpslaan".
     *
     * This request holds the parameters needed by the the whdonApi server.  After setting any
     * optional parameters, call the {@link MeldingOpslaan#execute()} method to invoke the remote
     * operation. <p> {@link MeldingOpslaan#initialize(com.google.api.client.googleapis.services.Abstr
     * actGoogleClientRequest)} must be called to initialize this instance immediately after invoking
     * the constructor. </p>
     *
     * @param content the {@link nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding}
     * @since 1.13
     */
    protected MeldingOpslaan(nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding content) {
      super(WhdonApi.this, "POST", REST_PATH, content, nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding.class);
    }

    @Override
    public MeldingOpslaan setAlt(java.lang.String alt) {
      return (MeldingOpslaan) super.setAlt(alt);
    }

    @Override
    public MeldingOpslaan setFields(java.lang.String fields) {
      return (MeldingOpslaan) super.setFields(fields);
    }

    @Override
    public MeldingOpslaan setKey(java.lang.String key) {
      return (MeldingOpslaan) super.setKey(key);
    }

    @Override
    public MeldingOpslaan setOauthToken(java.lang.String oauthToken) {
      return (MeldingOpslaan) super.setOauthToken(oauthToken);
    }

    @Override
    public MeldingOpslaan setPrettyPrint(java.lang.Boolean prettyPrint) {
      return (MeldingOpslaan) super.setPrettyPrint(prettyPrint);
    }

    @Override
    public MeldingOpslaan setQuotaUser(java.lang.String quotaUser) {
      return (MeldingOpslaan) super.setQuotaUser(quotaUser);
    }

    @Override
    public MeldingOpslaan setUserIp(java.lang.String userIp) {
      return (MeldingOpslaan) super.setUserIp(userIp);
    }

    @Override
    public MeldingOpslaan set(String parameterName, Object value) {
      return (MeldingOpslaan) super.set(parameterName, value);
    }
  }

  /**
   * Builder for {@link WhdonApi}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.3.0
   */
  public static final class Builder extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder {

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport, which should normally be:
     *        <ul>
     *        <li>Google App Engine:
     *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
     *        <li>Android: {@code newCompatibleTransport} from
     *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
     *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
     *        </li>
     *        </ul>
     * @param jsonFactory JSON factory, which may be:
     *        <ul>
     *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
     *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
     *        <li>Android Honeycomb or higher:
     *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
     *        </ul>
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @since 1.7
     */
    public Builder(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
        com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      super(
          transport,
          jsonFactory,
          DEFAULT_ROOT_URL,
          DEFAULT_SERVICE_PATH,
          httpRequestInitializer,
          false);
    }

    /** Builds a new instance of {@link WhdonApi}. */
    @Override
    public WhdonApi build() {
      return new WhdonApi(this);
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }

    /**
     * Set the {@link WhdonApiRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setWhdonApiRequestInitializer(
        WhdonApiRequestInitializer whdonapiRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(whdonapiRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        com.google.api.client.googleapis.services.GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}
