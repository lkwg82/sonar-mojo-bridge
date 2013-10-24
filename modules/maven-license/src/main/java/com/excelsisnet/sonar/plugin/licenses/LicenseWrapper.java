/*
 * Dependency Licenses
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
package com.excelsisnet.sonar.plugin.licenses;

import org.apache.commons.lang.StringUtils;
import org.sonar.check.Priority;

public class LicenseWrapper
{
	private final String name;

	private final String license;

	private final String uri;

	private final Priority severity;

	private final String title;

	/**
	 * Does not comply.
	 */
	public LicenseWrapper(String name, String license, String uri, Priority severity, String title)
	{
		super();
		if (StringUtils.isBlank(name))
		{
			throw new IllegalArgumentException("Name cannot be empty.");
		}
		this.name = name;

		if (StringUtils.isBlank(license))
		{
			this.license = null;
		}
		else this.license = license;

		if (StringUtils.isBlank(uri))
		{
			this.uri = null;
		}
		else
		{
			this.uri = uri;
		}

		this.severity = severity;

		if (StringUtils.isBlank(title))
		{
			this.title = null;
		}
		else
		{
			this.title = uri;
		}
	}

	/**
	 * Complies with the rules. No severity at all.
	 */
	public LicenseWrapper(String name, String license, String uri)
	{
		this(name, license, uri, null, null);

	}

	public String getName()
	{
		return name;
	}

	public String getLicense()
	{
		return license;
	}

	public String getUri()
	{
		return uri;
	}

	public Priority getSeverity()
	{
		return severity;
	}

	public String getTitle()
	{
		return title;
	}

	public String toString()
	{
		StringBuilder result = new StringBuilder();

		result.append(name);
		result.append('>');
		result.append(license == null ? "" : license);
		result.append('>');
		result.append(uri == null ? "" : uri);
		result.append('>');
		result.append(severity == null ? "" : severity.name());
		result.append('>');
		result.append(title == null ? "" : uri);

		return result.toString();
	}

}
