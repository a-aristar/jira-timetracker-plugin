/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.everit.jira.updatenotifier.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DTO for the Jira JSON format used in the marketplace REST interface.
 */
public class JiraMarketplaceJSONDTO {

  @SerializedName("_embedded")
  @Expose
  private Embedded embedded;

  public Embedded getEmbedded() {
    return embedded;
  }

  public void setEmbedded(final Embedded embedded) {
    this.embedded = embedded;
  }

}
