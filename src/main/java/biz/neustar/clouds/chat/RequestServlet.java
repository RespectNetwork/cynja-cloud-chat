package biz.neustar.clouds.chat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import xdi2.core.syntax.XDIAddress;

public class RequestServlet extends HttpServlet {

	private static final long serialVersionUID = 2049298539409005496L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		XDIAddress child1 = XDIAddress.create(req.getParameter("child1"));
		String child1SecretToken = req.getParameter("child1SecretToken");
		XDIAddress child2 = XDIAddress.create(req.getParameter("child2"));

		CynjaCloudChat.connectionService.requestConnection(child1, child1SecretToken, child2);
	}
}
