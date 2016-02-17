/**
 * 
 */
package net.rn.clouds.chat.service.impl;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			if (cloud1Discovery == null){
				
				LOGGER.error("Cloud1: {} not found", cloud1.toString());
				throw new NullPointerException("Cloud1 not found.");
			}

			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null){
				
				LOGGER.error("Cloud2: {} not found", cloud2.toString());
				throw new NullPointerException("Cloud2 not found.");						
			}
			
			LOGGER.debug("Authenticating cloud1: {}",cloud1.toString());			
			PrivateKey cloud1PrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloud1Discovery.getCloudNumber(), cloud1Discovery.getXdiEndpointUrl(), cloud1SecretToken);
			if (cloud1PrivateKey == null){
				
				LOGGER.error("Cloud1: {} private key not found", cloud1.toString());
				throw new NullPointerException("Cloud1 private key not found.");
			}
						
			String cloud1Number = cloud1Discovery.getCloudNumber().toString();			
			String cloud2Number = cloud2Discovery.getCloudNumber().toString();		
		
			LOGGER.debug("Checking if connection already requested");
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1Number, cloud2Number);
			
			if(connectionRequestList != null && connectionRequestList.size() == 1 &&
					((ConnectionRequest)connectionRequestList.get(0)).getConnectingClouds().getRequestingCloudNumber().equals(cloud1Number)){
				
				LOGGER.error("Connection already requested between {} and {}", cloud1.toString(), cloud2.toString());
				throw new Exception("Connection already exists.");
			}
							
			String approvingCloudNumber = "";
			String status = "cloudApprovalPending";
			
			LOGGER.debug("Checking if requester is a dependent cloud");
			String requesterCloudParent = EntityUtil.getGuardianCloudNumber(cloud1Number);
			if(!requesterCloudParent.equals("")){
				
				approvingCloudNumber = requesterCloudParent;
				status = "new";
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
		
		LOGGER.debug("Calling xdi requestConnection with cloud1: {}, cloud2: {}", cloud1, cloud2);		
		//return CynjaCloudChat.connectionService.requestConnection(cloud1, cloud1SecretToken, cloud2);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#approveConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection approveConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){	
		
		LOGGER.debug("Enter approveConnection with approverCloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {

			LOGGER.debug("Getting discovery of approvingCloud: {}", cloud.toString()); 
			XDIDiscoveryResult approverDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (approverDiscovery == null){ 
			
				LOGGER.error("Approving cloud: {} not found. ",cloud.toString());
				throw new NullPointerException("Parent cloud not found.");
			}

			LOGGER.debug("Authenticating approvingCloud: {}",cloud.toString());
			PrivateKey approverPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(approverDiscovery.getCloudNumber(), approverDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (approverPrivateKey == null){
				
				LOGGER.error("Approver cloud: {} private key not found.", cloud.toString());
				throw new NullPointerException("Approver cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null){
				
				LOGGER.error("Cloud1 not found");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of Cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null){
				
				LOGGER.error("Cloud2 is not found");
				throw new NullPointerException("cloud2 not found.");
			}
			
			String cloudNumber = approverDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			LOGGER.debug("Checking if approver cloud: {} is a dependent cloud", cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber)){
				
				LOGGER.debug("Checking if approver cloud: {} is parent of cloud1: {}", cloud.toString(), cloud1.toString());
				String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
				
				if(!cloudNumber.equals(guardianCloudNumber)){
					LOGGER.error("Invalid cloud1 provided");
					throw new Exception("Incorrect cloud1 provided. ");
				}
			}else if(cloudNumber.equals(cloud2CloudNumber)){
				
				LOGGER.error("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}						
			
			LOGGER.debug("Getting connection request");
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);						
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.error("Connection request not found");
				throw new NullPointerException("Connection request not found.");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){

					ConnectionRequest connectionRequest = (ConnectionRequest)obj;						
					if(cloudNumber.equals(connectionRequest.getApprovingCloudNumber())){

						String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString();

						String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();
						String acceptingCloudParent = EntityUtil.getGuardianCloudNumber(acceptingCloudNumber);

						String status = connectionRequest.getStatus();

						String newApprover = null;
						String newStatus = null;																		

						if(!acceptingCloudParent.equals("")){	

							if(status.equals("new")){

								newStatus = "cloudApprovalPending";
								newApprover =  acceptingCloudParent;

							}else if(status.equals("cloudApprovalPending")){

								newStatus = "childApprovalPending";
								newApprover = acceptingCloudNumber;

							}else if(status.equals("childApprovalPending")){

								newStatus = "approved";
								newApprover = null;
							}							
						}else{
							if(status.equals("new")){
								newStatus = "cloudApprovalPending";
								newApprover = acceptingCloudNumber;
							}
							if(status.equals("cloudApprovalPending")){

								newStatus = "approved";
								newApprover = null;
							}
						}

						LOGGER.debug("Upating connection request");
						connectionRequest.setApprovingCloudNumber(newApprover);
						connectionRequest.setStatus(newStatus);							
						connectionRequestDAO.updateRequest(connectionRequest);
					}else{
						
						LOGGER.error("Cloud: {} is not authorized approver.", cloud.toString());
						throw new Exception("You are not authorized to approve the connection.");
					}
				}
			}													
		}catch (Exception ex) {

			LOGGER.error("Error while approving connection: {}", ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
		
		LOGGER.debug("Calling xdi approveConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		//return CynjaCloudChat.connectionService.approveConnection(cloud, cloudSecretToken, cloud1, cloud2);
		return null;
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
			if (parentDiscovery == null){
				
				LOGGER.error("Parent cloud not found");
				throw new NullPointerException("Parent not found.");
			}

			LOGGER.debug("Authenticating parent cloud: {}",parent.toString());
			PrivateKey parentPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(parentDiscovery.getCloudNumber(), parentDiscovery.getXdiEndpointUrl(), parentSecretToken);
			if (parentPrivateKey == null){
				
				LOGGER.error("Parent private key not found");
				throw new NullPointerException("Parent parent key not found.");
			}
				
			LOGGER.debug("Getting all children of parent cloud: {}",parent.toString());
			XDIAddress[] children = CynjaCloudChat.parentChildService.getChildren(parent, parentSecretToken);			
												
			List collection = new ArrayList();
			for (XDIAddress child : children) {
				
				LOGGER.debug("Getting discovery of child cloud: {}", child.toString());
				XDIDiscoveryResult childDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(child, null);
				
				LOGGER.debug("Adding child: {} to list", childDiscovery.getCloudNumber().toString());
				collection.add(childDiscovery.getCloudNumber().toString());			
			}
			
			LOGGER.debug("Getting connection requests of children of parent cloud:{} ",parent.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.viewConnections(collection);
						
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				return new ConnectionImpl[0];				
			}
			
			connection = new ConnectionImpl[connectionRequestList.size()];
			int clounter = 0;
			
			for (Object obj : connectionRequestList) {
				
				if(obj instanceof ConnectionRequest){
					
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					XDIAddress requestingCloud = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
					XDIAddress acceptingCloud = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
										
					boolean isApproved1 = false;
					boolean isApproved2 = false;
					
					if(connectionRequest.getStatus().equals("approved")){
						isApproved1 = true;	
						isApproved2 = true;
					}

					boolean isApprovalRequired = false;
					
					if(connectionRequest.getApprovingCloudNumber() != null && 
							connectionRequest.getApprovingCloudNumber().equals(parentDiscovery.getCloudNumber().toString())){
						isApprovalRequired = true;
					}																		

					XDIAddress child1;
					XDIAddress child2;																																								
					CloudName connectionName;
					boolean isBlocked1 = false;
					boolean isBlocked2 = false;
					
					String deleted = connectionRequest.getDeleted();
					if (collection.contains(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString())){
						
						LOGGER.debug("Do not add connection request to view list if connection request is deleted by requester");
						if(deleted != null && deleted.equals("deletedByRequester")){
							continue;
						}
						child1 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						child2 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());												
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.debug("Checking ig connection is blocked by requester");
						if(connectionRequest.getStatus().equals("blockedByRequester")){
							isBlocked1 = true;
						}else if(connectionRequest.getStatus().equals("blockedByAcceptor")){
							isBlocked2 = true;
						}
					}else{
						LOGGER.debug("Do not add connection request to view list if connection request is deleted by deletedByAcceptor");
						if(deleted != null && deleted.equals("deletedByAcceptor")){
							continue;
						}
						child1 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						child2 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						
						LOGGER.debug("Checking if connection is blocked by acceptor"); 
						if(connectionRequest.getStatus().equals("blockedByAcceptor")){
							isBlocked1 = true;
						}if(connectionRequest.getStatus().equals("blockedByRequester")){
							isBlocked2 = true;
						}
					}

					LOGGER.debug("Adding connection request to view list");
					
					connection[clounter++] = new ConnectionImpl(child1, child2, isApprovalRequired, isApproved1, 
							isApproved2, isBlocked1, isBlocked2, connectionName);
				}					
			}				
			
		} catch (Exception ex) {

			LOGGER.error("Error while viewing as parent: {}", ex.getMessage());
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
			if (cloudDiscovery == null){
				
				LOGGER.error("Cloud: {} not found", cloud.toString());
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (cloudPrivateKey == null){
				
				LOGGER.error("Cloud private key not found");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();						
			
			List collection = new ArrayList();
			collection.add(cloudNumber);
			
			LOGGER.debug("Getting connection requests of cloud: {}", cloud.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.viewConnections(collection);			
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.debug("No connection request not found for cloud: {}", cloud.toString());
				return new ConnectionImpl[0];
			}
				
			connection = new ConnectionImpl[connectionRequestList.size()];
			int counter = 0;
			
			for (Object obj : connectionRequestList) {
				
				if(obj instanceof ConnectionRequest){
					
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					XDIAddress requestingCloud = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
					XDIAddress acceptingCloud = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());

					LOGGER.debug("Checking if the connection request has been deleted by cloud: {}",cloud.toString());
					String deleted = connectionRequest.getDeleted();
					if(deleted !=null &&
							((deleted.equals("deletedByAcceptor") && cloudNumber.equals(acceptingCloud.toString())) ||
							(deleted.equals("deletedByRequester") && cloudNumber.equals(requestingCloud.toString())))){
						continue;
					}
					
					boolean isApproved1 = false;
					boolean isApproved2 = false;
					
					if(connectionRequest.getStatus().equals("approved")){
						isApproved1 = true;
						isApproved2 = true;
					}

					boolean isApprovalRequired = false;
					
					if(connectionRequest.getApprovingCloudNumber() != null &&
							connectionRequest.getApprovingCloudNumber().equals(cloudNumber)){
						isApprovalRequired = true;
					}																		

					XDIAddress cloud1;
					XDIAddress cloud2;																																								
					CloudName connectionName;
					boolean isBlocked1 = false;
					boolean isBlocked2 = false;
					if (cloudNumber.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString())){
						cloud1 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						cloud2 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						
						LOGGER.debug("Checking if connection request has been blocked by cloud: {}",cloud.toString());
						if(connectionRequest.getStatus().equals("blockedByRequester")){
							isBlocked1 = true;
						}else if(connectionRequest.getStatus().equals("blockedByAcceptor")){
							isBlocked2 = true;
						}
					}else{
						cloud1 = XDIAddress.create(connectionRequest.getConnectingClouds().getAcceptingCloudNumber());
						cloud2 = XDIAddress.create(connectionRequest.getConnectingClouds().getRequestingCloudNumber());
						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						
						LOGGER.debug("Checking if connection request has been blocked by cloud: {}",cloud.toString());
						if(connectionRequest.getStatus().equals("blockedByAcceptor")){
							isBlocked1 = true;
						}else if(connectionRequest.getStatus().equals("blockedByRequester")){
							isBlocked2 = true;
						}
					}
					
					LOGGER.debug("Adding connection request to view list");
					connection[counter++] = new ConnectionImpl(cloud1, cloud2, isApprovalRequired, 
							isApproved1, isApproved2, isBlocked1, isBlocked1, connectionName);
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
	public Log[] logsConnection(XDIAddress parent, String parentSecretToken, XDIAddress child1, XDIAddress child2){
		
		return CynjaCloudChat.connectionServiceImpl.logsConnection(parent, parentSecretToken, child1, child2);
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#blockConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection blockConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.debug("Enter blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null){
				
				LOGGER.error("Cloud: {} not found", cloud.toString());
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			
			if (cloudPrivateKey == null){
				
				LOGGER.error("Cloud private key not found.");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			
			if (cloud1Discovery == null){
				
				LOGGER.error("Cloud1 not found.");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			
			if (cloud2Discovery == null){
				
				LOGGER.error("Cloud2 not found.");
				throw new NullPointerException("Cloud2 not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			LOGGER.debug("Checking if cloud: {} is a dependent cloud",cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber)){
				
				LOGGER.debug("Checking if cloud: {} is parent of cloud1: {}", cloud.toString(), cloud1.toString());
				String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
				
				if(!cloudNumber.equals(guardianCloudNumber)){
					
					LOGGER.error("Invalid cloud1 provided");
					throw new Exception("Incorrect cloud1 provided. ");
				}
			}else if(cloudNumber.equals(cloud2CloudNumber)){
				
				LOGGER.error("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
						
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.error("Conenction request not found.");
				throw new NullPointerException("Connection request not found.");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber();
					
					CloudName connectionName = null;
					String newStatus = null;
					
					if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){

						connectionName = CloudName.create(connectionRequest.getAcceptingConnectionName());
						if(status.equals("approved")){
							
							newStatus = "blockedByRequester";
							
						}else if(status.equals("blockedByAcceptor")){
							
							newStatus = "blocked";
							
						}else if(status.equals("blockedByRequester")){
							
							LOGGER.error("Connection is already blocked");
							throw new Exception("Connection is already blocked");
						}else{
							
							LOGGER.error("Connection can not be blocked until approved.");
							throw new Exception("Connection can not be blocked until approved.");
						}

					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){

						connectionName = CloudName.create(connectionRequest.getRequestingConnectionName());
						if(status.equals("approved")){
							
							newStatus = "blockedByAcceptor";
							
						}else if(status.equals("blockedByRequester")){
							
							newStatus = "blocked";
							
						}else if(status.equals("blockedByAcceptor")){
							
							LOGGER.error("Connection is already blocked");
							throw new Exception("Connection is already blocked");
						}else{
							
							LOGGER.error("Connection can not be blocked until approved.");
							throw new Exception("Connection can not be blocked until approved.");
						}
					}
					
					if(newStatus != null){
						
						LOGGER.debug("Updating connection request with new status: {}", newStatus);
						connectionRequest.setStatus(newStatus);
						connectionRequestDAO.updateRequest(connectionRequest);
						
						boolean approved1=false, approved2= false, blocked1=false, blocked2=false;
						if(newStatus.equals("approved")){
							approved1 = true;
							approved2 = true;
						}else if(newStatus.equals("blocked")){
							blocked1 = true;
							blocked2 = true;
						}else if(newStatus.equals("blockedByRequester")){
							blocked1 = true;
						}else if(newStatus.equals("blockedByAcceptor")){
							blocked2 = true;
						}
						ConnectionImpl connection = new ConnectionImpl(cloud1, cloud2, false, approved1, approved2, blocked1, blocked2, connectionName);
						return connection;
					}
				}
			}			
			
		}catch(Exception ex){
			
			LOGGER.error("Error while blocking request connection: {}", ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
		
		LOGGER.debug("Calling xdi blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		//return CynjaCloudChat.connectionService.blockConnection(cloud, cloudSecretToken, cloud1, cloud2);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#unblockConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection unblockConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.debug("Enter unblockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null){
				
				LOGGER.error("Cloud not found.");
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (cloudPrivateKey == null){
				
				LOGGER.error("Cloud private key not found.");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null){
				
				LOGGER.error("Cloud1 not found.");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null){
				
				LOGGER.error("Cloud2 not found.");
				throw new NullPointerException("Cloud2 not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			
			LOGGER.debug("Checking if cloud: {} is a dependent cloud", cloud.toString());
			String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloudNumber);
			if(guardianCloudNumber!=null && !guardianCloudNumber.equals("")){
				
				LOGGER.error("You are not authorized to unblock a connection");
				throw new Exception("You are not authorized to unblock a connection");
			}
				
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			String cloud1Guardian = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
			
			LOGGER.debug("Checking if cloud: {} is parent of cloud1: {}", cloud.toString(), cloud1.toString());
			if(!cloudNumber.equals(cloud1CloudNumber)){
								
				if(!cloudNumber.equals(cloud1Guardian)){ 
				
					LOGGER.error("Invalid cloud1 provided");
					throw new Exception("Incorrect Cloud1 provided. ");
				}
			}else if(cloudNumber.equals(cloud2CloudNumber)){
				
				LOGGER.error("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.error("Connection request not found");
				throw new NullPointerException("Connection request not found");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber();

					String newStatus = null;
					if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){

						if(status.equals("blockedByRequester")){
							newStatus = "approved";
						}else if(status.equals("blocked")){
							newStatus = "blockedByAcceptor";
						}else{
							
							LOGGER.error("Connection can not be unblocked until blocked");
							throw new Exception("Connection can not be unblocked until blocked");
						}
					}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){

						if(status.equals("blockedByAcceptor")){
							newStatus = "approved";
						}else if(status.equals("blocked")){
							newStatus = "blockedByRequester";
						}else{
							
							LOGGER.error("Connection can not be unblocked until blocked");
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
			
			LOGGER.error("Error while unblocking request connection: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
		
		LOGGER.debug("Enter xdi unblockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		//return CynjaCloudChat.connectionService.unblockConnection(cloud, cloudSecretToken, cloud1, cloud2);
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#deleteConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress, xdi2.core.syntax.XDIAddress)
	 */
	public Connection deleteConnection(XDIAddress cloud, String cloudSecretToken, XDIAddress cloud1, XDIAddress cloud2){
		
		LOGGER.debug("Enter deleteConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		try {

			LOGGER.debug("Getting discovery of cloud: {}", cloud.toString());
			XDIDiscoveryResult cloudDiscovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud, null);
			if (cloudDiscovery == null){
				
				LOGGER.error("Cloud not found.");
				throw new NullPointerException("Cloud not found.");
			}

			LOGGER.debug("Authenticating cloud: {}",cloud.toString());
			PrivateKey cloudPrivateKey = XDIClientUtil.retrieveSignaturePrivateKey(cloudDiscovery.getCloudNumber(), cloudDiscovery.getXdiEndpointUrl(), cloudSecretToken);
			if (cloudPrivateKey == null){
				
				LOGGER.error("Cloud private key not found.");
				throw new NullPointerException("Cloud private key not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud1: {}", cloud1.toString());
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null){
				
				LOGGER.error("Cloud1 not found.");
				throw new NullPointerException("Cloud1 not found.");
			}
			
			LOGGER.debug("Getting discovery of cloud2: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null){
				
				LOGGER.error("Cloud2 not found.");
				throw new NullPointerException("Cloud2 not found.");
			}
			
			String cloudNumber = cloudDiscovery.getCloudNumber().toString();
			String cloud1CloudNumber = cloud1Discovery.getCloudNumber().toString();
			String cloud2CloudNumber = cloud2Discovery.getCloudNumber().toString();
			
			LOGGER.debug("Checking if cloud: {} is a dependent cloud", cloud.toString());
			if(!cloudNumber.equals(cloud1CloudNumber)){
				
				LOGGER.debug("Checking if cloud: {} is parent of cloud1: {}", cloud.toString(), cloud1.toString());
				String guardianCloudNumber = EntityUtil.getGuardianCloudNumber(cloud1CloudNumber);
				
				if(!cloudNumber.equals(guardianCloudNumber)){ 
				
					LOGGER.error("Invalid cloud1 provided");
					throw new Exception("Incorrect Cloud1 provided. ");
				}
			}else if(cloudNumber.equals(cloud2CloudNumber)){
				
				LOGGER.error("Invalid cloud1 provided");
				throw new Exception("Incorrect cloud1 provided. ");
			}
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloud1.toString(), cloud2.toString());
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.getConnectionRequest(cloud1CloudNumber, cloud2CloudNumber);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.error("Connection request not found");
				throw new NullPointerException("Connection request not found");
			}
				
			for (Object obj : connectionRequestList) {

				if(obj instanceof ConnectionRequest){
					ConnectionRequest connectionRequest = (ConnectionRequest)obj;

					String status = connectionRequest.getStatus();
					String deleted = connectionRequest.getDeleted();
					String requestingCloudNumber = connectionRequest.getConnectingClouds().getRequestingCloudNumber().toString();
					String acceptingCloudNumber = connectionRequest.getConnectingClouds().getAcceptingCloudNumber().toString();
					
					CloudName connectionName = null;
					boolean approved1=false, approved2= false, blocked1=false, blocked2=false;
					if(status.equals("approved")){
						approved1 = true;
						approved2 = true;
					}else if(status.equals("blocked")){
						blocked1 = true;
						blocked2 = true;
					}else if(status.equals("blockedByRequester")){
						blocked1 = true;
					}else if(status.equals("blockedByAcceptor")){
						blocked2 = true;
					}					
										 
					connectionName = CloudName.create(cloud2.toString());
					ConnectionImpl connection = new ConnectionImpl(cloud1, cloud2, false, approved1, approved2, blocked1, blocked2, connectionName);
					
					if(status.equals("approved") || status.equals("blocked") || status.equals("blockedByRequester") || status.equals("blockedByAcceptor")){
						if(requestingCloudNumber.equals(cloudNumber) || requestingCloudNumber.equals(cloud1CloudNumber)){														

							if(deleted==null || deleted.equals("")){
								
								LOGGER.debug("Deleting connection request by reuquester");
								connectionRequest.setDeleted("deletedByRequester");
								connectionRequestDAO.updateRequest(connectionRequest);
								
							}else if(deleted.equals("deletedByAcceptor")){
								
								LOGGER.debug("Deleteing connection reuest from DB");
								connectionRequestDAO.deleteRequest(connectionRequest);								
							}else{
								
								LOGGER.error("Connection is already deleted.");
								throw new Exception("Connection is already deleted.");
							}

						}else if(acceptingCloudNumber.equals(cloudNumber) || acceptingCloudNumber.equals(cloud1CloudNumber)){
														
							if(deleted==null || deleted.equals("")){
								
								LOGGER.debug("Deleting connection request by acceptor");
								connectionRequest.setDeleted("deletedByAcceptor");
								connectionRequestDAO.updateRequest(connectionRequest);								
							}else if(deleted.equals("deletedByRequester")){
								
								LOGGER.debug("Deleteing connection reuest from DB");
								connectionRequestDAO.deleteRequest(connectionRequest);								
							}else{
								
								LOGGER.error("Connection is already deleted.");
								throw new Exception("Connection is already deleted.");
							}							
						}												
					}else{
						
						LOGGER.error("A connection can not be deleted until approved.");
						throw new Exception("A connection can not be deleted until approved.");
					}
					return connection;
				}
			}			
			
		}catch(Exception ex){
			
			LOGGER.error("Error while deleting request connection: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
		
		LOGGER.debug("Enter xdi blockConnection with cloud: {}, cloud1: {}, cloud2: {}", cloud, cloud1, cloud2);
		//return CynjaCloudChat.connectionService.deleteConnection(cloud, cloudSecretToken, cloud1, cloud2);
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see biz.neustar.clouds.chat.service.ConnectionService#findConnection(xdi2.core.syntax.XDIAddress, java.lang.String, xdi2.core.syntax.XDIAddress)
	 */
	public Connection findConnection(XDIAddress cloud1, String cloud1SecretToken, XDIAddress cloud2){
		
		LOGGER.debug("Enter requestConnection with requestingCloud: {}, acceptingCloud: {}", cloud1, cloud2);
		Connection connection = null;
		try {
			
			LOGGER.debug("Getting discovery of requestingCloud: {}", cloud1.toString());  
			XDIDiscoveryResult cloud1Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud1, null);
			if (cloud1Discovery == null){
				
				LOGGER.error("Cloud1: {} not found", cloud1.toString());
				throw new NullPointerException("Cloud1 not found.");
			}

			LOGGER.debug("Getting discovery of acceptingCloud: {}", cloud2.toString());
			XDIDiscoveryResult cloud2Discovery = InitFilter.XDI_DISCOVERY_CLIENT.discoverFromRegistry(cloud2, null);
			if (cloud2Discovery == null){
				
				LOGGER.error("Cloud2: {} not found", cloud2.toString());
				throw new NullPointerException("Cloud2 not found.");						
			}						
			
			String cloudNumber1 = cloud1Discovery.getCloudNumber().toString();
			String cloudNumber2 = cloud2Discovery.getCloudNumber().toString();
			
			LOGGER.debug("Getting connection request between cloud1: {}, cloud2: {}", cloudNumber1, cloudNumber2);
			ConnectionRequestDAO connectionRequestDAO = new ConnectionRequestDAOImpl();
			List connectionRequestList = connectionRequestDAO.getConnectionRequest(cloudNumber1, cloudNumber2);
			
			if(connectionRequestList == null || connectionRequestList.size()==0){
				
				LOGGER.error("Connection request not found");
				throw new NullPointerException("Connection request not found");
			}
									
			ConnectionRequest connectionRequest = (ConnectionRequest)connectionRequestList.get(0);
			
			boolean isApprovalReq = false;
			boolean approved1 = false;
			boolean blocked1 = false;
			
			boolean approved2 = false;
			boolean blocked2 = false;
			
			if(connectionRequest.getDeleted() == null || connectionRequest.getDeleted().equals("")){
				if(connectionRequest.getStatus().equals("approved")){
					approved1 = true;
					approved2 = true;
				}else{ 
					if(connectionRequest.getStatus().equals("blocked")){
						blocked1 = true;
						blocked2 = true;
					}else if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getRequestingCloudNumber()) 
							&& connectionRequest.getStatus().equals("blockedByRequester")){
						blocked1 = true;
					}else if(cloudNumber1.equals(connectionRequest.getConnectingClouds().getAcceptingCloudNumber()) 
							&& connectionRequest.getStatus().equals("blockedByAcceptor")){
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
			
			LOGGER.error("Error while find conenction: {}",ex.getMessage());
			throw new ConnectionNotFoundException(ex.getMessage());
		}
		
		return connection;		
	}
}
