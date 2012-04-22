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
