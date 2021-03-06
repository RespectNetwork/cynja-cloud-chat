package biz.neustar.clouds.chat.service;

import java.util.List;

import net.rn.clouds.chat.model.ChatMessage;
import xdi2.core.syntax.XDIAddress;
import biz.neustar.clouds.chat.model.Connection;
import biz.neustar.clouds.chat.model.Log;
import biz.neustar.clouds.chat.model.QueryInfo;

public interface ConnectionService {

    public Connection requestConnection(XDIAddress child1, String child1SecretToken, XDIAddress child2);

    public Connection approveConnection(XDIAddress parent, String parentSecretToken, XDIAddress child1,
            XDIAddress child2);

    public Connection[] viewConnectionsAsParent(XDIAddress parent, String parentSecretToken);

    public Connection[] viewConnectionsAsChild(XDIAddress child, String childSecretToken);

    public Log[] logsConnection(XDIAddress parent, String parentSecretToken, XDIAddress child1, XDIAddress child2);

    public Connection blockConnection(XDIAddress parent, String parentSecretToken, XDIAddress child1, XDIAddress child2);

    public Connection unblockConnection(XDIAddress parent, String parentSecretToken, XDIAddress child1,
            XDIAddress child2);

    public Connection deleteConnection(XDIAddress parent, String parentSecretToken, XDIAddress child1, XDIAddress child2);

    public Connection findConnection(XDIAddress child1, String child1SecretToken, XDIAddress child2);

    public List<ChatMessage> chatHistory(XDIAddress parent, String parentSecretToken, XDIAddress child1,
            XDIAddress child2, QueryInfo queryInfo);

    public Connection[] notifications(XDIAddress cloud, String cloudSecretToken);

    public void updateChatStatus(XDIAddress child1, List<ChatMessage> chatMessageList);
}
