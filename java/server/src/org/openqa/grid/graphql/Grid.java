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

// import com.google.common.collect.ImmutableList;
import org.openqa.grid.graphql.Session;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.Require;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.TestSlot;
import org.openqa.selenium.ImmutableCapabilities;
// import org.openqa.selenium.Capabilities;
// import org.openqa.selenium.ImmutableCapabilities;
// import org.openqa.selenium.grid.data.DistributorStatus;
// import org.openqa.selenium.grid.data.NodeStatus;
// import org.openqa.selenium.grid.data.SessionRequestCapability;
// import org.openqa.selenium.grid.data.Slot;
// import org.openqa.selenium.grid.distributor.Distributor;
// import org.openqa.selenium.grid.sessionqueue.NewSessionQueue;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
// import org.openqa.selenium.remote.SessionId;

// import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
// import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Grid {

  // private static final SessionId RESERVED = new SessionId("reserved");
  // private final URI uri;
  // private final DistributorStatus distributorStatus;
  // private final List<Set<Capabilities>> queueInfoList;
  private final GridRegistry registry;
  private final String version;
  private static final Json JSON = new Json();

  public Grid(
    // Distributor distributor,
    // NewSessionQueue newSessionQueue,
    // URI uri,
    GridRegistry registry,
    String version
  ) {
    // Require.nonNull("Distributor", distributor);
    // this.uri = Require.nonNull("Grid's public URI", uri);
    // NewSessionQueue sessionQueue = Require.nonNull("New session queue", newSessionQueue);
    // this.queueInfoList = sessionQueue
    //   .getQueueContents()
    //   .stream()
    //   .map(SessionRequestCapability::getDesiredCapabilities)
    //   .collect(Collectors.toList());
    // this.distributorStatus = distributor.getStatus();

    this.registry = Require.nonNull("Registry", registry);
    this.version = Require.nonNull("Grid's version", version);
  }

  // public URI getUri() {
  //   return uri;
  // }

  public String getVersion() {
    return version;
  }

  // public List<Node> getNodes() {
  //   ImmutableList.Builder<Node> toReturn = ImmutableList.builder();

  //   for (NodeStatus status : distributorStatus.getNodes()) {
  //     Map<Capabilities, Integer> stereotypes = new HashMap<>();
  //     Map<org.openqa.selenium.grid.data.Session, Slot> sessions = new HashMap<>();

  //     for (Slot slot : status.getSlots()) {
  //       org.openqa.selenium.grid.data.Session session = slot.getSession();
  //       if (session != null) {
  //         sessions.put(session, slot);
  //       }

  //       int count = stereotypes.getOrDefault(slot.getStereotype(), 0);
  //       count++;
  //       stereotypes.put(slot.getStereotype(), count);
  //     }

  //     OsInfo osInfo = new OsInfo(
  //       status.getOsInfo().get("arch"),
  //       status.getOsInfo().get("name"),
  //       status.getOsInfo().get("version"));

  //     toReturn.add(new Node(
  //       status.getNodeId(),
  //       status.getExternalUri(),
  //       status.getAvailability(),
  //       status.getMaxSessionCount(),
  //       status.getSlots().size(),
  //       stereotypes,
  //       sessions,
  //       status.getVersion(),
  //       osInfo));
  //   }

  //   return toReturn.build();
  // }

  // public int getSessionCount() {
  //   return distributorStatus.getNodes().stream()
  //     .map(NodeStatus::getSlots)
  //     .flatMap(Collection::stream)
  //     .filter(slot -> slot.getSession()!=null)
  //     .filter(slot -> !slot.getSession().getId().equals(RESERVED))
  //     .mapToInt(slot -> 1)
  //     .sum();
  // }

  // public int getTotalSlots() {
  //   return distributorStatus.getNodes().stream()
  //     .mapToInt(status -> status.getSlots().size())
  //     .sum();
  // }

  // public int getMaxSession() {
  //   return distributorStatus.getNodes().stream()
  //     .mapToInt(NodeStatus::getMaxSessionCount)
  //     .sum();
  // }

  // public int getSessionQueueSize() {
  //   return queueInfoList.size();
  // }

  public List<String> getSessionQueueRequests() {
    return StreamSupport.stream(registry.getDesiredCapabilities().spliterator(), false)
      .map((DesiredCapabilities capabilities) -> {
        capabilities.setCapability(CapabilityType.BROWSER_VERSION, "latest");
        return capabilities;
      })
      .map(JSON::toJson)
      .collect(Collectors.toList());
  }

  public List<Session> getSessions()
      throws URISyntaxException {
    List<Session> sessions = new ArrayList<>();
    // for (NodeStatus status : distributorStatus.getNodes()) {
    //   for (Slot slot : status.getSlots()) {
    //     if (slot.getSession() != null && !slot.getSession().getId().equals(RESERVED)) {
    //       org.openqa.selenium.grid.data.Session session = slot.getSession();
    //       sessions.add(
    //         new org.openqa.grid.graphql.Session(
    //           session.getId().toString(),
    //           session.getCapabilities(),
    //           session.getStartTime(),
    //           session.getUri(),
    //           status.getNodeId().toString(),
    //           status.getExternalUri(),
    //           slot)
    //       );
    //     }
    //   }
    // }

    for (RemoteProxy proxy : getActiveNodes()) {
      for (TestSlot slot : proxy.getTestSlots()) {
        // if (slot.getSession() != null && !slot.getSession().getId().equals(RESERVED)) {
        if (slot.getSession() != null) {
          TestSession session = slot.getSession();
          Map<String, Object> capabilities = new HashMap<>(slot.getCapabilities());
          capabilities.put(CapabilityType.BROWSER_VERSION, "latest");
          sessions.add(
            new Session(
              session.getExternalKey().toString(),
              new ImmutableCapabilities(capabilities),
              // session.getStartTime(),
              Instant.ofEpochMilli(slot.getLastSessionStart()),

              // session.getUri(),
              slot.getRemoteURL().toURI(),

              // status.getNodeId().toString(),
              proxy.getId(),

              // status.getExternalUri(),
              proxy.getRemoteHost().toURI(),
              slot)
          );
        }
      }
    }

    return sessions;
  }

  public int getNodeCount() {
    return getActiveNodes().size();
  }

  public int getMaxSession() {
    int totalSlots = 0;
    for (RemoteProxy proxy : getActiveNodes()) {
      totalSlots += Math.min(proxy.getMaxNumberOfConcurrentTestSessions(), proxy.getTestSlots().size());
    }

    return totalSlots;
  }

  private List<RemoteProxy> getActiveNodes() {
    return registry.getAllProxies().getSorted()
      .parallelStream().filter(remoteProxy -> {
        // Only counting proxies that reply at their status endpoint
        try {
          remoteProxy.getProxyStatus();
          return true;
        } catch (Exception e) {
          return false;
        }
      }).collect(Collectors.toList());
  }
}














// package org.openqa.grid.web.servlet;

// import static com.google.common.net.MediaType.JSON_UTF_8;
// import static java.net.HttpURLConnection.HTTP_OK;
// import static java.nio.charset.StandardCharsets.UTF_8;
// import static org.openqa.selenium.remote.ErrorCodes.SUCCESS;

// import com.google.common.collect.ImmutableMap;

// import org.openqa.grid.internal.GridRegistry;

// import org.openqa.selenium.BuildInfo;
// import org.openqa.selenium.json.Json;

// import java.io.IOException;

// import java.util.Map;
// import java.util.Objects;


// import javax.servlet.ServletOutputStream;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// /**
//  * Responds to the WebDriver status request to indicate whether or not the hub is ready to respond.
//  */
// public class HubW3CStatusServlet extends HttpServlet {

//   private final GridRegistry registry;

//   public HubW3CStatusServlet(GridRegistry registry) {
//     this.registry = Objects.requireNonNull(registry);
//   }

//   @Override
//   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
//       throws IOException {
//     List<RemoteProxy> allProxies = registry.getAllProxies().getSorted()
//         .parallelStream().filter(remoteProxy -> {
//           // Only counting proxies that reply at their status endpoint
//           try {
//             remoteProxy.getProxyStatus();
//             return true;
//           } catch (Exception e) {
//             return false;
//           }
//         }).collect(Collectors.toList());
//     List<RemoteProxy> busyProxies = allProxies.parallelStream()
//         .filter(proxy -> proxy.getMaxNumberOfConcurrentTestSessions() - proxy.getTotalUsed() <= 0)
//         .collect(Collectors.toList());

//     ImmutableMap.Builder<String, Object> value = ImmutableMap.builder();

//     // W3C spec
//     boolean availableProxies = allProxies.size() > busyProxies.size();
//     value.put("ready", availableProxies);
//     value.put("message", availableProxies ? "Hub has capacity" : "No spare hub capacity" );

//     BuildInfo buildInfo = new BuildInfo();
//     value.put("build", ImmutableMap.of(
//         // We need to fix the BuildInfo to properly fill out these values.
//         "revision", buildInfo.getBuildRevision(),
//         "time", buildInfo.getBuildTime(),
//         "version", buildInfo.getReleaseLabel()));

//     value.put("os", ImmutableMap.of(
//         "arch", System.getProperty("os.arch"),
//         "name", System.getProperty("os.name"),
//         "version", System.getProperty("os.version")));

//     value.put("java", ImmutableMap.of("version", System.getProperty("java.version")));

//     Map<String, Object> payloadObj = ImmutableMap.of(
//         "status", SUCCESS,
//         "value", value.build());

//     // Write out a minimal W3C status response.
//     byte[] payload = new Json().toJson(payloadObj).getBytes(UTF_8);

//     resp.setStatus(HTTP_OK);
//     resp.setHeader("Content-Type", JSON_UTF_8.toString());
//     resp.setHeader("Content-Length", String.valueOf(payload.length));

//     try (ServletOutputStream out = resp.getOutputStream()) {
//       out.write(payload);
//     }
//   }
// }
