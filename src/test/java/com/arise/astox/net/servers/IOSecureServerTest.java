package com.arise.astox.net.servers;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.io.IOServer;

public class IOSecureServerTest extends AbstractServerTest {

  @Override
  public AbstractServer serviceServer() {
    return new IOServer().setSslContext(getSSLContext()).setPort(8222);
  }
}
