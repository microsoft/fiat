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

package com.netflix.spinnaker.fiat.roles.azure;


import com.netflix.spinnaker.fiat.config.AzureProperties;
import com.netflix.spinnaker.fiat.model.resources.Role;
import com.netflix.spinnaker.fiat.roles.UserRolesProvider;
import com.netflix.spinnaker.fiat.roles.azure.client.AzureADClient;
import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import retrofit.client.Response;

import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(value = "auth.groupMembership.service", havingValue = "azureAD")
public class AzureADUserRolesProvider implements UserRolesProvider, InitializingBean {

    @Autowired
    @Setter
    private AzureADClient azureADClient;

    @Autowired
    @Setter
    AzureProperties azureProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(azureProperties.clientID != null, "Client ID must be supplied");
        Assert.state(azureProperties.appKey != null, "Client ID must be supplied");
        Assert.state(azureProperties.tenantID != null, "Tenant ID must be supplied");
        Assert.state(azureProperties.serviceHost != null, "Provider Domain must be supplied");
        Assert.state(azureProperties.organization != null, "Organization must be supplied");
        Assert.state(azureProperties.organization != null, "Subscription ID must be supplied");
    }

    @Override
    public List<Role> loadRoles(String userId) {
        List<Role> result = new ArrayList<>();
        log.debug("loadRoles for user " + userId);
        // if all the appropriate items are set then
        if (!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(azureProperties.serviceHost) && !StringUtils.isEmpty(azureProperties.organization)) {
            Response response = azureADClient.getMemberGroups(azureProperties.organization, userId);
            if (response != null) {
                // parse the response
            }
        }

        return result;
    }

    @Override
    public Map<String, Collection<Role>> multiLoadRoles(Collection<String> userIds) {
        HashMap<String, Collection<Role>> userGroups = new HashMap<String, Collection<Role>>();
        if (userIds != null && !userIds.isEmpty()) {
            userIds.forEach(user -> userGroups.put(user, loadRoles(user)));
        }

        return userGroups;
    }

}
