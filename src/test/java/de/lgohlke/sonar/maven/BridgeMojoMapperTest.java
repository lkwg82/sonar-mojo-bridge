package de.lgohlke.sonar.maven;

import lombok.Data;
import org.testng.annotations.Test;

/**
 * User: lars
 */
public class BridgeMojoMapperTest {
  @Data
  @Goal("test")
  class MyBridgeMojo implements BridgeMojo {
    ResultTransferHandler resultHandler;
  }

  @Data
  @Goal("test2")
  class MyBridgeMojo2 implements BridgeMojo {
    ResultTransferHandler resultHandler;
  }

  class MyResultTransferHandler implements ResultTransferHandler {

  }

  @Test(expectedExceptions = BridgeMojoMapperException.class)
  public void shouldFailWhenInjectingWrongBridgeMojo() throws Exception {
    BridgeMojoMapper mapper = new BridgeMojoMapper(MyBridgeMojo.class, new MyResultTransferHandler());
    mapper.injectResultTransferHandler(new MyBridgeMojo2());
  }
}
