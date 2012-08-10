package de.lgohlke.sonar.maven;

import de.lgohlke.sonar.maven.Maven3ExecutionProcessTest.MyResultTransferHandler;
import de.lgohlke.sonar.maven.plugin.BridgeMojo;
import de.lgohlke.sonar.maven.plugin.ResultTransferHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.versions.HelpMojo;

@Goal("help")
public class MyBridgeMojo extends HelpMojo implements BridgeMojo<MyResultTransferHandler> {

  private MyResultTransferHandler handler;

  @Override
  public void execute() throws MojoExecutionException {
    handler.setPing(true);
  }

  @Override
  public void injectResultHandler(final ResultTransferHandler<?> handler) {
    this.handler = (MyResultTransferHandler) handler;
  }
}