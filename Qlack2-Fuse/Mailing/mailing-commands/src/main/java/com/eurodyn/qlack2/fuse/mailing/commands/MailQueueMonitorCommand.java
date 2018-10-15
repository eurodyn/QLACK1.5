package com.eurodyn.qlack2.fuse.mailing.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.eurodyn.qlack2.fuse.mailing.api.MailQueueMonitorClock;

@Command(scope = "qlack", name = "mail-queue-monitor", description = "Control the mailing queue monitor")
public final class MailQueueMonitorCommand extends OsgiCommandSupport {

	@Argument(index = 0, name = "action", description = "The action to execute", required = true, multiValued = false)
	private String action;

	private MailQueueMonitorClock clock;

	public void setClock(MailQueueMonitorClock clock) {
		this.clock = clock;
	}

	@Override
	protected Object doExecute() {

		if (action.equals("start")) {
			clock.start();
		}
		else if (action.equals("stop")) {
			clock.stop();
		}
		else if (action.equals("status")) {
			clock.status();
		}

		return null;
	}

}
