/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.guice.jetty_exporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.onebusaway.guice.jsr250.JSR250Module;
import org.onebusaway.guice.jsr250.LifecycleService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class IntegrationTest {

  @Test
  public void test() throws IOException {
    Set<Module> modules = new HashSet<Module>();
    JettyExporterModule.addModuleAndDependencies(modules);
    JSR250Module.addModuleAndDependencies(modules);
    Injector injector = Guice.createInjector(modules);
    injector.getInstance(MyServlet.class);
    LifecycleService lifecycleService = injector.getInstance(LifecycleService.class);
    lifecycleService.start();

    URL url = getUrl();
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        url.openStream()));
    String line = reader.readLine();
    assertEquals("Hello world!", line);
    reader.close();

    lifecycleService.stop();

    try {
      url.openStream();
      fail();
    } catch (IOException ex) {

    }
  }

  private static URL getUrl() {
    try {
      String port = System.getProperty("org_onebusaway_test_port", "8080");
      return new URL("http://localhost:" + port + "/my-servlet");
    } catch (MalformedURLException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static class MyServlet extends HttpServlet implements ServletSource {

    private static final long serialVersionUID = 1L;

    @Override
    public URL getUrl() {
      return IntegrationTest.getUrl();
    }

    @Override
    public Servlet getServlet() {
      return this;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      PrintWriter writer = resp.getWriter();
      writer.println("Hello world!");
    }

  }
}
