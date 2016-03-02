/**
 * 
 */
package net.rn.clouds.chat.service.impl;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import net.rn.clouds.chat.constants.ChatErrors;
import net.rn.clouds.chat.constants.DeleteRenew;
import net.rn.clouds.chat.constants.Status;
import net.rn.clouds.chat.dao.ConnectionRequestDAO;
import net.rn.clouds.chat.dao.impl.ConnectionRequestDAOImpl;
import net.rn.clouds.chat.exceptions.ChatSystemException;
import net.rn.clouds.chat.exceptions.ChatValidationException;
import net.rn.clouds.chat.model.ConnectingClouds;
import net.rn.clouds.chat.model.ConnectionRequest;
import net.rn.clouds.chat.util.EntityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryResult;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.InitFilter;
import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.service.ConnectionService;

/**
 * @author Noopur Pandey
 *
 */
public class ConnectionServiceImpl implements ConnectionService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionServiceImpl.class);
	
	private XDIDiscoveryResult getXDIDiscovery(XDIAddress cloud){
		
		LOGGER.debug("Getting discovery of cloud1: {}", cloud.toString());
		if(cloud != null){
			try{
				XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
				if (cloudDiscovery == null|| cloudDiscovery.toString().equals("null (null)")){

					LOGGER.debug("{} not found", cloud.toString());
					throw new ChatValidationException(ChatErrors.CLOUD_NOT_FOUND.getErrorCode(), cloud.toString()+ChatErrors.CLOUD_NOT_FOUND.getErrorMessage());
				}
				return cloudDiscovery;
			}catch(Xdi2ClientException clientExcption){
				
				throw new ChatValidationException(ChatErrors.CLOUD_NOT_FOUND.getErrorCode(), cloud.toString()+ChatErrors.CLOUD_NOT_FOUND.getErrorMessage());
			}
		}
		return null;		
	}
	
	private XDIDiscoveryResult authenticate(XDIAddress cloud, String cloudSecretToken){
		
		XDIDiscoveryResult cloudDiscovery = getXDIDiscovery(cloud);
		
		LOGGER.debug("Authenticating cloud: {}",cloud.toString());
		try{
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);	

			if (cloudPrivateKey == null){

				LOGGER.debug("{} private key not found", cloud.toString());
				throw new ChatValidationException(ChatErrors.AUTHENTICATOION_FAILED.getErrorCode(), ChatErrors.AUTHENTICATOION_FAILED.getErrorMessage()+cloud.toString());
			}
		}
		catch(Xdi2ClientException | GeneralSecurityException ex){		
			throw new ChatValidationException(ChatErrors.AUTHENTICATOION_FAILED.getErrorCode(), ChatErrors.AUTHENTICATOION_FAILED.getErrorMessage()+cloud.toString());			
		}
				
		return cloudDiscovery;
	}

	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#requestConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress)
	 */
	@Override
	public Connection requestConnection(XDIAddress cloud1, String cloud1SecretToken, XDIAddress cloud2){
		
		LOGGER.debug("Enter requestConnection with requestingCloud: {}, acceptingCloud: {}", cloud1, cloud2);
		try {
			
			if(cloud1.toString().equals(cloud2.toString())){
				
				LOGGER.debug("Invalid connection requested between {} and {}",cloud1.toString(), cloud2.toString());
				throw new ChatValidationException(ChatErrors.INVALID_CONNECTION_REQUEST.getErrorCode(), ChatErrors.INVALID_CONNECTION_REQUEST.getErrorMessage());
			}
						  
			XDIDiscoveryResult cloud1Discovery = authenticate(cloud1, cloud1SecretToken);
			XDIDiscoveryResult cloud2Discovery = getXDIDiscovery(cloud2);
						
			String cloud1Number = cloud1Discovery.getCloudNumber().toString();			
			String cloud2Number = cloud2Discovery.getCloudNumber().toString();		
		
			LOGGER.debug("Checking if connection already requested");
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
												
						if((requestingCloudNumber.equals(cloud1Number) && (deleteRenew == null || 
								(deleteRenew != null && !deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())))) ||
							(acceptingCloudNumber.equals(cloud1Number) && !status.equals(Status.NEW.getStatus()) && 
								(deleteRenew == null || (deleteRenew != null && !deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew()))))){
							
								LOGGER.debug("Connection already requested between {} and {}", cloud1.toString(), cloud2.toString());
								throw new ChatValidationException(ChatErrors.CONNECTION_ALREADY_EXISTS.getErrorCode(),ChatErrors.CONNECTION_ALREADY_EXISTS.getErrorMessage());
						} 
						
						//Request has been deleted from one of the cloud
						if(cloudParent.equals("")){
							
							if(acceptingCloudNumber.equals(cloud1Number) && status.equals(Status.NEW.getStatus())){
								LOGGER.debug("Approved from cloud1 and renewed from cloud2 ");
								connectionRequest.setStatus(Status.APPROVED.getStatus());
								connectionRequest.setApprovingCloudNumber(null);
								connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew());
							}else{
								LOGGER.debug("Revert the deletion ");
								connectionRequest.setDeleteRenew(null);
							}
															
						}else{
							
							LOGGER.debug("Raise a request to parent to revert the deletion ");
							if(requestingCloudNumber.equals(cloud1Number)){ 

								connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew());
								
							}else if(acceptingCloudNumber.equals(cloud1Number)){
																
								connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew());								
							}														
						}
						connectionRequestDAO.updateRequest(connectionRequest);						
					}
				}
			}else{
				//Request does not exists, so raise a new request
				String approvingCloudNumber = cloudParent;
				String status = Status.CLOUD_APPROVAL_PENDING.getStatus();

				LOGGER.debug("Checking if requester is a dependent cloud");			
				if(!cloudParent.equals("")){

					approvingCloudNumber = cloudParent;
					status = Status.NEW.getStatus();
				}else{

					LOGGER.debug("Checking if acceptor is a dependent cloud");
					String acceptorCloudParent = EntityUtil.getGuardianCloudNumber(cloud2Number);
					if(!acceptorCloudParent.equals("")){
						approvingCloudNumber = acceptorCloudParent;
					}else{
						approvingCloudNumber = cloud2Number;
					}
				}
				LOGGER.debug("Approving cloud: {}, status: {}", approvingCloudNumber, status);

				LOGGER.debug("Creating new connection request");
				ConnectingClouds connectingClouds = new ConnectingClouds(
						cloud1Number, cloud2Number);
				ConnectionRequest connectionRequest = new ConnectionRequest();

				connectionRequest.setConnectingClouds(connectingClouds);
				connectionRequest.setRequestingConnectionName(cloud1.toString());
				connectionRequest.setAcceptingConnectionName(cloud2.toString());
				connectionRequest.setApprovingCloudNumber(approvingCloudNumber);
				connectionRequest.setStatus(status);

				LOGGER.debug("Saving new connection request");
				connectionRequestDAO.requestConnection(connectionRequest);
			}
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while requesting connection: {}", chatException.getMessage());
			throw chatException;
			
		}catch (Exception ex) {

			LOGGER.error("Error while requesting connection: {}", ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		LOGGER.debug("Exit requestConnection with cloud1: {}, cloud2: {}", cloud1, cloud2);				
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#approveConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection approveConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){	
		
		LOGGER.debug("Enter approveConnection with approverCloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			 
			XDIDiscoveryResult approverDiscovery = authenticate(cloud, cloudSecretToken);			
			XDIDiscoveryResult cloud1Discovery = getXDIDiscovery(cloud1);			
			XDIDiscoveryResult cloud2Discovery = getXDIDiscovery(cloud2);			
			
			String cloudNumber = approverDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorMessage());
			}							
			
			LOGGER.debug("Getting connection request");
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);						
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){

					ConnectionRequest connectionRequest = (ConnectionRequest)obj;
					
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();
					
										
					String deleteRenew = connectionRequest.getDeleteRenew();
					String status = connectionRequest.getStatus();
					String newStatus = status;
					String newApprover = null;
										
					if(deleteRenew != null && !guardianCloudNumber.equals("") && 
						((cloud1CloudNumber.equals(requestingCloudNumber) && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())) ||
						cloud1CloudNumber.equals(acceptingCloudNumber) && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew()))){
						
							if(status.equals(Status.NEW.getStatus())){
								
								connectionRequest.setStatus(Status.APPROVED.getStatus());
								connectionRequest.setApprovingCloudNumber(null);
								connectionRequest.setDeleteRenew(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew());
								
							}else{
								connectionRequest.setDeleteRenew(null);
							}
												
					}else{
					
						if(status.equals(Status.APPROVED.getStatus()) || status.equals(Status.BLOCKED.getStatus()) ||
								status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus()) || status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							LOGGER.debug("connection request is already approved");
							throw new ChatValidationException(ChatErrors.ACTION_ALREADY_PERFORMED.getErrorCode(),ChatErrors.ACTION_ALREADY_PERFORMED.getErrorMessage());
						}
						
						if(!cloudNumber.equals(connectionRequest.getApprovingCloudNumber())){
							LOGGER.debug("Cloud: {} is not authorized approver.", cloud.toString());
							throw new ChatValidationException(ChatErrors.UNAUTHORIZED_ACTION.getErrorCode(),ChatErrors.UNAUTHORIZED_ACTION.getErrorMessage());
						}											
						
						String acceptingCloudParent = EntityUtil.getGuardianCloudNumber(acceptingCloudNumber);
						
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
								newStatus = Status.CLOUD_APPROVAL_PENDING.getStatus();
								newApprover = acceptingCloudNumber;
							}
							if(status.equals(Status.CLOUD_APPROVAL_PENDING.getStatus())){

								newStatus = Status.APPROVED.getStatus();
								newApprover = null;
							}
						}
						connectionRequest.setApprovingCloudNumber(newApprover);
						connectionRequest.setStatus(newStatus);
					}

					LOGGER.debug("Upating connection request");										
					connectionRequestDAO.updateRequest(connectionRequest);					
				}
			}													
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while approving connection: {}", chatException.getMessage());
			throw chatException;
			
		}catch (Exception ex) {

			LOGGER.error("Error while approving connection: {}", ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.debug("Exit approveConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);		
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#viewConnectionsAsParent(xdi2.core.syntax.XDIAddress, java.lang.String)
	 */
	public Connection[] viewConnectionsAsParent(XDIAddress parent, String parentSecretToken){
		
		LOGGER.debug("Enter viewConnectionsAsParent with parent: {} ", parent);		
		ConnectionImpl[] connection = null;		
		try {
			
			XDIDiscoveryResult parentDiscovery = authenticate(parent, parentSecretToken);
				
			LOGGER.debug("Getting all children of parent cloud: {}",parent.toString());
			XDIAddress[] children = CynjaCloudChat.parentChildService.getChildren(parent, parentSecretToken);			
												
			List<String> collection = new ArrayList<String>();
			for (XDIAddress child : children) {
				
				LOGGER.debug("Getting discovery of child cloud: {}", child.toString());
				XDIDiscoveryResult childDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(child, null);
				
				LOGGER.debug("Adding child: {} to list", childDiscovery.getCloudNumber().toString());
				collection.add(childDiscovery.getCloudNumber().toString());			
			}
			
			LOGGER.debug("Getting connection requests of children of parent cloud:{} ",parent.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.viewConnections(collection);
						
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				return new ConnectionImpl[0];				
			}
			
			connection = new ConnectionImpl[connectionRequestList.size()];
			int clounter = 0;
			
			for (Object obj : connectionRequestList) {
				
				if(obj instanceof ConnectionRequest){
					
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;					

					XDIAddress child1;
					XDIAddress child2;																																								
					CloudName connectionName;
					boolean isBlocked1 = false;
					boolean isBlocked2 = false;
					boolean isApproved1 = false;
					boolean isApproved2 = false;
					
					String deleteRenew = connectionRequest.getDeleteRenew();
					String status = connectionRequest.getStatus();
					boolean isApprovalRequired = false;														
					
					
					if (collection.contains(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString())){
												
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())){
							LOGGER.debug("Do not add connection request to view list if connection request is deleted by requester");
							continue;
						}
						
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							isApprovalRequired = true;
						}
						child1 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						child2 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());												
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.debug("Checking ig connection is blocked by requester");
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							isBlocked1 = true;
							isApproved2 = true;
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							isBlocked2 = true;
							isApproved1 = true;
						}
						
						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew != null && 
								!deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							
							isApproved1 = true;							
						}
					}else{
						
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							LOGGER.debug("Do not add connection request to view list if connection request is deleted by acceptor");
							continue;
						}
						
						if(status.equals(Status.NEW.getStatus()) && deleteRenew == null){
							LOGGER.debug("Do not add connection request to view list if connection request has not been approved by requester guardian");
							continue;
						}
						
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
							isApprovalRequired = true;
						}
						
						child1 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						child2 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						
						LOGGER.debug("Checking if connection is blocked by acceptor"); 
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							isBlocked1 = true;
							isApproved2 = true;
							
						}if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							isBlocked2 = true;
							isApproved1 = true;
						}
						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew != null && 
								!deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
								
								isApproved1 = true;
							}
					}
						
					if(status.equals(Status.APPROVED.getStatus()) && deleteRenew == null){						
							
						isApproved1 = true;
						isApproved2 = true;						
					}
									
					if(connectionRequest.getApprovingCloudNumber() != null && 
							connectionRequest.getApprovingCloudNumber().equals(parentDiscovery.getCloudNumber().toString())){
						isApprovalRequired = true;
					}		
					
					LOGGER.debug("Adding connection request to view list");
					
					connection[clounter++] = new ConnectionImpl(child1, child2, isApprovalRequired, isApproved1, 
							isApproved2, isBlocked1, isBlocked2, connectionName);
				}					
			}				
			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while viewing connection as parent: {}", chatException.getMessage());
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while viewing connection as parent: {}", ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.debug("Exit viewConnectionsAsParent with parent: {} ", parent);
		return connection;		
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#viewConnectionsAsChild(xdi2.core.syntax.XDIAddress, java.lang.String)
	 */
	public Connection[] viewConnectionsAsChild(XDIAddress cloud, String cloudSecretToken){
		
		LOGGER.debug("Enter viewConnectionsAsChild with cloud: {}", cloud);
		
		ConnectionImpl[] connection = null;		

		try {
			
			XDIDiscoveryResult cloudDiscovery = authenticate(cloud, cloudSecretToken);
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();						
			
			List<String> collection = new ArrayList<String>();
			collection.add(cloudNumber);
			
			LOGGER.debug("Getting connection requests of cloud: {}", cloud.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.viewConnections(collection);			
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("No connection request found for cloud: {}", cloud.toString());
				return new ConnectionImpl[0];
			}
				
			connection = new ConnectionImpl[connectionRequestList.size()];
			int counter = 0;
			
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
					
					String deleteRenew = connectionRequest.getDeleteRenew();
					String status = connectionRequest.getStatus();
					
					if (cloudNumber.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString())){
						
						if(deleteRenew !=null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())){
							LOGGER.debug("Do not add connection request to view list if connection request is deleted by requester");
							continue;
						}
						
						cloud1 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						cloud2 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.debug("Checking if connection request has been blocked by cloud: {}",cloud.toString());
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							isBlocked1 = true;
							isApproved2 = true;
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							isBlocked2 = true;
							isApproved1 = true;
						}
						if(status.equals(Status.APPROVED.getStatus()) && deleteRenew != null && 
								!deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							
							isApproved1 = true;							
						}	
						
					}else{
						
						if(deleteRenew !=null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							LOGGER.debug("Do not add connection request to view list if connection request is deleted by acceptor");
							continue;
						}
										
						if(status.equals(Status.NEW.getStatus())){
							LOGGER.debug("Do not add connection request to view list if connection request has not been approved by requester's guardian");
							continue;
						}
						cloud1 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						cloud2 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						
						LOGGER.debug("Checking if connection request has been blocked by cloud: {}",cloud.toString());
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							isBlocked1 = true;
							isApproved2 = true;
							
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							isBlocked2 = true;
							isApproved1 = true;
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
					LOGGER.debug("Adding connection request to view list");
					connection[counter++] = new ConnectionImpl(cloud1, cloud2, isApprovalRequired, 
							isApproved1, isApproved2, isBlocked1, isBlocked2, connectionName);
				}
			}					
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while viewing connection as cloud: {}", chatException.getMessage());
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while viewing connection as cloud: {}", ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.debug("Exit viewConnectionsAsCloud with cloud: {}", cloud);
		return connection;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#logsConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Log[] logsConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){			

		LOGGER.debug("Enter logsConnection with cloud: {} for cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			
			XDIDiscoveryResult cloudDiscovery = authenticate(cloud, cloudSecretToken);
			XDIDiscoveryResult cloud1Discovery = getXDIDiscovery(cloud1);			
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud1Guardian = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(cloud1Guardian)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorMessage());
			}
			
			LOGGER.debug("Getting logs for cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			return CynjaCloudChat.logService.getLogs(new ConnectionImpl(cloud1, cloud2));
		
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while viewing connection logs");
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while viewing connection logs");
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}				
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#blockConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection blockConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.debug("Enter blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			
			XDIDiscoveryResult cloudDiscovery = authenticate(cloud, cloudSecretToken);
			XDIDiscoveryResult cloud1Discovery = getXDIDiscovery(cloud1)
;			XDIDiscoveryResult cloud2Discovery = getXDIDiscovery(cloud2);
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorMessage());
			}						
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
						
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found.");
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
							
							LOGGER.debug("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
							
						}else if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
							
							LOGGER.debug("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(),ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}
						if(status.equals(Status.APPROVED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_REQUESTER.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							newStatus = Status.BLOCKED.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							LOGGER.debug("Connection is already blocked");
							throw new ChatValidationException(ChatErrors.ACTION_ALREADY_PERFORMED.getErrorCode(),ChatErrors.ACTION_ALREADY_PERFORMED.getErrorMessage());
						}else{
							
							LOGGER.debug("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(),ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}

					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){
						
						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							
							LOGGER.debug("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
							
						}else if(deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
							
							LOGGER.debug("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}						
						if(status.equals(Status.APPROVED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_ACCEPTOR.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							newStatus = Status.BLOCKED.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							LOGGER.debug("Connection is already blocked");
							throw new ChatValidationException(ChatErrors.ACTION_ALREADY_PERFORMED.getErrorCode(),ChatErrors.ACTION_ALREADY_PERFORMED.getErrorMessage());
						}else{
							
							LOGGER.debug("Connection can not be blocked until approved.");
							throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
						}
					}
					
					if(newStatus != null){
						
						LOGGER.debug("Updating connection request with new status: {}", newStatus);
						connectionRequest.setStatus(newStatus);
						connectionRequestDAO.updateRequest(connectionRequest);
					}
				}
			}			
			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while blocking connection: {}",chatException.getMessage());
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while blocking connection: {}",ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.debug("Exit blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);		
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#unblockConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection unblockConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.debug("Enter unblockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {			
			
			XDIDiscoveryResult cloudDiscovery = authenticate(cloud, cloudSecretToken);
			XDIDiscoveryResult cloud1Discovery = getXDIDiscovery(cloud1);
			XDIDiscoveryResult cloud2Discovery = getXDIDiscovery(cloud2);
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloudNumber);
			String guardian1CloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.debug("Checking if cloud: {} is a dependent cloud", cloud.toString());			
			if(guardianCloudNumber!=null && !guardianCloudNumber.equals("")){
				
				LOGGER.debug("You are not authorized to unblock a connection");
				throw new ChatValidationException(ChatErrors.UNAUTHORIZED_ACTION.getErrorCode(),ChatErrors.UNAUTHORIZED_ACTION.getErrorMessage());
			}
			
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardian1CloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorMessage());
			}													
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
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
							
							LOGGER.debug("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
						}
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							newStatus = Status.APPROVED.getStatus();
							
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_ACCEPTOR.getStatus();
							
						}else{
							
							LOGGER.debug("Connection can not be unblocked until blocked");
							throw new ChatValidationException(ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorMessage());
						}
					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){

						if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
							LOGGER.debug("Conenction request not found.");
							throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
						}
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							newStatus = Status.APPROVED.getStatus();
							
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_REQUESTER.getStatus();
						}else{
							
							LOGGER.debug("Connection can not be unblocked until blocked");
							throw new ChatValidationException(ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.BLOCK_THE_CONNECTION_FIRST.getErrorMessage());
						}
					}
					
					if(newStatus!=null){
						
						LOGGER.debug("Updating connection request with new status: {}", newStatus);
						connectionRequest.setStatus(newStatus);
						connectionRequestDAO.updateRequest(connectionRequest);
					}
				}
			}			
			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while unblocking connection: {}",chatException.getMessage());
			throw chatException;
			
		}catch (Exception ex) {

			LOGGER.error("Error while unblocking connection: {}",ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		LOGGER.debug("Exit unblockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);		
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#deleteConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection deleteConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.debug("Enter deleteConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {
			
			XDIDiscoveryResult cloudDiscovery = authenticate(cloud, cloudSecretToken);
			XDIDiscoveryResult cloud1Discovery = getXDIDiscovery(cloud1);
			XDIDiscoveryResult cloud2Discovery = getXDIDiscovery(cloud2);
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new ChatValidationException(ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorCode(),ChatErrors.INVALID_CLOUD1_PROVIDED.getErrorMessage());
			}
						
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String deleteRenew = connectionRequest.getDeleteRenew();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();										
					
					if(status.equals(Status.APPROVED.getStatus()) || status.equals(Status.BLOCKED.getStatus()) || status.equals(Status.BLOCKED_BY_REQUESTER.getStatus()) || status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
						if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){														

							if(deleteRenew==null || deleteRenew.equals("")){
								
								LOGGER.debug("Deleting connection request by reuquester");
								connectionRequest.setDeleteRenew(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew());
								connectionRequestDAO.updateRequest(connectionRequest);
								
							}else if(deleteRenew.equals(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew())){
								
								LOGGER.debug("Deleteing connection reuest from DB");
								connectionRequestDAO.deleteRequest(connectionRequest);								
							}else if (deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_ACCEPTOR.getDeleteRenew())){
							
								String preReqConnectionName = connectionRequest.getRequestingConnectionName();
								String preAccConnectionName = connectionRequest.getAcceptingConnectionName();
								
								connectionRequestDAO.deleteRequest(connectionRequest);
								
								ConnectingClouds connectingClouds = new ConnectingClouds(requestingCloudNumber, acceptingCloudNumber);
								
								connectionRequest.setConnectingClouds(connectingClouds);								
								connectionRequest.setApprovingCloudNumber(EntityUtil.getGuardianCloudNumber(acceptingCloudNumber));
								connectionRequest.setRequestingConnectionName(preAccConnectionName);
								connectionRequest.setAcceptingConnectionName(preReqConnectionName);
								connectionRequest.setStatus(Status.NEW.getStatus());
								connectionRequest.setDeleteRenew(null);
																
								connectionRequestDAO.requestConnection(connectionRequest);
							}else{
								
								LOGGER.debug("Connection is already deleted.");
								throw new ChatValidationException(ChatErrors.ACTION_ALREADY_PERFORMED.getErrorCode(),ChatErrors.ACTION_ALREADY_PERFORMED.getErrorMessage());
							}

						}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){
														
							if(deleteRenew==null || deleteRenew.equals("")){
								
								LOGGER.debug("Deleting connection request by acceptor");
								connectionRequest.setDeleteRenew(DeleteRenew.DELETED_BY_ACCEPTOR.getDeleteRenew());
								connectionRequestDAO.updateRequest(connectionRequest);	
								
							}else if(deleteRenew != null && deleteRenew.equals(DeleteRenew.DELETED_BY_REQUESTER.getDeleteRenew())){
								
								LOGGER.debug("Deleteing connection reuest from DB");
								connectionRequestDAO.deleteRequest(connectionRequest);								
							}else if (deleteRenew != null && deleteRenew.equals(DeleteRenew.RENEWED_BY_REQUESTER.getDeleteRenew())){
											
								connectionRequest.setApprovingCloudNumber(EntityUtil.getGuardianCloudNumber(requestingCloudNumber));
								connectionRequest.setStatus(Status.NEW.getStatus());
								connectionRequest.setDeleteRenew(null);
																
								connectionRequestDAO.updateRequest(connectionRequest);
							}else{
								
								LOGGER.debug("Connection Not found.");
								throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
							}							
						}												
					}else{
						
						LOGGER.debug("A connection can not be deleted until approved.");
						throw new ChatValidationException(ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorCode(), ChatErrors.APPROVE_THE_CONNECTION_FIRST.getErrorMessage());
					}					
				}
			}						
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while deleting connection: {}",chatException.getMessage());
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while deleting connection: {}",ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		
		LOGGER.debug("Exit deleteConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);		
		return new ConnectionImpl(cloud1, cloud2);
	}
	
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#findConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress)
	 */
	public Connection findConnection(XDIAddress cloud1, String cloud1SecretToken, XDIAddress cloud2){
		
		LOGGER.debug("Enter findConnection with requestingCloud: {}, acceptingCloud: {}", cloud1, cloud2);
		Connection connection = null;
		try {
						  
			XDIDiscoveryResult cloud1Discovery = getXDIDiscovery(cloud1);
			XDIDiscoveryResult cloud2Discovery = getXDIDiscovery(cloud2);					
			
			String cloudNumber1 = cloud1Discovery.getCloudNumber().toString();
			String cloudNumber2 = cloud2Discovery.getCloudNumber().toString();
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloudNumber1, cloudNumber2);
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloudNumber1, cloudNumber2);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
				throw new ChatValidationException(ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorCode(), ChatErrors.CONNECTION_REQUEST_NOT_FOUND.getErrorMessage());
			}
									
			ConnectionRequest connectionRequest = (ConnectionRequest)connectionRequestList.get(0);
			
			boolean isApprovalReq = false;
			boolean approved1 = false;
			boolean blocked1 = false;
			
			boolean approved2 = false;
			boolean blocked2 = false;
			
			if(connectionRequest.getDeleteRenew() == null || connectionRequest.getDeleteRenew().equals("")){
				
				String status = connectionRequest.getStatus();
				if(status.equals(Status.APPROVED.getStatus())){
					
					approved1 = true;
					approved2 = true;
					
				}else if(status.equals(Status.BLOCKED.getStatus())){
					blocked1 = true;
					blocked2 = true;
						
				}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
					
					if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber())){
						
						blocked1 = true;
						approved2 = true;																	
					}else{				
						blocked2 = true;
						approved1 = true;
					}
				}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
					
					if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getAcceptingCloudNumber())){
						
						blocked2 = true;
						approved1 = true;
					}else{
						blocked1 = true;
						approved2 = true;
					}					
				}
			}
			
			CloudName connectionName = null;
			if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber())){
				connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
			}else{
				connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
			}						
				
			connection = new ConnectionImpl(cloud1, cloud2, isApprovalReq, approved1, approved2, blocked1, blocked2, connectionName);					
			
		}catch (ChatValidationException chatException) {

			LOGGER.error("Error while finding connection: {}",chatException.getMessage());
			throw chatException;
		}catch (Exception ex) {

			LOGGER.error("Error while finding connection: {}",ex.getMessage());
			throw new ChatSystemException(ChatErrors.SYSTEM_ERROR.getErrorCode(),ChatErrors.SYSTEM_ERROR.getErrorMessage());
		}
		return connection;		
	}
}
