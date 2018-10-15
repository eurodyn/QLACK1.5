package com.eurodyn.qlack2.fuse.zookeeper.shell;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.eurodyn.qlack2.fuse.zookeeper.api.ZookeeperManagerService;

@Command(scope = "qlack", name = "zk-stop", description = "Stops ZooKeeper.")
public final class StopCommand extends OsgiCommandSupport {
//	@Argument(index=0, name="username", description = "The username of the user to add.", required = true, multiValued = false)
//    private String username;
//	
	private ZookeeperManagerService zookeeperManager;

	public void setZookeeperManager(ZookeeperManagerService zookeeperManager) {
		this.zookeeperManager = zookeeperManager;
	}


	@Override
	protected Object doExecute() {
		
		
		return null;
	}
	
}
