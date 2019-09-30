package com.arise.astox.net.servers;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.io.IOServer;

public class IOServerTest extends AbstractServerTest {

  @Override
  public AbstractServer serviceServer() {
    return new IOServer().setPort(8221);
  }
}
