/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling.AdvancedThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling.ApplicationThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling.BlockingConditionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling.GlobalThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.throttling.SubscriptionThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * This is the service implementation class for Admin Portal Throttling related operations
 */
public class ThrottlingApiServiceImpl extends ThrottlingApiService {

    private static final Log log = LogFactory.getLog(ThrottlingApiServiceImpl.class);

    /**
     * Retrieves all Advanced level policies
     *
     * @param limit           maximum number of policies to return
     * @param offset          starting index
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return All matched Advanced Throttle policies to the given request
     */
    @Override
    public Response throttlingPoliciesAdvancedPoliciesGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy[] apiPolicies = (APIPolicy[]) apiProvider.getPolicies(userName, PolicyConstants.POLICY_LEVEL_API);
            AdvancedThrottlePolicyListDTO listDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAPIPolicyArrayToListDTO(apiPolicies);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Advanced level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Add an Advanced Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPost(AdvancedThrottlePolicyDTO body, String contentType) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiProvider.addPolicy(apiPolicy);

            //retrieve the new policy and send back as the response
            APIPolicy newApiPolicy = apiProvider.getAPIPolicy(userName, body.getPolicyName());
            AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAdvancedPolicyToDTO(newApiPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_ADVANCED + "/" + policyDTO
                            .getPolicyId())).entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding an Advanced level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Advanced Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get a specific Advanced Level Policy
     *
     * @param policyId      uuid of the policy
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Required policy specified by name
     */
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIPolicy apiPolicy = apiProvider.getAPIPolicyByUUID(policyId);
            AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(apiPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Advanced level policy : " + policyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates a given Advanced level policy specified by uuid
     *
     * @param policyId        uuid of the policy
     * @param body              DTO of policy to be updated
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Updated policy
     */
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyIdPut(String policyId,
            AdvancedThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();

            //overridden parameters
            body.setPolicyId(policyId);

            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiProvider.updatePolicy(apiPolicy);

            //retrieve the new policy and send back as the response
            APIPolicy newApiPolicy = apiProvider.getAPIPolicy(userName, body.getPolicyName());
            AdvancedThrottlePolicyDTO policyDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAdvancedPolicyToDTO(newApiPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Advanced level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Delete an Advanced level policy specified by uuid
     *
     * @param policyId        uuid of the policy
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response throttlingPoliciesAdvancedPoliciesPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            APIPolicy apiPolicy = apiProvider.getAPIPolicyByUUID(policyId);
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_API, apiPolicy.getPolicyName());
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Advanced level policy : " + policyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves all Application Throttle Policies
     *
     * @param limit           maximum number of policies to return
     * @param offset          starting index
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Retrieves all Application Throttle Policies
     */
    @Override
    public Response throttlingPoliciesApplicationGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            ApplicationPolicy[] appPolicies = (ApplicationPolicy[]) apiProvider
                    .getPolicies(userName, PolicyConstants.POLICY_LEVEL_APP);
            ApplicationThrottlePolicyListDTO listDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationPolicyArrayToListDTO(appPolicies);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Application level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Add an Application Level Throttle Policy
     *
     * @param body        DTO of the Application Policy to add
     * @param contentType Content-Type header
     * @return Newly created Application Throttle Policy with the location with the Location header
     */
    @Override
    public Response throttlingPoliciesApplicationPost(ApplicationThrottlePolicyDTO body, String contentType) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            ApplicationPolicy appPolicy = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(
                    body);
            apiProvider.addPolicy(appPolicy);

            //retrieve the new policy and send back as the response
            ApplicationPolicy newAppPolicy = apiProvider.getApplicationPolicy(userName,
                    body.getPolicyName());
            ApplicationThrottlePolicyDTO policyDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyToDTO(newAppPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_APPLICATION + "/" + policyDTO
                            .getPolicyId())).entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding an Application level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving Application Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get a specific Application Policy by its uuid
     *
     * @param policyId      uuid of the policy
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Matched Application Throttle Policy by the given name
     */
    @Override
    public Response throttlingPoliciesApplicationPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            ApplicationPolicy appPolicy = apiProvider.getApplicationPolicyByUUID(policyId);
            ApplicationThrottlePolicyDTO policyDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyToDTO(appPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Application level policy: " + policyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates a given Application level policy specified by uuid
     *
     * @param policyId        uuid of the policy
     * @param body              DTO of policy to be updated
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Updated policy
     */
    @Override
    public Response throttlingPoliciesApplicationPolicyIdPut(String policyId,
            ApplicationThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            
            //overridden properties
            body.setPolicyId(policyId);

            ApplicationPolicy appPolicy = ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyDTOToModel(
                    body);
            apiProvider.updatePolicy(appPolicy);

            //retrieve the new policy and send back as the response
            ApplicationPolicy newAppPolicy = apiProvider.getApplicationPolicy(userName, body.getPolicyName());
            ApplicationThrottlePolicyDTO policyDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyToDTO(newAppPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Application level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Delete an Application level policy specified by uuid
     *
     * @param policyId        uuid of the policy
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response throttlingPoliciesApplicationPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            Policy policy = apiProvider.getApplicationPolicyByUUID(policyId);
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_APP, policy.getPolicyName());
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_APP_POLICY, policyId, e, log);
            } else {
                String errorMessage = "Error while deleting Application level policy : " + policyId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves all Subscription level policies
     *
     * @param limit           maximum number of policies to return
     * @param offset          starting index
     * @param accept           Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return All matched Subscription Throttle policies to the given request
     */
    @Override
    public Response throttlingPoliciesSubscriptionGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            SubscriptionPolicy[] subscriptionPolicies = (SubscriptionPolicy[]) apiProvider
                    .getPolicies(userName, PolicyConstants.POLICY_LEVEL_SUB);
            SubscriptionThrottlePolicyListDTO listDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionPolicyArrayToListDTO(subscriptionPolicies);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while retrieving Subscription level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Add a Subscription Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response throttlingPoliciesSubscriptionPost(SubscriptionThrottlePolicyDTO body, String contentType) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyDTOToModel(
                            body);
            apiProvider.addPolicy(subscriptionPolicy);

            //retrieve the new policy and send back as the response
            SubscriptionPolicy newSubscriptionPolicy = apiProvider.getSubscriptionPolicy(userName,
                    body.getPolicyName());
            SubscriptionThrottlePolicyDTO policyDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(newSubscriptionPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_SUBSCRIPTION + "/" + policyDTO
                            .getPolicyId())).entity(policyDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while adding a Subscription level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving Subscription Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get a specific Subscription Policy by its uuid
     *
     * @param policyId      uuid of the policy
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Matched Subscription Throttle Policy by the given name
     */
    @Override
    public Response throttlingPoliciesSubscriptionPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            SubscriptionPolicy subscriptionPolicy = apiProvider.getSubscriptionPolicyByUUID(policyId);
            SubscriptionThrottlePolicyDTO policyDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(subscriptionPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while retrieving Subscription level policy: " + policyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates a given Subscription level policy specified by uuid
     *
     * @param policyId        u
     * @param body              DTO of policy to be updated
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Updated policy
     */
    @Override
    public Response throttlingPoliciesSubscriptionPolicyIdPut(String policyId,
            SubscriptionThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();

            //overridden properties
            body.setPolicyId(policyId);

            SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyDTOToModel(
                            body);
            apiProvider.updatePolicy(subscriptionPolicy);

            //retrieve the new policy and send back as the response
            SubscriptionPolicy newSubscriptionPolicy = apiProvider
                    .getSubscriptionPolicy(userName, body.getPolicyName());
            SubscriptionThrottlePolicyDTO policyDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyToDTO(newSubscriptionPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException | ParseException e) {
            String errorMessage = "Error while updating Subscription level policy: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Delete a Subscription level policy specified by uuid
     *
     * @param policyId        uuid of the policyu
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response throttlingPoliciesSubscriptionPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();
            SubscriptionPolicy subscriptionPolicy = apiProvider.getSubscriptionPolicyByUUID(policyId);
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_SUB, subscriptionPolicy.getPolicyName());
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Subscription level policy : " + policyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves all Global level policies
     *
     * @param limit           maximum number of policies to return
     * @param offset          starting index
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return All matched Global Throttle policies to the given request
     */
    @Override
    public Response throttlingPoliciesCustomGet(Integer limit, Integer offset, String accept,
            String ifNoneMatch, String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String userName = RestApiUtil.getLoggedInUsername();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            GlobalPolicy[] globalPolicies = (GlobalPolicy[]) apiProvider
                    .getPolicies(userName, PolicyConstants.POLICY_LEVEL_GLOBAL);
            CustomRuleListDTO listDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalPolicyArrayToListDTO(globalPolicies);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Global level policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Add an Global Level Throttle Policy
     *
     * @param body        DTO of new policy to be created
     * @param contentType Content-Type header
     * @return Created policy along with the location of it with Location header
     */
    @Override
    public Response throttlingPoliciesCustomPost(CustomRuleDTO body, String contentType) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            GlobalPolicy globalPolicy = GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyDTOToModel(
                    body);
            apiProvider.addPolicy(globalPolicy);

            //retrieve the new policy and send back as the response
            GlobalPolicy newGlobalPolicy = apiProvider.getGlobalPolicy(body.getPolicyName());
            CustomRuleDTO policyDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalThrottlePolicyToDTO(newGlobalPolicy);
            return Response.created(
                    new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_POLICIES_GLOBAL + "/" + policyDTO.getPolicyId()))
                    .entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding a custom rule: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Global Throttle policy location : " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get a specific custom rule by its name
     *
     * @param ruleId      uuid of the policy
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Matched Global Throttle Policy by the given name
     */
    @Override
    public Response throttlingPoliciesCustomRuleIdGet(String ruleId, String ifNoneMatch,
            String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            GlobalPolicy globalPolicy = apiProvider.getGlobalPolicyByUUID(ruleId);
            CustomRuleDTO policyDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalThrottlePolicyToDTO(globalPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Custom Rule: " + ruleId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates a given Global level policy/custom rule specified by uuid
     *
     * @param ruleId        uuid of the policy
     * @param body              DTO of policy to be updated
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Updated policy
     */
    @Override
    public Response throttlingPoliciesCustomRuleIdPut(String ruleId, CustomRuleDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            //overridden properties
            body.setPolicyId(ruleId);

            GlobalPolicy globalPolicy = GlobalThrottlePolicyMappingUtil.fromGlobalThrottlePolicyDTOToModel(
                    body);
            apiProvider.updatePolicy(globalPolicy);

            //retrieve the new policy and send back as the response
            GlobalPolicy newGlobalPolicy = apiProvider.getGlobalPolicy(body.getPolicyName());
            CustomRuleDTO policyDTO = GlobalThrottlePolicyMappingUtil
                    .fromGlobalThrottlePolicyToDTO(newGlobalPolicy);
            return Response.ok().entity(policyDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating custom rule: " + body.getPolicyName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Delete a Global level policy/custom rule specified by uuid
     *
     * @param ruleId        uuid of the policy
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 OK response if successfully deleted the policy
     */
    @Override
    public Response throttlingPoliciesCustomRuleIdDelete(String ruleId, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //only super tenant is allowed to access global policies/custom rules
            checkTenantDomainForCustomRules();

            String userName = RestApiUtil.getLoggedInUsername();
            GlobalPolicy globalPolicy = apiProvider.getGlobalPolicyByUUID(ruleId);
            apiProvider.deletePolicy(userName, PolicyConstants.POLICY_LEVEL_GLOBAL, globalPolicy.getUUID());
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting custom rule : " + ruleId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves all Block Conditions
     *
     * @param limit           maximum number of block conditions to return
     * @param offset          starting index
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return All matched block conditions to the given request
     */
    @Override
    public Response throttlingBlacklistGet(Integer limit, Integer offset, String accept, String ifNoneMatch,
            String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<BlockConditionsDTO> blockConditions = apiProvider.getBlockConditions();
            BlockingConditionListDTO listDTO = BlockingConditionMappingUtil
                    .fromBlockConditionListToListDTO(blockConditions);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Block Conditions";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Add a Block Condition
     *
     * @param body        DTO of new block condition to be created
     * @param contentType Content-Type header
     * @return Created block condition along with the location of it with Location header
     */
    @Override
    public Response throttlingBlacklistPost(BlockingConditionDTO body, String contentType) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String uuid = apiProvider.addBlockCondition(body.getConditionType(), body.getConditionValue());

            //retrieve the new blocking condition and send back as the response
            BlockConditionsDTO newBlockingCondition = apiProvider.getBlockConditionByUUID(uuid);
            BlockingConditionDTO dto = BlockingConditionMappingUtil
                    .fromBlockingConditionToDTO(newBlockingCondition);
            return Response.created(new URI(RestApiConstants.RESOURCE_PATH_THROTTLING_BLOCK_CONDITIONS + "/" + uuid))
                    .entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage =
                    "Error while adding Blocking Condition. Condition type: " + body.getConditionType() + ", value: "
                            + body.getConditionValue();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving Blocking Condition resource location. Condition type: " + body
                    .getConditionType() + ", value: " + body.getConditionValue();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get a specific Block condition by its id
     *
     * @param conditionId     Id of the block condition
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Matched block condition for the given Id
     */
    @Override
    public
    Response throttlingBlacklistConditionIdGet(String conditionId, String ifNoneMatch,
            String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            BlockConditionsDTO blockCondition = apiProvider
                    .getBlockConditionByUUID(conditionId);
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(blockCondition);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Block Condition. Id : " + conditionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates a given block condition specified by the condition Id
     *
     * @param conditionId       Id of the block condition
     * @param body              DTO of block condition to be updated
     * @param contentType       Content-Type header
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Updated block condition
     */
    @Override
    public Response throttlingBlacklistConditionIdPut(String conditionId, BlockingConditionDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            apiProvider.updateBlockConditionByUUID(conditionId, body.getEnabled().toString());
            //retrieves the updated condition and send back as the response
            BlockConditionsDTO updatedBlockCondition = apiProvider.getBlockConditionByUUID(conditionId);
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(updatedBlockCondition);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating Block Condition. Id : " + conditionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Delete a block condition specified by the condition Id
     *
     * @param conditionId       Id of the block condition
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 OK response if successfully deleted the block condition
     */
    @Override
    public Response throttlingBlacklistConditionIdDelete(String conditionId, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            apiProvider.deleteBlockConditionByUUID(conditionId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting Block Condition. Id : " + conditionId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Checks if the logged in user belongs to super tenant and throws 403 error if not
     *
     * @throws ForbiddenException
     */
    private void checkTenantDomainForCustomRules() throws ForbiddenException {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            RestApiUtil.handleAuthorizationFailure("You are not allowed to access this resource",
                    new APIManagementException("Tenant " + tenantDomain
                            + " is not allowed to access custom rules. Only super tenant is allowed"), log);
        }
    }
}
