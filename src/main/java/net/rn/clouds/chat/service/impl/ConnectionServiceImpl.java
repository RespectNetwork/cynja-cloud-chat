/**
 * 
 */
package net.rn.clouds.chat.service.impl;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rn.clouds.chat.constants.Deleted;
import net.rn.clouds.chat.constants.Status;
import net.rn.clouds.chat.dao.ConnectionRequestDAO;
import net.rn.clouds.chat.dao.impl.ConnectionRequestDAOImpl;
import net.rn.clouds.chat.model.ConnectingClouds;
import net.rn.clouds.chat.model.ConnectionRequest;
import net.rn.clouds.chat.util.EntityUtil;
import xdi2.client.util.XDIClientUtil;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryResult;
import biz.neustar.clouds.chat.CynjaCloudChat;
import biz.neustar.clouds.chat.InitFilter;
import biz.neustar.clouds.chat.exceptions.ConnectionNotFoundException;
import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.service.ConnectionService;

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
		
		LOGGER.debug("Enter requestConnection with requestingCloud: {}, acceptingCloud: {}", cloud1, cloud2);
		try {
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());  
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null || cloud1Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud1: {} not found", cloud1.toString());
				throw new NullPointerException("Cloud1 not found.");
			}

			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null || cloud2Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud2: {} not found", cloud2.toString());
				throw new NullPointerException("Cloud2 not found.");						
			}
			
			LOGGER.debug("Authenticating cloud1: {}",cloud1.toString());			
			PrivateKey cloud1PrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloud1Discovery.getCloudNumber(), cloud1Discovery.getXdiEndpointUrl(), cloud1SecretToken);
			if (cloud1PrivateKey == null){
				
				LOGGER.debug("Cloud1: {} private key not found", cloud1.toString());
				throw new NullPointerException("Cloud1 private key not found.");
			}
						
			String cloud1Number = cloud1Discovery.getCloudNumber().toString();			
			String cloud2Number = cloud2Discovery.getCloudNumber().toString();		
		
			LOGGER.debug("Checking if connection already requested");
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1Number, cloud2Number);
			
			if(connectionRequestList != null && connectionRequestList.size() == 1 &&
					((ConnectionRequest)connectionRequestList.get(0)).getConnectingClouds().getRequestingCloudNumber().equals(cloud1Number)){
				
				LOGGER.debug("Connection already requested between {} and {}", cloud1.toString(), cloud2.toString());
				throw new Exception("Connection already exists.");
			}
							
			String approvingCloudNumber = "";
			String status = Status.CLOUD_APPROVAL_PENDING.getStatus();
			
			LOGGER.debug("Checking if requester is a dependent cloud");
			String requesterCloudParent = EntityUtil.getGuardianCloudNumber(cloud1Number);
			if(!requesterCloudParent.equals("")){
				
				approvingCloudNumber = requesterCloudParent;
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
		
		} catch (Exception ex) {

			LOGGER.error("Error while requesting connection: {}", ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
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

			LOGGER.debug("Getting discovery of approvingCloud: {}", cloud.toString()); 
			XDIDiscoveryResult approverDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (approverDiscovery == null || approverDiscovery.toString().equals("null (null)")){ 
			
				LOGGER.debug("Approving cloud: {} not found. ",cloud.toString());
				throw new NullPointerException("Parent cloud not found.");
			}

			LOGGER.debug("Authenticating approvingCloud: {}",cloud.toString());
			PrivateKey approverPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(approverDiscovery.getCloudNumber(), approverDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (approverPrivateKey == null){
				
				LOGGER.debug("Approver cloud: {} private key not found.", cloud.toString());
				throw new NullPointerException("Approver cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null || cloud1Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud1 not found");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of Cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null|| cloud2Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud2 is not found");
				throw new NullPointerException("cloud2 not found.");
			}
			
			String cloudNumber = approverDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}							
			
			LOGGER.debug("Getting connection request");
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);						
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
				throw new NullPointerException("Connection request not found.");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){

					ConnectionRequest connectionRequest = (ConnectionRequest)obj;
					
					String status = connectionRequest.getStatus();					
					if(status.equals(Status.APPROVED.getStatus())){
						LOGGER.debug("connection request is already approved");
						throw new Exception("This connection is already approved.");
					}
					
					if(!cloudNumber.equals(connectionRequest.getApprovingCloudNumber())){
						LOGGER.debug("Cloud: {} is not authorized approver.", cloud.toString());
						throw new Exception("You are not authorized to approve the connection.");
					}
					
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();
					String acceptingCloudParent = EntityUtil.getGuardianCloudNumber(acceptingCloudNumber);

					String newApprover = null;
					String newStatus = null;																		

					if(!acceptingCloudParent.equals("")){
																		
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

					LOGGER.debug("Upating connection request");
					connectionRequest.setApprovingCloudNumber(newApprover);
					connectionRequest.setStatus(newStatus);							
					connectionRequestDAO.updateRequest(connectionRequest);					
				}
			}													
		}catch (Exception ex) {

			LOGGER.error("Error while approving connection: {}", ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
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

			LOGGER.debug("Getting discovery of parent cloud: {}", parent.toString());
			XDIDiscoveryResult parentDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(parent, null);
			if (parentDiscovery == null|| parentDiscovery.toString().equals("null (null)")){
				
				LOGGER.debug("Parent cloud not found");
				throw new NullPointerException("Parent not found.");
			}

			LOGGER.debug("Authenticating parent cloud: {}",parent.toString());
			PrivateKey parentPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(parentDiscovery.getCloudNumber(), parentDiscovery.getXdiEndpointUrl(), parentSecretToken);
			if (parentPrivateKey == null){
				
				LOGGER.debug("Parent private key not found");
				throw new NullPointerException("Parent parent key not found.");
			}
				
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
					
					String deleted = connectionRequest.getDeleted();
					String status = connectionRequest.getStatus();
					
					if (collection.contains(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString())){
												
						if(deleted != null && deleted.equals(Deleted.DELETED_BY_REQUESTER.getDeleted())){
							LOGGER.debug("Do not add connection request to view list if connection request is deleted by requester");
							continue;
						}
						child1 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						child2 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());												
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.debug("Checking ig connection is blocked by requester");
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							isBlocked1 = true;
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							isBlocked2 = true;
						}
					}else{
						
						if(deleted != null && deleted.equals(Deleted.DELETED_BY_ACCEPTOR.getDeleted())){
							LOGGER.debug("Do not add connection request to view list if connection request is deleted by acceptor");
							continue;
						}
						
						if(status.equals(Status.NEW.getStatus())){
							LOGGER.debug("Do not add connection request to view list if connection request has not been approved by requester guardian");
							continue;
						}
						child1 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						child2 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						
						LOGGER.debug("Checking if connection is blocked by acceptor"); 
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							isBlocked1 = true;
						}if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							isBlocked2 = true;
						}
					}
					
					boolean isApproved1 = false;
					boolean isApproved2 = false;
					boolean isApprovalRequired = false;
					
					if(status.equals(Status.APPROVED.getStatus())){
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
			
		} catch (Exception ex) {

			LOGGER.error("Error while viewing connection as parent: {}", ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
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

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null|| cloudDiscovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud: {} not found", cloud.toString());
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (cloudPrivateKey == null){
				
				LOGGER.debug("Cloud private key not found");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();						
			
			List<String> collection = new ArrayList<String>();
			collection.add(cloudNumber);
			
			LOGGER.debug("Getting connection requests of cloud: {}", cloud.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.viewConnections(collection);			
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("No connection request not found for cloud: {}", cloud.toString());
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
					
					String deleted = connectionRequest.getDeleted();
					String status = connectionRequest.getStatus();
					
					if (cloudNumber.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString())){
						
						if(deleted !=null && deleted.equals(Deleted.DELETED_BY_REQUESTER.getDeleted())){
							LOGGER.debug("Do not add connection request to view list if connection request is deleted by requester");
							continue;
						}
						
						cloud1 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						cloud2 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.debug("Checking if connection request has been blocked by cloud: {}",cloud.toString());
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							isBlocked1 = true;
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							isBlocked2 = true;
						}
						
					}else{
						
						if(deleted !=null && deleted.equals(Deleted.DELETED_BY_ACCEPTOR.getDeleted())){
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
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							isBlocked2 = true;
						}
					}
					
					boolean isApproved1 = false;
					boolean isApproved2 = false;
					boolean isApprovalRequired = false;
					
					if(status.equals(Status.APPROVED.getStatus())){
						isApproved1 = true;
						isApproved2 = true;
					}
										
					if(connectionRequest.getApprovingCloudNumber() != null &&
							connectionRequest.getApprovingCloudNumber().equals(cloudNumber)){
						isApprovalRequired = true;
					}	
					LOGGER.debug("Adding connection request to view list");
					connection[counter++] = new ConnectionImpl(cloud1, cloud2, isApprovalRequired, 
							isApproved1, isApproved2, isBlocked1, isBlocked2, connectionName);
				}
			}					
		}catch (Exception ex) {
			
			LOGGER.error("Error while viewing connection: {}", ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
		
		LOGGER.debug("Exit viewConnectionsAsChild with cloud: {}", cloud);
		return connection;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#logsConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Log[] logsConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){			

		LOGGER.debug("Enter logsConnection with cloud: {} for cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null|| cloudDiscovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud: {} not found", cloud.toString());
				throw new NullPointerException("Cloud not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null|| cloud1Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud1: {} not found", cloud1.toString());
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null|| cloud2Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud2: {} not found", cloud2.toString());
				throw new NullPointerException("Cloud2 not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (cloudPrivateKey == null){
				
				LOGGER.debug("Cloud private key not found");
				throw new NullPointerException("Cloud private key not found.");
			}
		
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud1Guardian = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(cloud1Guardian)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new Exception("Invalid cloud1 provided");
			}
			
			LOGGER.debug("Getting logs for cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			return CynjaCloudChat.logService.getLogs(new ConnectionImpl(cloud1, cloud2));
		
		}catch (Exception ex) {

			LOGGER.error("Error while viewing connection logs: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
				
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#blockConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection blockConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.debug("Enter blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null|| cloudDiscovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud: {} not found", cloud.toString());
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			
			if (cloudPrivateKey == null){
				
				LOGGER.debug("Cloud private key not found.");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			
			if (cloud1Discovery == null|| cloud1Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud1 not found.");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			
			if (cloud2Discovery == null|| cloud2Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud2 not found.");
				throw new NullPointerException("Cloud2 not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}						
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
						
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Conenction request not found.");
				throw new NullPointerException("Connection request not found.");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber();
					String deleted = connectionRequest.getDeleted();
										
					String newStatus = null;
					
					if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){
						
						if(deleted != null && deleted.equals(Deleted.DELETED_BY_REQUESTER.getDeleted())){
							
							LOGGER.debug("Conenction request not found.");
							throw new Exception("Connection request not found.");
						}
						if(status.equals(Status.APPROVED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_REQUESTER.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							newStatus = Status.BLOCKED.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							LOGGER.debug("Connection is already blocked");
							throw new Exception("Connection is already blocked");
						}else{
							
							LOGGER.debug("Connection can not be blocked until approved.");
							throw new Exception("Connection can not be blocked until approved.");
						}

					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){
						
						if(deleted != null && deleted.equals(Deleted.DELETED_BY_ACCEPTOR.getDeleted())){
							LOGGER.debug("Conenction request not found.");
							throw new Exception("Connection request not found.");
						}
						
						if(status.equals(Status.APPROVED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_ACCEPTOR.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							newStatus = Status.BLOCKED.getStatus();
							
						}else if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							LOGGER.debug("Connection is already blocked");
							throw new Exception("Connection is already blocked");
						}else{
							
							LOGGER.debug("Connection can not be blocked until approved.");
							throw new Exception("Connection can not be blocked until approved.");
						}
					}
					
					if(newStatus != null){
						
						LOGGER.debug("Updating connection request with new status: {}", newStatus);
						connectionRequest.setStatus(newStatus);
						connectionRequestDAO.updateRequest(connectionRequest);
					}
				}
			}			
			
		}catch(Exception ex){
			
			LOGGER.error("Error while blocking connection: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
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

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null || cloudDiscovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud not found.");
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (cloudPrivateKey == null){
				
				LOGGER.debug("Cloud private key not found.");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null || cloud1Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud1 not found.");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null || cloud2Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud2 not found.");
				throw new NullPointerException("Cloud2 not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloudNumber);
			String guardian1CloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.debug("Checking if cloud: {} is a dependent cloud", cloud.toString());			
			if(guardianCloudNumber!=null && !guardianCloudNumber.equals("")){
				
				LOGGER.debug("You are not authorized to unblock a connection");
				throw new Exception("You are not authorized to unblock a connection");
			}
			
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardian1CloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}													
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
				throw new NullPointerException("Connection request not found");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber();
					String deleted = connectionRequest.getDeleted();

					String newStatus = null;
					if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){

						if(deleted != null && deleted.equals(Deleted.DELETED_BY_REQUESTER.getDeleted())){
							
							LOGGER.debug("Conenction request not found.");
							throw new Exception("Connection request not found.");
						}
						if(status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
							
							newStatus = Status.APPROVED.getStatus();
							
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_ACCEPTOR.getStatus();
							
						}else{
							
							LOGGER.debug("Connection can not be unblocked until blocked");
							throw new Exception("Connection can not be unblocked until blocked");
						}
					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){

						if(deleted != null && deleted.equals(Deleted.DELETED_BY_ACCEPTOR.getDeleted())){
							LOGGER.debug("Conenction request not found.");
							throw new Exception("Connection request not found.");
						}
						if(status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
							
							newStatus = Status.APPROVED.getStatus();
							
						}else if(status.equals(Status.BLOCKED.getStatus())){
							
							newStatus = Status.BLOCKED_BY_REQUESTER.getStatus();
						}else{
							
							LOGGER.debug("Connection can not be unblocked until blocked");
							throw new Exception("Connection can not be unblocked until blocked");
						}
					}
					
					if(newStatus!=null){
						
						LOGGER.debug("Updating connection request with new status: {}", newStatus);
						connectionRequest.setStatus(newStatus);
						connectionRequestDAO.updateRequest(connectionRequest);
					}
				}
			}			
			
		}catch(Exception ex){
			
			LOGGER.error("Error while unblocking connection: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
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

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null|| cloudDiscovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud not found.");
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (cloudPrivateKey == null){
				
				LOGGER.debug("Cloud private key not found.");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null|| cloudDiscovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud1 not found.");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null|| cloud2Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud2 not found.");
				throw new NullPointerException("Cloud2 not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			LOGGER.debug("Checking if cloud1: {} is a self or dependent cloud of cloud: {}",cloud1.toString(), cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber) && !cloudNumber.equals(guardianCloudNumber)){
				LOGGER.debug("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}
						
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
				throw new NullPointerException("Connection request not found");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String deleted = connectionRequest.getDeleted();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();										
					
					if(status.equals(Status.APPROVED.getStatus()) || status.equals(Status.BLOCKED.getStatus()) || status.equals(Status.BLOCKED_BY_REQUESTER.getStatus()) || status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
						if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){														

							if(deleted==null || deleted.equals("")){
								
								LOGGER.debug("Deleting connection request by reuquester");
								connectionRequest.setDeleted(Deleted.DELETED_BY_REQUESTER.getDeleted());
								connectionRequestDAO.updateRequest(connectionRequest);
								
							}else if(deleted.equals(Deleted.DELETED_BY_ACCEPTOR.getDeleted())){
								
								LOGGER.debug("Deleteing connection reuest from DB");
								connectionRequestDAO.deleteRequest(connectionRequest);								
							}else{
								
								LOGGER.debug("Connection is already deleted.");
								throw new Exception("Connection is already deleted.");
							}

						}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){
														
							if(deleted==null || deleted.equals("")){
								
								LOGGER.debug("Deleting connection request by acceptor");
								connectionRequest.setDeleted(Deleted.DELETED_BY_ACCEPTOR.getDeleted());
								connectionRequestDAO.updateRequest(connectionRequest);								
							}else if(deleted.equals(Deleted.DELETED_BY_REQUESTER.getDeleted())){
								
								LOGGER.debug("Deleteing connection reuest from DB");
								connectionRequestDAO.deleteRequest(connectionRequest);								
							}else{
								
								LOGGER.debug("Connection is already deleted.");
								throw new Exception("Connection is already deleted.");
							}							
						}												
					}else{
						
						LOGGER.debug("A connection can not be deleted until approved.");
						throw new Exception("A connection can not be deleted until approved.");
					}					
				}
			}						
		}catch(Exception ex){
			
			LOGGER.error("Error while deleting connection: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
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
			
			LOGGER.debug("Getting discovery of requestingCloud: {}", cloud1.toString());  
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null|| cloud1Discovery.toString().equals("null (null)")){
				
				LOGGER.debug("Cloud1: {} not found", cloud1.toString());
				throw new NullPointerException("Cloud1 not found.");
			}

			LOGGER.debug("Getting discovery of acceptingCloud: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null|| cloud2Discovery.toString().equals("null (null)")){
				
				LOGGER.error("Cloud2: {} not found", cloud2.toString());
				throw new NullPointerException("Cloud2 not found.");						
			}						
			
			String cloudNumber1 = cloud1Discovery.getCloudNumber().toString();
			String cloudNumber2 = cloud2Discovery.getCloudNumber().toString();
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloudNumber1, cloudNumber2);
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List<ConnectionRequest> connectionRequestList = connectionRequestDAO.getConnectionRequest(cloudNumber1, cloudNumber2);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("Connection request not found");
				throw new NullPointerException("Connection request not found");
			}
									
			ConnectionRequest connectionRequest = (ConnectionRequest)connectionRequestList.get(0);
			
			boolean isApprovalReq = false;
			boolean approved1 = false;
			boolean blocked1 = false;
			
			boolean approved2 = false;
			boolean blocked2 = false;
			
			if(connectionRequest.getDeleted() == null || connectionRequest.getDeleted().equals("")){
				
				String status = connectionRequest.getStatus();
				if(status.equals(Status.APPROVED.getStatus())){
					
					approved1 = true;
					approved2 = true;
					
				}else{ 
					if(status.equals(Status.BLOCKED.getStatus())){
						blocked1 = true;
						blocked2 = true;
						
					}else if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber()) 
							&& status.equals(Status.BLOCKED_BY_REQUESTER.getStatus())){
						
						blocked1 = true;
						
					}else if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getAcceptingCloudNumber()) 
							&& status.equals(Status.BLOCKED_BY_ACCEPTOR.getStatus())){
						
						blocked2 = true;
					}
				}
			}
			
			CloudName connectionName = null;
			if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber())){
				connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
			}else{
				connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
			}						
				
			connection = new ConnectionImpl(cloud1, cloud2, isApprovalReq, approved1, approved2, blocked1, blocked2, connectionName);					
			
		}catch(Exception ex){
			
			LOGGER.error("Error while find connection: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
		
		return connection;		
	}
}
