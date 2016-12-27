/**
 * Copyright (c) 2016 Respect Network Corp. All Rights Reserved.
 */
package biz.neustar.clouds.chat.model;

/**
 * Author: kvats Date: Apr 5, 2016 Time: 4:07:05 PM
 */
public class QueryInfo {

    private int offset;
    private int limit;
    private String sortOrder;
    private String status;

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset
     *            the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit
     *            the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return the sortOrder
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * @param sortOrder
     *            the sortOrder to set
     */
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

	/**
	 * @return status READ/UNREAD
	 *
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 * 			READ/UNREAD to set
	 */
	public void setStatus(String status) {
		this.status = status;
    }

}
