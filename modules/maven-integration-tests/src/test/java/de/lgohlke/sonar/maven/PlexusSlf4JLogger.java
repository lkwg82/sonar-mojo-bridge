/*
 * sonar-mojo-bridge-integration
 * Copyright (C) 2012 Lars Gohlke
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package de.lgohlke.sonar.maven;

import lombok.RequiredArgsConstructor;
import org.codehaus.plexus.logging.Logger;


/**
 * need for setting log level of maven-subsystem
 *
 * @author Lars Gohlke *
 */
@RequiredArgsConstructor
public class PlexusSlf4JLogger implements Logger {
  private final org.slf4j.Logger log;

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
    return getClass().getCanonicalName();
  }
}
