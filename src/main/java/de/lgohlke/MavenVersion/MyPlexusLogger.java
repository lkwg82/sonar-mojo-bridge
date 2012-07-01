package de.lgohlke.MavenVersion;

import org.codehaus.plexus.logging.Logger;

public class MyPlexusLogger implements Logger {

  private final org.slf4j.Logger log;

  public MyPlexusLogger(final org.slf4j.Logger logger) {
    this.log = logger;
  }

  @Override
  public void debug(final String message) {
    log.debug(message);
  }

  @Override
  public void debug(final String message, final Throwable throwable) {
    log.debug(message, throwable);
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  @Override
  public void info(final String message) {
    log.info(message);
  }

  @Override
  public void info(final String message, final Throwable throwable) {
    log.info(message, throwable);
  }

  @Override
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  @Override
  public void warn(final String message) {
    log.warn(message);
  }

  @Override
  public void warn(final String message, final Throwable throwable) {
    log.warn(message, throwable);
  }

  @Override
  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

  @Override
  public void error(final String message) {
    log.error(message);
  }

  @Override
  public void error(final String message, final Throwable throwable) {
    log.error(message, throwable);
  }

  @Override
  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  @Override
  public void fatalError(final String message) {
    log.error("FATAL: " + message);
  }

  @Override
  public void fatalError(final String message, final Throwable throwable) {
    log.error("FATAL: " + message, throwable);
  }

  @Override
  public boolean isFatalErrorEnabled() {
    return log.isErrorEnabled();
  }

  @Override
  public int getThreshold() {
    if (log.isDebugEnabled() || log.isTraceEnabled()) {
      return Logger.LEVEL_DEBUG;
    } else if (log.isErrorEnabled()) {
      return Logger.LEVEL_ERROR;
    } else if (log.isWarnEnabled()) {
      return Logger.LEVEL_WARN;
    } else {
      return Logger.LEVEL_DISABLED;
    }
  }

  @Override
  public void setThreshold(final int threshold) {
    // ok
  }

  @Override
  public Logger getChildLogger(final String name) {
    return this;
  }

  @Override
  public String getName() {
    return this.getName();
  }
}
