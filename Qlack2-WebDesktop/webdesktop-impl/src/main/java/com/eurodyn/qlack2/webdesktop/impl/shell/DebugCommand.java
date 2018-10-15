package com.eurodyn.qlack2.webdesktop.impl.shell;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

@Command(scope = "qlack", name = "wd-debug", description = "A debug gateway for web desktop.")
public final class DebugCommand extends OsgiCommandSupport {

	@Override
	protected Object doExecute() {
//		new QlackMessage.Builder(BayeuxServerSingleton.getInstance().getServer())
//			.service("nodemanager")
//			.handler("aaa")
//			.channel(QlackMessage.CHANNEL_PUBLIC)
//			.data(new DebugMsg("test"))
//			.build()
//			.publish();

		return null;
	}

	class DebugMsg {
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public DebugMsg(String message) {
			this.message = message;
		}
	}

}
