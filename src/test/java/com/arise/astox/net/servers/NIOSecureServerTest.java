package com.arise.astox.net.servers;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.nio.NIOServer;

public class NIOSecureServerTest extends AbstractServerTest {

  @Override
  public AbstractServer serviceServer() {
    return new NIOServer().setSslContext(getSSLContext()).setPort(9222);
  }
}
