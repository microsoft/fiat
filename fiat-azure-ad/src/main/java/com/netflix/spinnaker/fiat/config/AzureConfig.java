/*
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.fiat.config;


import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.credentials.TokenCredentialsInterceptor;
import com.netflix.spinnaker.fiat.roles.azure.client.AzureADClient;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;
import retrofit.Endpoints;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(value = "auth.groupMembership.service", havingValue = "azureAD")
@Slf4j
public class AzureConfig {
    @Autowired
    @Setter
    private OkClient okClient;

    @Autowired
    @Setter
    private RestAdapter.LogLevel retrofitLogLevel;

    @Autowired
    @Setter
    private AzureProperties azureProperties;

    @Bean
    public AzureADClient azureADClient() {
        try {
            ApplicationTokenCredentials tokenCredentials = new ApplicationTokenCredentials(azureProperties.clientID,
                    azureProperties.tenantID, azureProperties.appKey, AzureEnvironment.AZURE);
            Azure azure = Azure.authenticate(tokenCredentials).withSubscription(azureProperties.subscriptionID);
            //TokenCredentialsInterceptor interceptor = new TokenCredentialsInterceptor(tokenCredentials);
            BasicAuthRequestInterceptor interceptor = new BasicAuthRequestInterceptor().setAccessToken(tokenCredentials.getToken());

            return new RestAdapter.Builder()
                    .setEndpoint(Endpoints.newFixedEndpoint(azureProperties.serviceHost))
                    .setRequestInterceptor(interceptor)
                    .setClient(okClient)
                    .setConverter(new JacksonConverter())
                    .setLogLevel(retrofitLogLevel)
                    .setLog(new Slf4jRetrofitLogger(AzureADClient.class))
                    .build()
                    .create(AzureADClient.class);
        } catch (IOException ioe) {
            //log the exception
        }
        return null;
    }

    private static class Slf4jRetrofitLogger implements RestAdapter.Log {
        private final Logger logger;

        Slf4jRetrofitLogger(Class type) {
            this(LoggerFactory.getLogger(type));
        }

        Slf4jRetrofitLogger(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void log(String message) {
            logger.info(message);
        }
    }

    private static class BasicAuthRequestInterceptor implements RequestInterceptor {

        @Setter
        private String accessToken;

        @Override
        public void intercept(RequestFacade request) {
            // Although the encoded string is normally "user:password" for Basic auth, GitHub appears to
            // only look at the value to the right of the colon, or the whole value if no colon exists.
            // This is nice for us, because we don't necessarily have a username to pass along anyway.
            String encoded = Base64Utils.encodeToString(accessToken.getBytes(StandardCharsets.UTF_8));
            request.addHeader("Authorization", encoded);
        }
    }
}
