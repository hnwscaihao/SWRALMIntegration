package com.sw.connect;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.mks.api.response.APIException;

public class IntegrityFactory implements InitializingBean {

	private static Log log = LogFactory.getLog(IntegrityFactory.class);

	private static String host;

	private static int port = 7001; 

	private static int majorVersion = 4;

	private static int minorVersion = 15;

	private static boolean secure;

	private static int maxCmdRunners = 10;

	private static int initSession = 5;

	private static int maxSessionSize = 16;

	private static int waitTimes = 60;

	private static long lazyCheck = 60 * 60;

	private static long periodCheck = 60 * 60 * 2; // 2 hours

	private static String userKeyPrefix = "multi_user";

	private static Queue<SessionPool> pools;

	public synchronized SessionPool getConnection() throws APIException {
		SessionPool conn = pools.peek(); // balance

		if (conn == null) {
			log.info("IntegrityFactory.getConnection: Connection Factory is null. Please check you config.");
			throw new APIException(
					"IntegrityFactory.getConnection: Connection Factory is null. Please check you config.");
		}

		return conn;
	}

	static {

		Set<MksInfo> infos;
		try {
			infos = loadProps();
			if (infos.isEmpty()) {
				log.error("IntegrityFactory init Exception: integrity properties config error.");
				try {
					throw new Exception("IntegrityFactory init Exception: integrity properties config error.");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			pools = new LinkedBlockingDeque<SessionPool>();
			for (MksInfo mksInfo : infos) {
				SessionPool sessionPool = new SessionPool(mksInfo);
				pools.offer(sessionPool);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static Set<MksInfo> loadProps() throws IOException {
		try {
			Properties prop = new Properties();
			prop.load(IntegrityFactory.class.getResourceAsStream("/resources/integrity.properties"));

			Set<MksInfo> mksinfos = new HashSet<MksInfo>();
			for (Object key : prop.keySet()) {

				if (!((String) key).startsWith(userKeyPrefix)) {
					continue;
				}

				String val = (String) prop.get(key);

				String[] userPwds = val.split("\\|q;q;q;\\|");

				MksInfo info = new MksInfo();
				info.setUser(userPwds[0]);
				info.setPassword(userPwds[1]);

				info.setHost(host);
				info.setPort(port);
				info.setMajorVersion(majorVersion);
				info.setMinorVersion(minorVersion);
				info.setSecure(secure);
				info.setMaxCmdRunners(maxCmdRunners);
				info.setInitSession(initSession);
				info.setMaxSessionSize(maxSessionSize);
				info.setWaitTimes(waitTimes);
				info.setLazyCheck(lazyCheck);
				info.setPeriodCheck(periodCheck);

				mksinfos.add(info);
			}

			return mksinfos;
		} catch (IOException e) {
			throw e;
		}
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public void setMaxCmdRunners(int maxCmdRunners) {
		this.maxCmdRunners = maxCmdRunners;
	}

	public void setInitSession(int initSession) {
		this.initSession = initSession;
	}

	public void setMaxSessionSize(int maxSessionSize) {
		this.maxSessionSize = maxSessionSize;
	}

	public void setWaitTimes(int waitTimes) {
		this.waitTimes = waitTimes;
	}

	public void setLazyCheck(long lazyCheck) {
		this.lazyCheck = lazyCheck;
	}

	public void setPeriodCheck(long periodCheck) {
		this.periodCheck = periodCheck;
	}

	public void setUserKeyPrefix(String userKeyPrefix) {
		this.userKeyPrefix = userKeyPrefix;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
