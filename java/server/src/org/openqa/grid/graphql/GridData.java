// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.grid.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.internal.Require;
// import org.openqa.selenium.grid.distributor.Distributor;
// import org.openqa.selenium.grid.sessionqueue.NewSessionQueue;
import org.openqa.selenium.BuildInfo;

import java.net.URI;

public class GridData implements DataFetcher<Grid> {
  // private final Distributor distributor;
  // private final NewSessionQueue newSessionQueue;
  // private final URI publicUri;
  // private final String version;
  private final GridRegistry registry;
  private static final BuildInfo buildInfo = new BuildInfo();

  public GridData(
    // Distributor distributor,
    // NewSessionQueue newSessionQueue,
    // URI publicUri,
    // String version
    GridRegistry registry
  ) {
    // this.distributor = Require.nonNull("Distributor", distributor);
    // this.publicUri = Require.nonNull("Grid's public URI", publicUri);
    // this.newSessionQueue = Require.nonNull("New session queue", newSessionQueue);
    // this.version = Require.nonNull("Grid's version", version);
    this.registry = Require.nonNull("Registry", registry);
  }

  @Override
  public Grid get(DataFetchingEnvironment environment) {
    // return new Grid(distributor, newSessionQueue, publicUri, version);
    return new Grid(registry, version());
  }

  private String version() {
    return String.format(
        "Selenium server version: %s, revision: %s",
        buildInfo.getReleaseLabel(),
        buildInfo.getBuildRevision());
  }
}
