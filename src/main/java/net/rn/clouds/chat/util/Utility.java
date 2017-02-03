/**
 * Copyright (c) 2016 Respect Network Corp. All Rights Reserved.
 */
package net.rn.clouds.chat.util;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rn.clouds.chat.constants.ChatErrors;
import net.rn.clouds.chat.dao.CloudNameDAO;
import net.rn.clouds.chat.dao.EntityCloudDAO;
import net.rn.clouds.chat.dao.impl.CloudNameHibernateDAO;
import net.rn.clouds.chat.dao.impl.EntityCloudHibernateDAO;
import net.rn.clouds.chat.exceptions.ChatSystemException;
import net.rn.clouds.chat.exceptions.ChatValidationException;
import net.rn.clouds.chat.model.CloudName;
import net.rn.clouds.chat.model.EntityCloud;
import net.rn.clouds.chat.service.impl.PBKDF2PasswordImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.impl.http.XDIHttpClient;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.parser.ParserException;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import biz.neustar.clouds.chat.InitFilter;
import biz.neustar.clouds.chat.model.QueryInfo;
import biz.neustar.clouds.chat.service.PasswordManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Author: kvats Date: Apr 5, 2016 Time: 4:07:05 PM
 */
public class Utility {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);

    public static QueryInfo createQueryInfo(HttpServletRequest req) {
        QueryInfo queryInfo = new QueryInfo();
        if (req.getParameter("offset") != null) {
            queryInfo.setOffset(Integer.valueOf(req.getParameter("offset")));
        }
        if (req.getParameter("limit") != null) {
            queryInfo.setLimit(Integer.valueOf(req.getParameter("limit")));
        }
        queryInfo.setSortOrder(req.getParameter("sortOrder"));
        queryInfo.setStatus(req.getParameter("status"));
        return queryInfo;
    }

    public static XDIAddress createXDIAddress(String cloud) {
        LOGGER.info("Enter creteXDIAddress for cloud: {}", cloud);
        try {
            return XDIAddress.create(cloud);
        } catch (ParserException pe) {
            LOGGER.error("Incorrect cloud format: " + cloud);
            throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(),
                    ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage() + cloud);
        }
    }

    public static void handleChatException(HttpServletResponse resp, int errorCode, String errorMessage) {

        LOGGER.info("Enter handleChatException for errorCode: {} errorMessage: {}", errorCode, errorMessage);
        resp.setContentType("appliction/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        JsonObject logJsonObject = new JsonObject();
        logJsonObject.add("errorCode", new JsonPrimitive(errorCode));
        logJsonObject.add("errorMessage", new JsonPrimitive(errorMessage));

        try {
            resp.getWriter().write(logJsonObject.toString());
        } catch (IOException e) {
            LOGGER.error("ErrorCode: [{}] errorMessage: {}", ChatErrors.SYSTEM_ERROR, e.getMessage(), e);
            throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),
                    ChatErrors.SYSTEM_ERROR.getErrorMessage());
        }
    }

    /**
     * @param connection
     */
    public static Integer getConnectionId(XDIAddress cloud1, XDIAddress cloud2) {
        Integer id = cloud1.hashCode() * cloud2.hashCode();
        LOGGER.debug("Connection id is: {}", id);
        return Math.abs(id);
    }

    public static XDIDiscoveryResult getXDIDiscovery(XDIAddress cloud) {

        XDIDiscoveryResult cloudDiscoveryResult = null;
        LOGGER.info("Getting discovery of cloud: {}", cloud.toString());
        if (cloud != null) {
            try {
                XDIDiscoveryClient cloudDiscovery = new XDIDiscoveryClient(
                        ((XDIHttpClient) InitFilter.XDI_DISCOVERY_CLIENT.getRegistryXdiClient()).getXdiEndpointUri());
                cloudDiscoveryResult = cloudDiscovery.discoverFromRegistry(cloud);

                if (cloudDiscoveryResult == null || cloudDiscoveryResult.toString().equals("null (null)")) {

                    LOGGER.error("{} not found", cloud.toString());
                    throw new ChatValidationException(ChatErrors.CLOUD_NOT_FOUND.getErrorCode(), cloud.toString()
                            + ChatErrors.CLOUD_NOT_FOUND.getErrorMessage());
                }
                LOGGER.info("cloud number: {}", cloudDiscoveryResult.getCloudNumber().toString());
            } catch (Xdi2ClientException clientExcption) {

                LOGGER.error("Error while discovery of cloud: {}", clientExcption);
                throw new ChatValidationException(ChatErrors.CLOUD_NOT_FOUND.getErrorCode(), cloud.toString()
                        + ChatErrors.CLOUD_NOT_FOUND.getErrorMessage());
            }
        }
        return cloudDiscoveryResult;
    }

    public static void authenticate(String cloud, String cloudSecretToken) {
        List<CloudName> cloudNames = null;
        List<EntityCloud> entityClouds = null;
        CloudName cloudName = null;
        try {
            CloudNameDAO cloudNameDAO = new CloudNameHibernateDAO();
            cloudNames = cloudNameDAO.findByCloudName(cloud);

        } catch (Exception ex) {
            LOGGER.error("Error while authenticating cloud: {}" + cloud, ex);
            throw new ChatValidationException(ChatErrors.AUTHENTICATOION_FAILED.getErrorCode(),
                    ChatErrors.AUTHENTICATOION_FAILED.getErrorMessage() + cloud.toString());
        }
        if (cloudNames != null && !cloudNames.isEmpty()) {
            cloudName = cloudNames.get(0);
        } else {
            EntityCloudDAO entityCloudDAO = new EntityCloudHibernateDAO();
            entityClouds = entityCloudDAO.findByCloudNumber(cloud);
            if (entityClouds == null || entityClouds.isEmpty()) {
                LOGGER.info("Login to personal cloud failed for {}", cloud);
                throw new ChatValidationException(ChatErrors.AUTHENTICATOION_FAILED.getErrorCode(),
                        ChatErrors.AUTHENTICATOION_FAILED.getErrorMessage() + cloud.toString());
            }
            cloudName = entityClouds.get(0).getCloudNames().get(0);
        }
        boolean validPassword = false;
        if (cloudName.getEntityCloud() != null) {
            PasswordManager passwordManager = new PBKDF2PasswordImpl();
            validPassword = passwordManager.validatePassword(cloudSecretToken, cloudName.getEntityCloud().getSalt(),
                    cloudName.getEntityCloud().getPassword());
        }
        if (!validPassword) {
            LOGGER.error("Authenticating to personal cloud failed for {}", cloud);
            throw new ChatValidationException(ChatErrors.AUTHENTICATOION_FAILED.getErrorCode(),
                    ChatErrors.AUTHENTICATOION_FAILED.getErrorMessage() + cloud);
        }
        LOGGER.info("Authenticated successflly for {}", cloud);
    }
}
