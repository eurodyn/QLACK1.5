package com.eurodyn.qlack2.fuse.zookeeper.impl;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServer;

import com.eurodyn.qlack2.fuse.zookeeper.api.ZookeeperManagerService;

public class ZookeeperManagerServiceImpl implements ZookeeperManagerService {
	private static final Logger LOGGER = Logger.getLogger(ZookeeperManagerServiceImpl.class.getName());

	private String nodeName;
	private String zkTxDir;
	private String zkLogDir;
	private int zkTickTime;
	private int zkConnections;
	private String zkPort;
	private String zkIPAddress;
	private ServerCnxnFactory zkFactory;
	// Holds the actual configuration with which the currently running
	// ZooKeeper instance is initialised with.
	private ZKConfig config;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getZkTxDir() {
		return zkTxDir;
	}

	public void setZkTxDir(String zkTxDir) {
		this.zkTxDir = zkTxDir;
	}

	public String getZkLogDir() {
		return zkLogDir;
	}

	public void setZkLogDir(String zkLogDir) {
		this.zkLogDir = zkLogDir;
	}

	public int getZkTickTime() {
		return zkTickTime;
	}

	public void setZkTickTime(int zkTickTime) {
		this.zkTickTime = zkTickTime;
	}

	public int getZkConnections() {
		return zkConnections;
	}

	public void setZkConnections(int zkConnections) {
		this.zkConnections = zkConnections;
	}

	public String getZkPort() {
		return zkPort;
	}

	public void setZkPort(String zkPort) {
		this.zkPort = zkPort;
	}

	public String getZkIPAddress() {
		return zkIPAddress;
	}

	public void setZkIPAddress(String zkIPAddress) {
		this.zkIPAddress = zkIPAddress;
	}

	public void start() {
		// Do not proceed if ZooKeeper is already running.

//		if (zk.isRunning()) {
//			System.out.println("ZooKeeper is already running.");
//			return;
//		}

		// Before ZooKeeper is started we establish sensible default values,
		// either from configuration properties or ZooKeeper defaults.
		ZKConfig zkConfig = new ZKConfig();
		try {
			// Nodename.
			if (StringUtils.isNotBlank(nodeName)) {
				zkConfig.nodeName = nodeName;
			} else {
				zkConfig.nodeName = InetAddress.getLocalHost().getHostName();
			}

			// ZK directories.
			if (StringUtils.isNotBlank(zkTxDir)) {
				zkConfig.zkTxDir = zkTxDir;
			} else {
				zkConfig.zkTxDir = System.getProperty("java.io.tmpdir") +
					File.pathSeparator + nodeName + File.pathSeparator +
					"zk" + File.pathSeparator + "tx";
			}
			if (StringUtils.isNotBlank(zkTxDir)) {
				zkConfig.zkLogDir = zkLogDir;
			} else {
				zkConfig.zkLogDir = System.getProperty("java.io.tmpdir") +
					File.pathSeparator + nodeName + File.pathSeparator +
					"zk" + File.pathSeparator + "log";
			}

			// Ticktime and Connections.
			zkConfig.zkTickTime = zkTickTime;
			zkConfig.zkConnections = zkConnections;

			// IP address and port.
			if (StringUtils.isNotEmpty(zkIPAddress)) {
				zkConfig.zkIPAddress = zkIPAddress;
			} else {
				zkConfig.zkIPAddress = null;
			}
			if (StringUtils.isNotEmpty(zkPort)) {
				zkConfig.zkPort = Integer.parseInt(zkPort);
			} else {
				zkConfig.zkPort = 0;
			}

			// Start ZooKeeper using the configuration established above.
			ServerConfig config = new ServerConfig();



			ZooKeeperServer zk = new ZooKeeperServer(
				new File(zkConfig.zkTxDir).getAbsoluteFile(),
				new File(zkConfig.zkLogDir).getAbsoluteFile(),
				zkConfig.zkTickTime);
			zkFactory = ServerCnxnFactory.createFactory(
				new InetSocketAddress(zkConfig.zkIPAddress, zkConfig.zkPort),
				zkConfig.zkConnections);
			zkFactory.startup(zk);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could start ZooKeeper server.", e);
		}


//		String dataDirectory = System.getProperty("java.io.tmpdir");
//		dataDirectory = "/Users/nassos/tmp/zk";
//		File dir = new File(dataDirectory, "zookeeper").getAbsoluteFile();
//		int tickTime = 2000;
//		int numConnections = 5000;
//		System.out.println("Using: " + dir.getAbsolutePath());
//		ZooKeeperServer server = new ZooKeeperServer(dir, dir, tickTime);
//		ServerCnxnFactory standaloneServerFactory =
//				ServerCnxnFactory.createFactory(0, numConnections);
//		int zkPort = standaloneServerFactory.getLocalPort();
//		standaloneServerFactory.startup(server);
//
//		System.out.println("Started.");
//		System.in.read();
//		standaloneServerFactory.shutdown();
//		System.out.println("Ended");
//

	}

	public void stop() {


	}

	public void init() {

	}

	public void destroy() {

	}

	private class ZKConfig {
		public String nodeName;
		public String zkTxDir;
		public String zkLogDir;
		public int zkTickTime;
		public int zkConnections;
		public int zkPort;
		public String zkIPAddress;
	}

}
