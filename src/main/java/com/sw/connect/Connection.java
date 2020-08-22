package com.sw.connect;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.sw.SWAPI.util.APIExceptionUtil;

public class Connection {

	private static Log log = LogFactory.getLog(Connection.class);

	@Autowired
	private IntegrityFactory integrityFactory;

	private CmdRunner getCmdRunner(Session session) throws APIException {
		Iterator<?> it = session.getCmdRunners();
		int i = 0;
		while (it.hasNext()) {
			it.next();
			i++;
		}
		log.info("User: " + session.getDefaultUsername() + ", session cmdRunners: " + i);
		if (i < 5) {
			return session.createCmdRunner();
		}
		/*try {
			wait(1000);
		} catch (InterruptedException e) {
			log.warn("CmdRunner wait InterruptedException: " + e);
		}*/
		return getCmdRunner(session);
	}

	public Response execute(Command command) throws APIException {
		long start = System.currentTimeMillis();
		SessionPool conn = integrityFactory.getConnection();
		Session session = conn.getSession();
		if(session == null) {
			throw new APIException("Connection is null. Please wait and try later.");
		}
		CmdRunner cmdRunner = getCmdRunner(session);
		String[] cmds = command.toStringArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cmds.length; i++) {
			sb.append(" ");
			sb.append(cmds[i]);
		}
		log.info("Execcuting command: " + sb);
		Response response;
		try {
			response = cmdRunner.execute(command);
			long spend = System.currentTimeMillis() - start;
			log.info("Execute command Spent: " + spend);
		} finally {
			if (cmdRunner != null) {
				try {
					cmdRunner.release();
				} catch (APIException e) {
					log.info(APIExceptionUtil.getMsg(e));
				}
			}
			conn.release(session);
		}
		return response;
	}

	public IntegrityFactory getIntegrityFactory() {
		return integrityFactory;
	}

	public void setIntegrityFactory(IntegrityFactory integrityFactory) {
		this.integrityFactory = integrityFactory;
	}

}
