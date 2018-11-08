/*
 * Copyright 2018 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plugin.httptours;

import com.dtolabs.rundeck.core.Constants;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AgentInterceptor implements Interceptor {
    Logger LOG = LoggerFactory.getLogger(AgentInterceptor.class);
    public final String ua;

    public AgentInterceptor() {
        Properties frameworkProps = new Properties();
        try {
            frameworkProps.load(new FileInputStream(new File(Constants.getFrameworkConfigDir(),
                                                             "framework.properties")));
        } catch(Exception ex) {
            LOG.error("Unable to load framework properties",ex);
        }

        String src = frameworkProps.getProperty("framework.server.url","na");
        ua = String.format("httptours/%s (%s)",System.getProperty("rd.app.ident","unk"),src);
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request rq = chain.request()
                    .newBuilder()
                    .addHeader("User-Agent", ua)
                    .build();

        return chain.proceed(rq);
    }
}
