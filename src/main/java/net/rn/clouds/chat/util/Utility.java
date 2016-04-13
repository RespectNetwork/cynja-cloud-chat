/**
 * Copyright (c) 2016 Respect Network Corp. All Rights Reserved.
 */
package net.rn.clouds.chat.util;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.clouds.chat.model.QueryInfo;

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
        queryInfo.setSortOrder(req.getParameter("offset"));
        return queryInfo;
    }
}
