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

package org.openqa.grid.web.servlet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import org.openqa.grid.graphql.GridData;
import org.openqa.grid.graphql.SessionData;
import org.openqa.grid.graphql.Types;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.remote.http.Contents;
import org.openqa.grid.remote.http.HttpResponse;
import org.openqa.grid.server.JeeInterop;
import org.openqa.selenium.json.Json;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphqlServlet extends RegistryBasedServlet {
  public static final String GRID_SCHEMA = "/org/openqa/grid/graphql/selenium-grid-schema.graphqls";
  public static final Json JSON = new Json();
  private GraphQL graphQl;

  public GraphqlServlet() {
    this(null);
  }

  public GraphqlServlet(GridRegistry registry) {
    super(registry);
  }

  @Override
  public void init() {
    GraphQLSchema schema = new SchemaGenerator()
      .makeExecutableSchema(buildTypeDefinitionRegistry(), buildRuntimeWiring());

    Cache<String, PreparsedDocumentEntry> cache = CacheBuilder.newBuilder()
      .maximumSize(1024)
      .build();

    graphQl = GraphQL.newGraphQL(schema)
      .preparsedDocumentProvider((executionInput, computeFunction) -> {
        try {
          return cache.get(executionInput.getQuery(), () -> computeFunction.apply(executionInput));
        } catch (ExecutionException e) {
          if (e.getCause() instanceof RuntimeException) {
            throw (RuntimeException) e.getCause();
          } else if (e.getCause() != null) {
            throw new RuntimeException(e.getCause());
          }
          throw new RuntimeException(e);
        }
      })
      .build();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    process(request, response);
  }

  protected void process(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Map<String, Object> inputs = JSON.toType(Contents.string(JeeInterop.toHttpRequest(request)), Json.MAP_TYPE);
    if (!(inputs.get("query") instanceof String)) {
        JeeInterop.copyResponse(
          new HttpResponse()
            .setStatus(HTTP_INTERNAL_ERROR)
            .setContent(Contents.utf8String("Unable to find query")),
          response);
    }

    String query = (String) inputs.get("query");
    @SuppressWarnings("unchecked") Map<String, Object> variables = inputs.get("variables") instanceof Map ?
      (Map<String, Object>) inputs.get("variables") :
      new HashMap<>();

    ExecutionInput executionInput = ExecutionInput.newExecutionInput(query)
      .variables(variables)
      .build();

    ExecutionResult result = graphQl.execute(executionInput);

    if (result.isDataPresent()) {
      JeeInterop.copyResponse(
        new HttpResponse()
          .addHeader("Content-Type", Json.JSON_UTF_8)
          .setContent(Contents.utf8String(JSON.toJson(result.toSpecification()))),
        response);
    }

    JeeInterop.copyResponse(
      new HttpResponse()
        .setStatus(HTTP_INTERNAL_ERROR)
        .setContent(Contents.utf8String(JSON.toJson(result.getErrors()))),
      response);
  }

  private RuntimeWiring buildRuntimeWiring() {
    GridData gridData = new GridData(getRegistry());
    return RuntimeWiring.newRuntimeWiring()
      .scalar(Types.Uri)
      .scalar(Types.Url)
      .type("GridQuery", typeWiring -> typeWiring
        .dataFetcher("grid", gridData)
        .dataFetcher("sessionsInfo", gridData)
        // .dataFetcher("nodesInfo", gridData)
        .dataFetcher("session", new SessionData(getRegistry())))
      .build();
  }

  private TypeDefinitionRegistry buildTypeDefinitionRegistry() {
    try (InputStream stream = getClass().getResourceAsStream(GRID_SCHEMA)) {
      return new SchemaParser().parse(stream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
