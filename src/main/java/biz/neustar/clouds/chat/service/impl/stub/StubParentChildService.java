package biz.neustar.clouds.chat.service.impl.stub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xdi2.core.syntax.XDIAddress;
import biz.neustar.clouds.chat.service.ParentChildService;

public class StubParentChildService implements ParentChildService {

	private static final String STUB_SECRET_TOKEN = "abcd";

	private Map<XDIAddress, List<XDIAddress>> parentsChildren;

	/*
	 * EXAMPLE PARENT/CHILD DATA:
	 * 
	 * Parents
	 *   [=]!:uuid:1111 and [=]!:uuid:2222
	 * have the following children
	 *   [=]!:uuid:3333, [=]!:uuid:4444
	 * 
	 * Parents
	 *   [=]!:uuid:5555 and [=]!:uuid:6666
	 * have the following children
	 *   [=]!:uuid:7777, [=]!:uuid:8888, [=]!:uuid:9999
	 *   
	 * Secret token for all parents and children: test@123
	 */

	public StubParentChildService() {

		this.parentsChildren = new HashMap<XDIAddress, List<XDIAddress>> ();

		List<XDIAddress> parentChildren1 = new ArrayList<XDIAddress> ();
		parentChildren1.add(XDIAddress.create("[=]!:uuid:3333"));
		parentChildren1.add(XDIAddress.create("[=]!:uuid:4444"));

		List<XDIAddress> parentChildren2 = new ArrayList<XDIAddress> ();
		parentChildren2.add(XDIAddress.create("[=]!:uuid:7777"));
		parentChildren2.add(XDIAddress.create("[=]!:uuid:8888"));
		parentChildren2.add(XDIAddress.create("[=]!:uuid:9999"));

		this.parentsChildren.put(XDIAddress.create("[=]!:uuid:1111"), parentChildren1);
		this.parentsChildren.put(XDIAddress.create("[=]!:uuid:2222"), parentChildren1);
		this.parentsChildren.put(XDIAddress.create("[=]!:uuid:5555"), parentChildren2);
		this.parentsChildren.put(XDIAddress.create("[=]!:uuid:6666"), parentChildren2);
	}

	@Override
	public boolean isParent(XDIAddress parent, String parentSecretToken, XDIAddress child) {

		if (! STUB_SECRET_TOKEN.equals(parentSecretToken)) throw new RuntimeException("Invalid parent secret token for " + parent);

		List<XDIAddress> parentChildren = this.parentsChildren.get(parent);
		if (parentChildren == null) return false;

		return parentChildren.contains(child);
	}

	@Override
	public XDIAddress[] getChildren(XDIAddress parent, String parentSecretToken) {

		if (! STUB_SECRET_TOKEN.equals(parentSecretToken)) throw new RuntimeException("Invalid parent secret token for " + parent);

		List<XDIAddress> parentChildren = this.parentsChildren.get(parent);
		if (parentChildren == null) return new XDIAddress[0];

		return parentChildren.toArray(new XDIAddress[parentChildren.size()]);
	}
}
