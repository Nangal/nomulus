// Copyright 2016 The Nomulus Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.module.tools;

import static java.util.Arrays.asList;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import google.registry.request.RequestHandler;
import google.registry.request.RequestModule;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Security;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/** Servlet that should handle all requests to our "tools" App Engine module. */
public final class ToolsServlet extends HttpServlet {

  private static final ToolsComponent component = DaggerToolsComponent.create();

  private static final RequestHandler<ToolsRequestComponent> requestHandler =
      RequestHandler.create(ToolsRequestComponent.class, FluentIterable
          .from(asList(ToolsRequestComponent.class.getMethods()))
          .transform(new Function<Method, Method>() {
            @Override
            public Method apply(Method method) {
              method.setAccessible(true);  // Make App Engine's security manager happy.
              return method;
            }}));

  @Override
  public void init() {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
    requestHandler.handleRequest(req, rsp, component.startRequest(new RequestModule(req, rsp)));
  }
}
