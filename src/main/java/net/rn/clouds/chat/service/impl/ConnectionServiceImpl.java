/**
 * 
 */
package net.rn.clouds.chat.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.model.QueryInfo;
import biz.neustar.clouds.chat.service.ConnectionService;
import net.rn.clouds.chat.constants.ChatErrors;
import net.rn.clouds.chat.constants.DeleteRenew;
import net.rn.clouds.chat.constants.MessageStatus;
import net.rn.clouds.chat.constants.Status;
import net.rn.clouds.chat.dao.ConnectionProfileDAO;
import net.rn.clouds.chat.dao.ConnectionRequestDAO;
import net.rn.clouds.chat.dao.EntityCloudDAO;
import net.rn.clouds.chat.dao.impl.ConnectionProfileDAOImpl;
import net.rn.clouds.chat.dao.impl.ConnectionRequestDAOImpl;
import net.rn.clouds.chat.dao.impl.EntityCloudHibernateDAO;
import net.rn.clouds.chat.exceptions.ChatSystemException;
import net.rn.clouds.chat.exceptions.ChatValidationException;
import net.rn.clouds.chat.model.ChatMessage;
import net.rn.clouds.chat.model.ConnectingClouds;
import net.rn.clouds.chat.model.ConnectionProfile;
import net.rn.clouds.chat.model.ConnectionRequest;
import net.rn.clouds.chat.util.EntityUtil;
import net.rn.clouds.chat.util.Utility;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.XDIAddress;
/**
 * @author Noopur Pandey
 *
 */
public class ConnectionServiceImpl implements ConnectionService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionServiceImpl.class);
	
	

	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#requestConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress)
	 */
	@Override
	public Connection requestConnection(XDIAddress cloud1, String cloud1SecretToken, XDIAddress cloud2){
		
		LOGGER.info("Enter requestConnection with requestingCloud: {}, acceptingCloud: {}", cloud1, cloud2);
		try {
			if(cloud1.toString().equals(cloud2.toString())){
				
				LOGGER.info("Invalid connection requested between {} and {}",cloud1.toString(), cloud2.toString());
				throw new ChatValidationException(ChatErrors.INVALID_CONNECTION_REQUEST.getErrorCode(), ChatErrors.INVALID_CONNECTION_REQUEST.getErrorMessage());
			}
			Utility.authenticate(cloud1.toString(), cloud1SecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());

			String cloud1Number = null;
			String cloud2Number = null;
			String cloud1Name = null;
			String cloud2Name = null;

			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloud1Number = (String)obj[0];
					cloud1Name = (String)obj[1];
				}
			}

			if(cloudList2 != null && cloudList2.size() > 0){
				Object[] obj = cloudList2.get(0);
				if(obj != null && obj.length > 0){
					cloud2Number = (String)obj[0];
					cloud2Name = (String)obj[1];
				}
			}
			if(cloud2Number == null) {
			    throw new ChatValidationException(ChatErrors.CLOUD_NOT_FOUND.getErrorCode(), cloud2.toString()+ChatErrors.CLOUD_NOT_FOUND.getErrorMessage());
			}
			LOGGER.info("Checking if connection already requested");
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1Number, cloud2Number);
			
			String cloudParent = EntityUtil.getGuardianCloudNumber(cloud1Number);			
			
			if(connectionRequestList != null && connectionRequestList.size() >= 1){
				//Request already exists
				for (Object obj : connectionRequestList) {

					if(obj instanceof ConnectionRequest){

						ConnectionRequest connectionRequest = (ConnectionRequest)obj;
						String deleteRenew = connectionRequest.getDeleteRenew();
						String status = connectionRequest.getStatus();
						String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber();
						String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber();

						if((requestingCloudNumber.equals(cloud1Number) && ((deleteRenew == null && status.equals(Status.NEW.getStatus()) ||
								status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus()) || status.equals(Status.CHILD_APPROVAL_PENDING.getStatus()))
								|| (deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())))) 
								||
								(acceptingCloudNumber.equals(cloud1Number) && deleteRenew != null && 
								(deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew()) || 
										(deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew()) && status.equals(Status.NEW.getStatus()))))){

							LOGGER.info("Connection already requested between {} and {} and is in pending state", cloud1.toString(), cloud2.toString());
							throw new ChatValidationException(ChatErrors.PENDING_FOR_APPROVAL.getErrorCode(), ChatErrors.PENDING_FOR_APPROVAL.getErrorMessage());

						}else if((deleteRenew == null && (status.equals(Status.APPROVED.getStatus()) ||
								(requestingCloudNumber.equals(cloud1Number) && status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())) ||
								(acceptingCloudNumber.equals(cloud1Number) && status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())))) 
								||
								(deleteRenew != null && status.equals(Status.APPROVED.getStatus()) && 
								(requestingCloudNumber.equals(cloud1Number) && !deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew()) ||
										(acceptingCloudNumber.equals(cloud1Number) && !deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew()))))){

							LOGGER.info("Connection already requested between {} and {}", cloud1.toString(), cloud2.toString());
							throw new ChatValidationException(ChatErrors.CONNECTION_ALREADY_EXISTS.getErrorCode(),ChatErrors.CONNECTION_ALREADY_EXISTS.getErrorMessage());

						}else if(status.equals(Status.BLOCKED.getStatus()) || 
								(acceptingCloudNumber.equals(cloud1Number) && status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())) ||
								(requestingCloudNumber.equals(cloud1Number) && status.equals(Status.BLOCKED_BY_REQUESTER.getStatus()))){

							LOGGER.info("Connection already requested between {} and {} and is in blocked state", cloud1.toString(), cloud2.toString());
							throw new ChatValidationException(ChatErrors.CONNECTION_BLOCKED.getErrorCode(), ChatErrors.CONNECTION_BLOCKED.getErrorMessage());
						}

						//Request has been deleted from one of the cloud
						if(cloudParent.equals("")){

							if(acceptingCloudNumber.equals(cloud1Number) && status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus())){

								LOGGER.info("Approving the request");
								connectionRequest.setStatus(Status.APPROVED.getStatus());
								connectionRequest.setApprovingCloudNumber(null);
							}else if(acceptingCloudNumber.equals(cloud1Number) && status.equals(Status.NEW.getStatus())){
								LOGGER.info("Approved from cloud1 and renewed from cloud2 ");
								connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew());
							}else{
								LOGGER.info("Revert the deletion ");
								connectionRequest.setDeleteRenew(null);
							}

						}else{

							LOGGER.info("Raise a request to parent to revert the deletion or if a request is already raised");
							if(requestingCloudNumber.equals(cloud1Number)){

								if(connectionRequest.getDeleteRenew() != null && 
										connectionRequest.getDeleteRenew().equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){

									LOGGER.info("Connection already requested between {} and {} and is in pending state", cloud1.toString(), cloud2.toString());
									throw new ChatValidationException(ChatErrors.PENDING_FOR_APPROVAL.getErrorCode(), ChatErrors.PENDING_FOR_APPROVAL.getErrorMessage());
								}else{

									connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew());
								}

							}else if(acceptingCloudNumber.equals(cloud1Number)){

								if(connectionRequest.getDeleteRenew() != null && 
										connectionRequest.getDeleteRenew().equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){

									LOGGER.info("Connection already requested between {} and {} and is in pending state", cloud1.toString(), cloud2.toString());
									throw new ChatValidationException(ChatErrors.PENDING_FOR_APPROVAL.getErrorCode(), ChatErrors.PENDING_FOR_APPROVAL.getErrorMessage());
								}else if(status.equals(Status.CHILD_APPROVAL_PENDING.getStatus())){

									connectionRequest.setStatus(Status.APPROVED.getStatus());
									connectionRequest.setApprovingCloudNumber(null);
								}else {

									connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew());
								}
							}
						}
						connectionRequestDAO.updateRequest(connectionRequest);						
					}
				}
			}else{
				//Request does not exists, so raise a new request
				String approvingCloudNumber = cloudParent;
				String status = Status.CLOUD_APPROVAL_PENDING.getStatus();

				LOGGER.info("Checking if requester is a dependent cloud");
				if(!cloudParent.equals("")){

					approvingCloudNumber = cloudParent;
					status = Status.NEW.getStatus();
				}else{

					LOGGER.info("Checking if acceptor is a dependent cloud");
					String acceptorCloudParent = EntityUtil.getGuardianCloudNumber(cloud2Number);
					if(!acceptorCloudParent.equals("")){
						approvingCloudNumber = acceptorCloudParent;
					}else{
						approvingCloudNumber = cloud2Number;
					}
				}
				LOGGER.info("Approving cloud: {}, status: {}", approvingCloudNumber, status);

				LOGGER.info("Creating new connection request");
				ConnectingClouds connectingClouds = new ConnectingClouds(
						cloud1Number, cloud2Number);
				ConnectionRequest connectionRequest = new ConnectionRequest();

				connectionRequest.setConnectingClouds(connectingClouds);
				connectionRequest.setRequestingConnectionName(cloud1Name);
				connectionRequest.setAcceptingConnectionName(cloud2Name);
				connectionRequest.setApprovingCloudNumber(approvingCloudNumber);
				connectionRequest.setStatus(status);

				Integer connection_id = Utility.getConnectionId(Utility.createXDIAddress(cloud1Number), Utility.createXDIAddress(cloud2Number));
				connectionRequest.setConnectionId(connection_id);

				LOGGER.info("Saving new connection request");
				connectionRequestDAO.requestConnection(connectionRequest);
			}
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while requesting connection: {}", chatException);
			throw chatException;

		}catch (Exception ex) {

			LOGGER.error("Error while requesting connection: {}", ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		LOGGER.info("Exit requestConnection with cloud1: {}, cloud2: {}", cloud1, cloud2);
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#approveConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection approveConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){	

		LOGGER.info("Enter approveConnection with approverCloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			Utility.authenticate(cloud.toString(), cloudSecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());

			String cloudNumber = null;
			String cloud1CloudNumber = null;
			String cloud2CloudNumber = null;

			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];
				}
			}

			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloud1CloudNumber = (String)obj[0];
				}
			}

			if(cloudList2 != null && cloudList2.size() > 0){
				Object[] obj = cloudList2.get(0);
				if(obj != null && obj.length > 0){
					cloud2CloudNumber = (String)obj[0];
				}
			}

			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);

			LOGGER.info("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.info("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage());
			}							

			LOGGER.info("Getting connection request");
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);						

			if(connectionRequestList == null || connectionRequestList.size()==0){

				LOGGER.info("Connection request not found");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}

			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){

					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();
					String approvingCloudNumber = connectionRequest.getApprovingCloudNumber();

					String deleteRenew = connectionRequest.getDeleteRenew();
					String status = connectionRequest.getStatus();
					String newStatus = status;
					String newApprover = null;

					String requestingCloudParent = EntityUtil.getGuardianCloudNumber(requestingCloudNumber);
					String acceptingCloudParent = EntityUtil.getGuardianCloudNumber(acceptingCloudNumber);

					if(!guardianCloudNumber.equals("") && guardianCloudNumber.equals(cloudNumber) && requestingCloudParent.equals(acceptingCloudParent)){

						if(status.equals(Status.NEW.getStatus()) && cloud1CloudNumber.equals(requestingCloudNumber)){

							connectionRequest.setStatus(Status.CHILD_APPROVAL_PENDING.getStatus());
							connectionRequest.setApprovingCloudNumber(acceptingCloudNumber);

						}else if (deleteRenew != null && 
								((cloud1CloudNumber.equals(requestingCloudNumber) && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())) || 
										(cloud1CloudNumber.equals(acceptingCloudNumber) && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())))){

							connectionRequest.setStatus(Status.APPROVED.getStatus());
							connectionRequest.setApprovingCloudNumber(null);
							connectionRequest.setDeleteRenew(null);

						}else if(status.equals(Status.APPROVED.getStatus())){

								LOGGER.info("connection request is already approved");
								throw new ChatValidationException(ChatErrors.ALREADY_APPROVED.getErrorCode(),ChatErrors.ALREADY_APPROVED.getErrorMessage());

						}else if(status.equals(Status.BLOCKED.getStatus()) || 
									(cloud1CloudNumber.equals(requestingCloudNumber) && status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())) ||
									(cloud1CloudNumber.equals(acceptingCloudNumber) && status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus()))){

						}else{
							LOGGER.info("Cloud: {} is not authorized approver.", cloud.toString());
							throw new ChatValidationException(ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorCode(),ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorMessage());
						}
					}else{

						if(deleteRenew != null && (!guardianCloudNumber.equals(""))){

							if(!cloudNumber.equals(guardianCloudNumber)){

								LOGGER.info("Cloud: {} is not authorized approver.", cloud.toString());
								throw new ChatValidationException(ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorCode(),ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorMessage());
							}
							if(guardianCloudNumber.equals(requestingCloudParent)){

								if(status.equals(Status.NEW.getStatus()) && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){

									connectionRequest.setStatus(Status.CLOUD_APPROVAL_PENDING.getStatus());
									connectionRequest.setApprovingCloudNumber(acceptingCloudParent);

								}else if(status.equals(Status.NEW.getStatus()) && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){

									connectionRequest.setStatus(Status.APPROVED.getStatus());
									connectionRequest.setApprovingCloudNumber(null);
									connectionRequest.setDeleteRenew(null);

								}else if(deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){

									connectionRequest.setDeleteRenew(null);

								}else{

									LOGGER.info("Cloud: {} is not authorized approver.", cloud.toString());
									throw new ChatValidationException(ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorCode(),ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorMessage());
								}

							}else if(guardianCloudNumber.equals(acceptingCloudParent)){

								if(status.equals(Status.NEW.getStatus()) && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){

									connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew());

								}else if(status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus()) && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){

									connectionRequest.setStatus(Status.APPROVED.getStatus());
									connectionRequest.setApprovingCloudNumber(null);
									connectionRequest.setDeleteRenew(null);

								}else if(deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){

									connectionRequest.setDeleteRenew(null);

								}else{

									LOGGER.info("Cloud: {} is not authorized approver.", cloud.toString());
									throw new ChatValidationException(ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorCode(),ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorMessage());
								}
							}
						}else{

							if(status.equals(Status.APPROVED.getStatus()) || status.equals(Status.BLOCKED.getStatus()) ||
									status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus()) || status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
								LOGGER.info("connection request is already approved");
								throw new ChatValidationException(ChatErrors.ALREADY_APPROVED.getErrorCode(),ChatErrors.ALREADY_APPROVED.getErrorMessage());
							}

							if(!cloudNumber.equals(approvingCloudNumber)){
								LOGGER.info("Cloud: {} is not authorized approver.", cloud.toString());
								throw new ChatValidationException(ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorCode(),ChatErrors.NOT_AUTHORIZED_TO_APPROVE.getErrorMessage());
							}

							if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){

								newStatus = Status.APPROVED.getStatus();
								newApprover = null;

							}else if(!acceptingCloudParent.equals("")){

								if(status.equals(Status.NEW.getStatus())){

									newStatus = Status.CLOUD_APPROVAL_PENDING.getStatus();
									newApprover =  acceptingCloudParent;

								}else if(status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus())){

									newStatus = Status.CHILD_APPROVAL_PENDING.getStatus();
									newApprover = acceptingCloudNumber;

								}else if(status.equals(Status.CHILD_APPROVAL_PENDING.getStatus())){

									newStatus = Status.APPROVED.getStatus();
									newApprover = null;
								}
							}else{
								if(status.equals(Status.NEW.getStatus())){

									if(acceptingCloudNumber.equals(approvingCloudNumber)){

										newStatus = Status.APPROVED.getStatus();
										newApprover = null;

									}else{

										newStatus = Status.CLOUD_APPROVAL_PENDING.getStatus();
										newApprover = acceptingCloudNumber;
									}
								}
								if(status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus())){

									newStatus = Status.APPROVED.getStatus();
									newApprover = null;
								}
							}
							connectionRequest.setApprovingCloudNumber(newApprover);
							connectionRequest.setStatus(newStatus);
						}
					}

					LOGGER.info("Upating connection request");
					connectionRequestDAO.updateRequest(connectionRequest);					
				}
			}													
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while approving connection: {}", chatException);
			throw chatException;
			
		}catch (Exception ex) {

			LOGGER.error("Error while approving connection: {}", ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.info("Exit approveConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#viewConnectionsAsParent(xdi2.core.syntax.XDIAddress, java.lang.String)
	 */
	public Connection[] viewConnectionsAsParent(XDIAddress parent, String parentSecretToken){
		LOGGER.info("Enter viewConnectionsAsParent with parent: {} ", parent);
		List<Connection> connectionList = new ArrayList<Connection>();
		try {
			Utility.authenticate(parent.toString(), parentSecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(parent.toString());

			String cloudNumber = null;

			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];
				}
			}

			LOGGER.info("Getting all children of parent cloud: {}",parent.toString());			
			List<String> children = entityDAO.findDependentByGuardian(cloudNumber);
			List<String> collection = new ArrayList<String>();
			String collection_str = "";
			for (String child : children) {
				
				LOGGER.info("Getting child cloudNumber", child);				
				LOGGER.info("Adding child: {} to list", child);
				collection.add(child);
				
				if(!collection_str.equals("")){
					collection_str+=",";
				}
				collection_str+="'"+child+"'";
			}
			
			LOGGER.info("Getting connection requests of children of parent cloud:{} ",parent.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			if(collection == null || collection.size()==0){
				return new ConnectionImpl[0];
			}
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.viewConnections(collection);
			if(connectionRequestList == null || connectionRequestList.size()==0){
				return new ConnectionImpl[0];				
			}						
			
			Set<String> cloudSet = new HashSet<String>();
			for (Object obj : connectionRequestList) {
				
				if(obj instanceof ConnectionRequest){
	    			ConnectionRequest connectionRequest = (ConnectionRequest)obj;					

					XDIAddress child1 = null;
					XDIAddress child2 = null;																																								
					CloudName connectionName = null;
					
					String deleteRenew = connectionRequest.getDeleteRenew();
					String status = connectionRequest.getStatus();
					
					if (collection.contains(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString()) && 
							!(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew()))){
						
						boolean isBlocked1 = false;
						boolean isBlocked2 = false;
						boolean isApproved1 = false;
						boolean isApproved2 = false;
						boolean isApprovalRequired = false;
						String blockedBy1 = null;
						String blockedBy2 = null;

						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew == null){

							isApproved1 = true;
							isApproved2 = true;
						}

						if(connectionRequest.getApprovingCloudNumber() != null && 
								connectionRequest.getApprovingCloudNumber().equals(cloudNumber)){

							isApprovalRequired = true;
						}

						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							isApprovalRequired = true;
						}
						child1 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						child2 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());												
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.info("Checking ig connection is blocked by requester");

						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){

							isBlocked1 = true;
							isApproved2 = true;
							blockedBy1 = connectionRequest.getBlockedByRequester();
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){

							isBlocked2 = true;
							isApproved1 = true;
							blockedBy2 = connectionRequest.getBlockedByAcceptor();

						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							isBlocked1 = true;
							isBlocked2 = true;
							blockedBy1 = connectionRequest.getBlockedByRequester();
							blockedBy2 = connectionRequest.getBlockedByAcceptor();
						}
						
						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew != null && 
								!deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							
							isApproved1 = true;							
						}
						
						LOGGER.info("Adding connection request to view list");
						
						Connection connection = new ConnectionImpl(child1, child2, isApprovalRequired, isApproved1, 
								isApproved2, isBlocked1, isBlocked2, connectionName, blockedBy1, blockedBy2);
						connectionList.add(connection);
						cloudSet.add(child2.toString());
						
					}if (collection.contains(connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString())){
						
						boolean isBlocked1 = false;
						boolean isBlocked2 = false;
						boolean isApproved1 = false;
						boolean isApproved2 = false;
						boolean isApprovalRequired = false;
						String blockedBy1 = null;
						String blockedBy2 = null;

						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew == null){

							isApproved1 = true;
							isApproved2 = true;
						}

						if(connectionRequest.getApprovingCloudNumber() != null && 
								connectionRequest.getApprovingCloudNumber().equals(cloudNumber)){

							isApprovalRequired = true;
						}

						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							LOGGER.info("Do not add connection request to view list if connection request is deleted by acceptor");
							continue;
						}
						
						if(status.equals(Status.NEW.getStatus()) && deleteRenew == null){
							LOGGER.info("Do not add connection request to view list if connection request has not been approved by requester guardian");
							continue;
						}
						
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
							isApprovalRequired = true;
						}
						
						child1 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						child2 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						
						LOGGER.info("Checking if connection is blocked by acceptor");
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							isBlocked1 = true;
							isApproved2 = true;
							blockedBy1 = connectionRequest.getBlockedByAcceptor();
							
						}if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							isBlocked2 = true;
							isApproved1 = true;
							blockedBy2 = connectionRequest.getBlockedByRequester();
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							isBlocked1 = true;
							isBlocked2 = true;
							blockedBy1 = connectionRequest.getBlockedByAcceptor();
							blockedBy2 = connectionRequest.getBlockedByRequester();							
						}
						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew != null && 
								!deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
								
								isApproved1 = true;
						}
						
						LOGGER.info("Adding connection request to view list");
						
						Connection connection = new ConnectionImpl(child1, child2, isApprovalRequired, isApproved1, 
								isApproved2, isBlocked1, isBlocked2, connectionName, blockedBy1, blockedBy2);
						connectionList.add(connection);
						cloudSet.add(child2.toString());
					}										
				}					
			}
			
			if(cloudSet != null && cloudSet.size() >= 1){								
				
				ConnectionProfileDAO profileDAO = new ConnectionProfileDAOImpl();
				List<ConnectionProfile> profiles = profileDAO.viewConnections(cloudSet);
				
				for (Object obj : connectionList) {
					
					ConnectionImpl connection = (ConnectionImpl)obj;
					
					for (Object profile : profiles){
						
						ConnectionProfile connectionProfile = (ConnectionProfile)profile;
						
						if(connectionProfile.getCloudNumber().equals(connection.getChild2().toString())){
							
							connection.setFirstName(connectionProfile.getFirstName());
							connection.setLastName(connectionProfile.getLastName());
							connection.setNickName(connectionProfile.getNickName());
							connection.setAvatar(connectionProfile.getAvatar());
							break;
						}
					}
				}
			}
			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while viewing connection as parent: {}", chatException);
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while viewing connection as parent: {}", ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.info("Exit viewConnectionsAsParent with parent: {} ", parent);
		
		Connection[] connections = new ConnectionImpl[connectionList.size()]; 
		return connectionList.toArray(connections);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#viewConnectionsAsChild(xdi2.core.syntax.XDIAddress, java.lang.String)
	 */
	public Connection[] viewConnectionsAsChild(XDIAddress cloud, String cloudSecretToken){
		LOGGER.info("Enter viewConnectionsAsChild with cloud: {}", cloud);
		List<Connection> connectionList = new ArrayList<Connection>();				

		try {
			Utility.authenticate(cloud.toString(), cloudSecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
			String cloudNumber = null;			
			
			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];					
				}
			}			
			
			List<String> collection = new ArrayList<String>();
			collection.add(cloudNumber);
			
			LOGGER.info("Getting connection requests of cloud: {}", cloud.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.viewConnections(collection);			
						
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.info("No connection request found for cloud: {}", cloud.toString());
				return new ConnectionImpl[0];				
			}										
			
			Set<String> cloudSet = new HashSet<String>();		
			for (Object obj : connectionRequestList) {
				
				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;																															
					XDIAddress cloud1;
					XDIAddress cloud2;																																								
					CloudName connectionName;
					boolean isBlocked1 = false;
					boolean isBlocked2 = false;
					boolean isApproved1 = false;
					boolean isApproved2 = false;
					String blockedBy1 = null;
					String blockedBy2 = null;
					
					String deleteRenew = connectionRequest.getDeleteRenew();
					String status = connectionRequest.getStatus();
					
					if (cloudNumber.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString())){
						
						if(deleteRenew !=null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())){
							LOGGER.info("Do not add connection request to view list if connection request is deleted by requester");
							continue;
						}
						
						cloud1 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						cloud2 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());						
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.info("Checking if connection request has been blocked by cloud: {}",cloud.toString());
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							isBlocked1 = true;
							isApproved2 = true;
							blockedBy1 = connectionRequest.getBlockedByRequester();
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							isBlocked2 = true;
							isApproved1 = true;
							blockedBy2 = connectionRequest.getBlockedByAcceptor();
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							isBlocked1 = true;
							isBlocked2 = true;
							blockedBy1 = connectionRequest.getBlockedByRequester();
							blockedBy2 = connectionRequest.getBlockedByAcceptor();
						}
						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew != null && 
								!deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							
							isApproved1 = true;							
						}	
						
					}else{
						
						if(deleteRenew !=null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							LOGGER.info("Do not add connection request to view list if connection request is deleted by acceptor");
							continue;
						}
										
						if(deleteRenew == null && status.equals(Status.NEW.getStatus())){
							LOGGER.info("Do not add connection request to view list if connection request has not been approved by requester's guardian");
							continue;
						}
						cloud1 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						cloud2 = Utility.createXDIAddress(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						
						LOGGER.info("Checking if connection request has been blocked by cloud: {}",cloud.toString());

						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							isBlocked1 = true;
							isApproved2 = true;
							blockedBy1 = connectionRequest.getBlockedByAcceptor();
							
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							isBlocked2 = true;
							isApproved1 = true;
							blockedBy2 = connectionRequest.getBlockedByRequester();
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							isBlocked1 = true;
							isBlocked2 = true;
							blockedBy1 = connectionRequest.getBlockedByAcceptor();
							blockedBy2 = connectionRequest.getBlockedByRequester();							
						}						
						else if(status.equals(Status.APPROVED.getStatus()) && deleteRenew != null && 
								!deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
							
							isApproved1 = true;
						}
					}
																				
					if(status.equals(Status.APPROVED.getStatus()) && deleteRenew == null){
													
						isApproved1 = true;
						isApproved2 = true;					
					}
							
					boolean isApprovalRequired = false;
					if(connectionRequest.getApprovingCloudNumber() != null &&
							connectionRequest.getApprovingCloudNumber().equals(cloudNumber)){
						isApprovalRequired = true;
					}	
					LOGGER.info("Adding connection request to view list");
					
					Connection connection = new ConnectionImpl(cloud1, cloud2, isApprovalRequired, 
							isApproved1, isApproved2, isBlocked1, isBlocked2, connectionName, blockedBy1, blockedBy2);
					connectionList.add(connection);
					
					cloudSet.add(cloud2.toString());
				}
			}
			
			if(cloudSet != null && cloudSet.size() >= 1){								
				
				ConnectionProfileDAO profileDAO = new ConnectionProfileDAOImpl();
				List<ConnectionProfile> profiles = profileDAO.viewConnections(cloudSet);
				
				for (Object obj : connectionList) {
					
					ConnectionImpl connection = (ConnectionImpl)obj;
					
					for (Object profile : profiles){
						
						ConnectionProfile connectionProfile = (ConnectionProfile)profile;
						
						if(connectionProfile.getCloudNumber().equals(connection.getChild2().toString())){
							
							connection.setFirstName(connectionProfile.getFirstName());
							connection.setLastName(connectionProfile.getLastName());
							connection.setNickName(connectionProfile.getNickName());
							connection.setAvatar(connectionProfile.getAvatar());
							break;
						}
					}
				}
			}
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while viewing connection as cloud: {}", chatException);
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while viewing connection as cloud: {}", ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.info("Exit viewConnectionsAsCloud with cloud: {}", cloud);
		Connection[] connections = new ConnectionImpl[connectionList.size()]; 
		return connectionList.toArray(connections);			
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#logsConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Log[] logsConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){			

		LOGGER.info("Enter logsConnection with cloud: {} for cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			Utility.authenticate(cloud.toString(), cloudSecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			
			String cloudNumber = null;
			String cloud1CloudNumber = null;			

			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];				
				}
			}

			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloud1CloudNumber = (String)obj[0];					
				}
			}
			
			
			String cloud1Guardian = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);

			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(cloud1Guardian)){
				LOGGER.info("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage());
			}
			
			LOGGER.info("Getting logs for cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			return CynjaCloudChat.logService.getLogs(new ConnectionImpl(cloud1, cloud2));
		
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while viewing connection logs: {}", chatException);
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while viewing connection logs: {}", ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}				
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#blockConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection blockConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.info("Enter blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			Utility.authenticate(cloud.toString(), cloudSecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());
			
			String cloudNumber = null;
			String cloud1CloudNumber = null;
			String cloud2CloudNumber = null;
			
			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloud1CloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList2 != null && cloudList2.size() > 0){
				Object[] obj = cloudList2.get(0);
				if(obj != null && obj.length > 0){
					cloud2CloudNumber = (String)obj[0];					
				}
			}
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.info("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.info("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage());
			}						
			
			LOGGER.info("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
						
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.info("Connection request not found.");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber();
					String deleteRenew = connectionRequest.getDeleteRenew();
										
					String newStatus = null;
					
					if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){
						
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())){
							
							LOGGER.info("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
							
						}else if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							
							LOGGER.info("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(),ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							LOGGER.info("Connection is already blocked");
							throw new ChatValidationException(ChatErrors.ALREADY_BLOCKED.getErrorCode(),ChatErrors.ALREADY_BLOCKED.getErrorMessage());
							
						}else if(status.equals(Status.APPROVED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_REQUESTER.getStatus();
							connectionRequest.setBlockedByRequester(cloudNumber);
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							newStatus = Status.BLOCKED.getStatus();
							connectionRequest.setBlockedByRequester(cloudNumber);
							
						}else{
							
							LOGGER.info("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(),ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}

					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){
						
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							
							LOGGER.info("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
							
						}else if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
							
							LOGGER.info("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}						
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							LOGGER.info("Connection is already blocked");
							throw new ChatValidationException(ChatErrors.ALREADY_BLOCKED.getErrorCode(),ChatErrors.ALREADY_BLOCKED.getErrorMessage());
							
						}else if(status.equals(Status.APPROVED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_ACCEPTOR.getStatus();
							connectionRequest.setBlockedByAcceptor(cloudNumber);
							
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							newStatus = Status.BLOCKED.getStatus();
							connectionRequest.setBlockedByAcceptor(cloudNumber);
							
						}else{
							
							LOGGER.info("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}
					}
					
					if(newStatus != null){
						
						LOGGER.info("Updating connection request with new status: {}", newStatus);
						connectionRequest.setStatus(newStatus);						
						connectionRequestDAO.updateRequest(connectionRequest);
					}
				}
			}			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while blocking connection: {}",chatException);
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while blocking connection: {}",ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.info("Exit blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#unblockConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection unblockConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.info("Enter unblockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {			
			
			Utility.authenticate(cloud.toString(), cloudSecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());
			
			String cloudNumber = null;
			String cloud1CloudNumber = null;
			String cloud2CloudNumber = null;
			
			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloud1CloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList2 != null && cloudList2.size() > 0){
				Object[] obj = cloudList2.get(0);
				if(obj != null && obj.length > 0){
					cloud2CloudNumber = (String)obj[0];					
				}
			}
			
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloudNumber);
			String guardian1CloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.info("Checking if cloud: {} is a dependent cloud", cloud.toString());
			if(guardianCloudNumber!=null && !guardianCloudNumber.equals("")){
				
				LOGGER.info("You are not authorized to unblock a connection");
				throw new ChatValidationException(ChatErrors.NOT_AUTHORIZED_TO_UNBLOCK.getErrorCode(),ChatErrors.NOT_AUTHORIZED_TO_UNBLOCK.getErrorMessage());
			}
			
			LOGGER.info("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardian1CloudNumber)){
				LOGGER.info("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage());
			}													
			
			LOGGER.info("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.info("Connection request not found");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber();
					String deleteRenew = connectionRequest.getDeleteRenew();

					String newStatus = null;
					if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())){
							
							LOGGER.info("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
						}
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							newStatus = Status.APPROVED.getStatus();
							connectionRequest.setBlockedByRequester(null);
							
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_ACCEPTOR.getStatus();
							connectionRequest.setBlockedByRequester(null);
							
						}else{
							
							LOGGER.info("Connection can not be unblocked until blocked");
							throw new ChatValidationException(ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorMessage());
						}
					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){

						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							LOGGER.info("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
						}
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							newStatus = Status.APPROVED.getStatus();
							connectionRequest.setBlockedByAcceptor(null);
							
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_REQUESTER.getStatus();
							connectionRequest.setBlockedByAcceptor(null);
							
						}else{
							
							LOGGER.info("Connection can not be unblocked until blocked");
							throw new ChatValidationException(ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorMessage());
						}
					}
					
					if(newStatus!=null){
						
						LOGGER.info("Updating connection request with new status: {}", newStatus);
						connectionRequest.setStatus(newStatus);
						connectionRequestDAO.updateRequest(connectionRequest);
					}
				}
			}			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while unblocking connection: {}",chatException);
			throw chatException;
			
		}catch (Exception ex) {

			LOGGER.error("Error while unblocking connection: {}",ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		LOGGER.info("Exit unblockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#deleteConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection deleteConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.info("Enter deleteConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			Utility.authenticate(cloud.toString(), cloudSecretToken);
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());
			
			String cloudNumber = null;
			String cloud1CloudNumber = null;
			String cloud2CloudNumber = null;
			
			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloud1CloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList2 != null && cloudList2.size() > 0){
				Object[] obj = cloudList2.get(0);
				if(obj != null && obj.length > 0){
					cloud2CloudNumber = (String)obj[0];					
				}
			}
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			LOGGER.info("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.info("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage());
			}
						
			LOGGER.info("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.info("Connection request not found");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;
					String status = connectionRequest.getStatus();
					String deleteRenew = connectionRequest.getDeleteRenew();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();

					if(deleteRenew == null && (status.equals(Status.NEW.getStatus()) || status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus()) 
							|| status.equals(Status.CHILD_APPROVAL_PENDING.getStatus()))){
						
						LOGGER.info("Deleteing connection reuest from DB");
						connectionRequestDAO.deleteRequest(connectionRequest);		
						
					}									
					else if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){														

						if (deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){

							LOGGER.info("Deleteing connection reuest from DB");
							connectionRequestDAO.deleteRequest(connectionRequest);

						}else if(deleteRenew==null || (deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew()))){

							LOGGER.info("Deleting connection request by reuquester");
							connectionRequest.setDeleteRenew(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew());
							
							if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
								connectionRequest.setStatus(Status.APPROVED.getStatus());
							}else if(status.equals(Status.BLOCKED.getStatus())){
								connectionRequest.setStatus(Status.BLOCKED_BY_ACCEPTOR.getStatus());
							}else if(status.equals(Status.NEW.getStatus())){
								connectionRequest.setStatus(Status.CLOUD_APPROVAL_PENDING.getStatus());
							}
							connectionRequest.setBlockedByRequester(null);
							connectionRequestDAO.updateRequest(connectionRequest);

						}else if (deleteRenew != null && (deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew()))){

							String preReqConnectionName = connectionRequest.getRequestingConnectionName();
							String preAccConnectionName = connectionRequest.getAcceptingConnectionName();

							connectionRequestDAO.deleteRequest(connectionRequest);

							ConnectingClouds connectingClouds = new ConnectingClouds(acceptingCloudNumber, requestingCloudNumber);

							connectionRequest.setConnectingClouds(connectingClouds);								
							connectionRequest.setApprovingCloudNumber(EntityUtil.getGuardianCloudNumber(acceptingCloudNumber));
							connectionRequest.setRequestingConnectionName(preAccConnectionName);
							connectionRequest.setAcceptingConnectionName(preReqConnectionName);
							connectionRequest.setStatus(Status.NEW.getStatus());
							connectionRequest.setDeleteRenew(null);

							connectionRequestDAO.requestConnection(connectionRequest);
						}else{

							LOGGER.info("Connection Not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(),ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
						}

					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){

						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())){

							LOGGER.info("Deleteing connection reuest from DB");
							connectionRequestDAO.deleteRequest(connectionRequest);

						}else if(deleteRenew==null || (deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew()))){

							LOGGER.info("Deleting connection request by acceptor");
							connectionRequest.setDeleteRenew(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew());
							
							if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
								connectionRequest.setStatus(Status.APPROVED.getStatus());
							}else if(status.equals(Status.BLOCKED.getStatus())){
								connectionRequest.setStatus(Status.BLOCKED_BY_REQUESTER.getStatus());
							}
							connectionRequest.setBlockedByAcceptor(null);
							connectionRequestDAO.updateRequest(connectionRequest);	

						}else if (deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){

							connectionRequest.setApprovingCloudNumber(EntityUtil.getGuardianCloudNumber(requestingCloudNumber));
							connectionRequest.setStatus(Status.NEW.getStatus());
							connectionRequest.setDeleteRenew(null);

							connectionRequestDAO.updateRequest(connectionRequest);
						}else{

							LOGGER.info("Connection Not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
						}							
					}																		
				}
			}						
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while deleting connection: {}",chatException);
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while deleting connection: {}",ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.info("Exit deleteConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#findConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress)
	 */
	public Connection findConnection(XDIAddress cloud1, String cloud1SecretToken, XDIAddress cloud2){
		LOGGER.info("Enter findConnection with requestingCloud: {}, acceptingCloud: {}", cloud1, cloud2);
		Connection connection = null;
		try {
			Utility.authenticate(cloud1.toString(), cloud1SecretToken);			  
			EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();			
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());
			
			String cloudNumber1 = null;
			String cloudNumber2 = null;
			
			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber1 = (String)obj[0];					
				}
			}
			
			if(cloudList2 != null && cloudList2.size() > 0){
				Object[] obj = cloudList2.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber2 = (String)obj[0];					
				}
			}
			
			LOGGER.info("Getting connection request between cloud1: {}, cloud2: {}", cloudNumber1, cloudNumber2);
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();

			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloudNumber1.toString(), cloudNumber2.toString());
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.info("Connection request not found");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}
									
			ConnectionRequest connectionRequest = (ConnectionRequest)connectionRequestList.get(0);

			String status = connectionRequest.getStatus();
			String deleteRenew = connectionRequest.getDeleteRenew();

			boolean isApprovalReq = false;
			boolean approved1 = true;
			boolean blocked1 = false;
			boolean approved2 = true;
			boolean blocked2 = false;
			String blockedBy1 = null;
			String blockedBy2 = null;			

			if(status.equals(Status.NEW.getStatus()) || status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus())
					|| status.equals(Status.CHILD_APPROVAL_PENDING.getStatus())){

				approved1 = false;
				approved2 = false;
			}


			if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber())){				
				
				if(deleteRenew != null){

					if(deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew()) 
							|| deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){						
						return null;
					}else{
						approved2 = false;
					}
				}

				if(status.equals(Status.BLOCKED.getStatus())){
					
					blocked1 = true;
					blocked2 = true;
					blockedBy1 = connectionRequest.getBlockedByRequester();
					blockedBy2 = connectionRequest.getBlockedByAcceptor();
					
				}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
					
					blocked1 = true;
					blockedBy1 = connectionRequest.getBlockedByRequester();
					
				}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
					
					blocked2 = true;
					blockedBy2 = connectionRequest.getBlockedByAcceptor();
				}
			}else if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getAcceptingCloudNumber())){

				if(deleteRenew != null){
					if(deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew()) 
							|| deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){						
						return null;
					}else{
						approved2 = false;
					}
				}

				if(status.equals(Status.BLOCKED.getStatus())){
					
					blocked1 = true;
					blocked2 = true;
					blockedBy1 = connectionRequest.getBlockedByAcceptor();
					blockedBy2 = connectionRequest.getBlockedByRequester();
										
				}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
					
					blocked1 = true;
					blockedBy1 = connectionRequest.getBlockedByAcceptor();
					
				}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
					
					blocked2 = true;
					blockedBy2 = connectionRequest.getBlockedByRequester();
				}
			}
			
			CloudName connectionName = null;
			if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber())){
				connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
			}else{
				connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
			}						
				
			connection = new ConnectionImpl(Utility.createXDIAddress(cloudNumber1), Utility.createXDIAddress(cloudNumber2), isApprovalReq, approved1, approved2, 
					blocked1, blocked2, connectionName, blockedBy1, blockedBy2);					
			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while finding connection: {}",chatException);
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while finding connection: {}",ex);
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}

		return connection;		
	}

    @Override
    public List<ChatMessage> chatHistory(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1,
            XDIAddress cloud2, QueryInfo queryInfo) {
       LOGGER.info("Enter logsConnection with cloud: {} for cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
       try {

            Utility.authenticate(cloud.toString(), cloudSecretToken);
    	    EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();
			List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());
			List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());
			
			String cloudNumber = null;
			String cloud1CloudNumber = null;
			String cloud2CloudNumber = null;
			
			if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList1 != null && cloudList1.size() > 0){
				Object[] obj = cloudList1.get(0);
				if(obj != null && obj.length > 0){
					cloud1CloudNumber = (String)obj[0];					
				}
			}
			
			if(cloudList2 != null && cloudList2.size() > 0){
				Object[] obj = cloudList2.get(0);
				if(obj != null && obj.length > 0){
					cloud2CloudNumber = (String)obj[0];					
				}
			}
           String cloud1Guardian = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);      
           
           if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(cloud1Guardian)){
               LOGGER.info("Invalid cloud1 provided");
               throw new ChatValidationException(ChatErrors.INVALID_CLOUD_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD_PROVIDED.getErrorMessage());
           }
           
           LOGGER.info("Getting logs for cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
           List<ChatMessage> chatMessageList = CynjaCloudChat.logService.getChatHistory(new ConnectionImpl(Utility.createXDIAddress(cloud1CloudNumber), 
        		   Utility.createXDIAddress(cloud2CloudNumber)), queryInfo);

           return chatMessageList;
       
       }catch (ChatValidationException chatException) {

           LOGGER.error("Error while viewing connection logs: {}", chatException);
           throw chatException;
       }catch (Exception ex) {

           LOGGER.error("Error while viewing connection logs: {}", ex);
           throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
       }               
    }

    /* (non-Javadoc)
     * @see biz.neustar.clouds.chat.service.ConnectionService#notifications(xdi2.core.syntax.XDIAddress, java.lang.String)
     */
    @Override
    public Connection[] notifications(XDIAddress cloud, String cloudSecretToken) {
    	 LOGGER.info("Enter notifications of cloud: {}", cloud);
    	 List<Connection> connectionList = new ArrayList<Connection>();
         try {

             Utility.authenticate(cloud.toString(), cloudSecretToken);
             
             EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();             
 			 List<Object[]> cloudList = entityDAO.findByCloud(cloud.toString());
 			 String cloudNumber = null;			 
			
			 if(cloudList != null && cloudList.size() > 0){
				Object[] obj = cloudList.get(0);
				if(obj != null && obj.length > 0){
					cloudNumber = (String)obj[0];					
				}
			 }
             
             String allCloudNumbers = "";

             List<String> dependentCloudNumbers =  entityDAO.findDependentByGuardian(cloudNumber);

             dependentCloudNumbers.add(cloudNumber);

             Iterator<String> itr = dependentCloudNumbers.iterator();

             while(itr.hasNext()){
            	 allCloudNumbers+= "'"+itr.next()+"',";
             }

             allCloudNumbers = allCloudNumbers.substring(0, allCloudNumbers.length()-1);

             LOGGER.info("Getting unread message notification for cloud: {}", allCloudNumbers);
 			 ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();

 			 List<Object[]> connectionRequestList = connectionRequestDAO.getNotification(allCloudNumbers);

             if(connectionRequestList == null || connectionRequestList.size()==0){

 				LOGGER.info("No connection request found for cloud: {}", cloud.toString());
 				return new ConnectionImpl[0];
 			}

            int size = connectionRequestList.size();

 			for (int i = 0;i < size; i++) {

 				Object obj[] = connectionRequestList.get(i);

 				XDIAddress cloud1 = null;
 				XDIAddress cloud2 = null;
 				CloudName connectionName = null;

 				String requestingCloudNumber = (String)obj[0];
 				String acceptingCloudNumber = (String)obj[1];
 				String requestingConnectionName = (String)obj[2];
 				String acceptingConnectionName = (String)obj[3];
 				String status = (String)obj[4];
 				String deleteRenew =  (String)obj[5];
 				String messageBy = (String)obj[6];

 				if (dependentCloudNumbers.contains(requestingCloudNumber) && !messageBy.equals(requestingConnectionName)
 						&& !messageBy.equals(requestingCloudNumber)){

 					if(deleteRenew !=null && (deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew()) 
 							|| deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew()))){
 						LOGGER.info("Do not add unread message notification to view list if connection request is deleted by requester");
 						continue;
 					}

 					if(status.equals(Status.BLOCKED) || status.equals(Status.BLOCKED_BY_REQUESTER)){
 						LOGGER.info("Do not add unread message notification to view list if connection request is blocked by requester");
 						continue;
 					}

 					cloud1 = Utility.createXDIAddress(requestingCloudNumber);
 					cloud2 = Utility.createXDIAddress(acceptingCloudNumber);
 					connectionName = CloudName.create(acceptingConnectionName);
 				}

 				if (dependentCloudNumbers.contains(acceptingCloudNumber) && !messageBy.equals(acceptingConnectionName)
 						&& !messageBy.equals(acceptingCloudNumber)){

 					if(deleteRenew !=null && (deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew()) 
 							|| deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew()))){
 						LOGGER.info("Do not add unread message notification to view list if connection request is deleted by requester");
 						continue;
 					}

 					if(status.equals(Status.BLOCKED) || status.equals(Status.BLOCKED_BY_REQUESTER)){
 						LOGGER.info("Do not add unread message notification to view list if connection request is blocked by requester");
 						continue;
 					}

 					cloud1 = Utility.createXDIAddress(acceptingCloudNumber);
 					cloud2 = Utility.createXDIAddress(requestingCloudNumber);
 					connectionName = CloudName.create(requestingConnectionName);
 				}

 				Connection connection = new ConnectionImpl(cloud1, cloud2, connectionName);
 				connectionList.add(connection);
 			}

             Connection[] connections = new ConnectionImpl[connectionList.size()];
     		 return connectionList.toArray(connections);

         }catch (ChatValidationException chatException) {

             LOGGER.error("Error while viewing connection logs: {}", chatException);
             throw chatException;
         }catch (Exception ex) {

             LOGGER.error("Error while viewing connection logs: {}", ex);
             throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
         }
    }

    /* (non-Javadoc)
     * @see biz.neustar.clouds.chat.service.ConnectionService#updateChatStatus(xdi2.core.syntax.XDIAddress)
     */
    @Override
    public void updateChatStatus(XDIAddress cloud1, List<ChatMessage> chatMessageList) {
    	LOGGER.info("Enter updateChatStatus of cloud: {}", cloud1.toString());

    	String cloudNumber1 = null;
    	List<Integer> chatHistoryId = new ArrayList<Integer>();

    	for(ChatMessage chatMessage : chatMessageList){

			if(chatMessage.getStatus().equals(MessageStatus.READ.getStatus())){
				continue;
			}

			LOGGER.info("a unread message found chat_history_id: {}", chatMessage.getChatHistoryId());
			if(cloudNumber1 == null){
				EntityCloudDAO entityDAO = new EntityCloudHibernateDAO();             
	 			List<Object[]> cloudList1 = entityDAO.findByCloud(cloud1.toString());	 						
				
				if(cloudList1 != null && cloudList1.size() > 0){
					Object[] obj = cloudList1.get(0);
					if(obj != null && obj.length > 0){
						cloudNumber1 = (String)obj[0];					
					}
				}

				XDIAddress cloud2 = Utility.createXDIAddress(chatMessage.getMessageBy());
				String cloudNumber2 = null;
				
				List<Object[]> cloudList2 = entityDAO.findByCloud(cloud2.toString());	 						
				
				if(cloudList2 != null && cloudList2.size() > 0){
					Object[] obj = cloudList2.get(0);
					if(obj != null && obj.length > 0){
						cloudNumber2 = (String)obj[0];					
					}
				}

				if(cloudNumber1.equals(cloudNumber2)){
					LOGGER.info("cloud: {} is not having any unread message, so exiting");
					break;
				}
			}
			chatHistoryId.add(chatMessage.getChatHistoryId());
    	}

    	if(chatHistoryId.size() > 0){
    		LOGGER.info("Cloud: {} is having unread messages :{}",chatHistoryId.toArray());
    		Integer[] arrChatHistoryId = new Integer[chatHistoryId.size()];
    		CynjaCloudChat.logService.updateMessageStatus(chatHistoryId.toArray(arrChatHistoryId));
    	}
    }
}