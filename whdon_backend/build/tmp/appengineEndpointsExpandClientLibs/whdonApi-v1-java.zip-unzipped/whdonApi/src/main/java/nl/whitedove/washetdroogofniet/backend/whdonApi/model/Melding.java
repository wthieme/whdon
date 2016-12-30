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
 * (build: 2016-10-17 16:43:55 UTC)
 * on 2016-12-09 at 19:38:19 UTC 
 * Modify at your own risk.
 */

package nl.whitedove.washetdroogofniet.backend.whdonApi.model;

/**
 * Model definition for Melding.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the whdonApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Melding extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long datum;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean droog;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String error;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String locatie;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean nat;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getDatum() {
    return datum;
  }

  /**
   * @param datum datum or {@code null} for none
   */
  public Melding setDatum(java.lang.Long datum) {
    this.datum = datum;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getDroog() {
    return droog;
  }

  /**
   * @param droog droog or {@code null} for none
   */
  public Melding setDroog(java.lang.Boolean droog) {
    this.droog = droog;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getError() {
    return error;
  }

  /**
   * @param error error or {@code null} for none
   */
  public Melding setError(java.lang.String error) {
    this.error = error;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public Melding setId(java.lang.String id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getLocatie() {
    return locatie;
  }

  /**
   * @param locatie locatie or {@code null} for none
   */
  public Melding setLocatie(java.lang.String locatie) {
    this.locatie = locatie;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getNat() {
    return nat;
  }

  /**
   * @param nat nat or {@code null} for none
   */
  public Melding setNat(java.lang.Boolean nat) {
    this.nat = nat;
    return this;
  }

  @Override
  public Melding set(String fieldName, Object value) {
    return (Melding) super.set(fieldName, value);
  }

  @Override
  public Melding clone() {
    return (Melding) super.clone();
  }

}
