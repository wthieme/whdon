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
 * on 2017-03-25 at 21:15:26 UTC 
 * Modify at your own risk.
 */

package nl.whitedove.washetdroogofniet.backend.whdonApi.model;

/**
 * Model definition for MeldingCollection.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the whdonApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MeldingCollection extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Melding> items;

  static {
    // hack to force ProGuard to consider Melding used, since otherwise it would be stripped out
    // see https://github.com/google/google-api-java-client/issues/543
    com.google.api.client.util.Data.nullOf(Melding.class);
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<Melding> getItems() {
    return items;
  }

  /**
   * @param items items or {@code null} for none
   */
  public MeldingCollection setItems(java.util.List<Melding> items) {
    this.items = items;
    return this;
  }

  @Override
  public MeldingCollection set(String fieldName, Object value) {
    return (MeldingCollection) super.set(fieldName, value);
  }

  @Override
  public MeldingCollection clone() {
    return (MeldingCollection) super.clone();
  }

}
