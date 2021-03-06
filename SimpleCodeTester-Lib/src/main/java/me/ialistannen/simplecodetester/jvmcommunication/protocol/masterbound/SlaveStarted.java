package me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound;

import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;

public class SlaveStarted extends ProtocolMessage {

  private long pid;

  public SlaveStarted(String uid, long pid) {
    super(uid);
    this.pid = pid;
  }

  public long getPid() {
    return pid;
  }

  @Override
  public String toString() {
    return "SlaveStarted{"
        + "uid=" + getUid()
        + ", pid=" + pid
        + "}";
  }
}
