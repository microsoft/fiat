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

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import retrofit.RestAdapter;

import javax.validation.constraints.NotNull;

@Data
@ConditionalOnProperty(value = "auth.groupMembership.service", havingValue = "azureAD")
@Configuration
@ConfigurationProperties("auth.groupMembership.azureAD")
public class AzureProperties {

    @NotEmpty
    public String clientID;
    @NotEmpty
    public String appKey;
    @NotEmpty
    public String tenantID;
    @NotEmpty
    public String serviceHost;
    @NotEmpty
    public String organization;
    @NotEmpty
    public String subscriptionID;

}
