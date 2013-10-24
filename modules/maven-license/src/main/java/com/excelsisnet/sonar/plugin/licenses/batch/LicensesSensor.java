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
package com.excelsisnet.sonar.plugin.licenses.batch;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.check.Priority;

import com.excelsisnet.sonar.plugin.licenses.LicenseMetrics;
import com.excelsisnet.sonar.plugin.licenses.LicenseWrapper;

public class LicensesSensor implements Sensor
{

	public boolean shouldExecuteOnProject(Project project)
	{
		return true;
	}

	private Measure getMeasure(List<LicenseWrapper> licenses)
	{
		StringBuilder strB = new StringBuilder();

		Collections.sort(licenses, new Comparator<LicenseWrapper>()
		{
			@Override
			public int compare(LicenseWrapper o1, LicenseWrapper o2)
			{
				int s1 = (o1.getSeverity() == null ? -1 : o1.getSeverity().ordinal());
				int s2 = (o2.getSeverity() == null ? -1 : o2.getSeverity().ordinal());

				if (s1 == s2)
				{
					return o1.getName().compareTo(o2.getName());
				}
				else
				{
					return (s1 < s2 ? 1 : -1);
				}
			}
		});

		Iterator<LicenseWrapper> iter = licenses.iterator();

		while (iter.hasNext())
		{
			strB.append(iter.next().toString());
			if (iter.hasNext())
			{
				strB.append("|");
			}
		}

		Measure result = new Measure();

		result.setMetric(LicenseMetrics.LICENSE);
		result.setData(strB.toString());
		result.setDate(new Date());

		return result;
	}

	public void analyse(Project project, SensorContext context)
	{
		List<LicenseWrapper> licenses = new LinkedList<LicenseWrapper>();

		licenses.add(new LicenseWrapper("Commons Codec", "The Apache Software License, Version 2.0", "http://commons.apache.org/codec/"));
		licenses.add(new LicenseWrapper("Commons IO", "The Apache Software License, Version 2.0", "http://commons.apache.org/io/"));
		licenses.add(new LicenseWrapper("Commons Logging", "The Apache Software License, Version 2.0", "http://commons.apache.org/logging/"));
		licenses.add(new LicenseWrapper("servlet-api", "Common Development and Distribution License (CDDL) Version 1.0", null, Priority.INFO, "No URL was defined."));
		licenses.add(new LicenseWrapper("JUnit", "Common Public License Version 1.0", "http://junit.org"));
		licenses.add(new LicenseWrapper("Apache Log4j", "The Apache Software License, Version 2.0", "http://logging.apache.org/log4j/1.2/"));
		licenses.add(new LicenseWrapper("SLF4J API Module", "MIT License", "http://www.slf4j.org", Priority.MAJOR, "Unknown license."));
		licenses.add(new LicenseWrapper("SLF4J LOG4J-12 Binding", "MIT License", "http://www.slf4j.org", Priority.MAJOR, "Unknown license."));
		licenses.add(new LicenseWrapper("Spring Beans", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Spring Context", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Spring Context Support", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Spring Core", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Spring Expression Language (SpEL)", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Spring TestContext Framework", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Spring Web", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Spring Web MVC", "The Apache Software License, Version 2.0", "https://github.com/SpringSource/spring-framework"));
		licenses.add(new LicenseWrapper("Some GPL Software", "GNU General Public License", "http://domain.tld", Priority.CRITICAL, "License is forbidden in this project"));
		licenses.add(new LicenseWrapper("Some PD Software", "Public Domain", "http://domain.tld"));
		licenses.add(new LicenseWrapper("AcMe Base Framework", "Company's own private sources.", "http://domain.tld"));

		context.saveMeasure(getMeasure(licenses));

	}

	@Override
	public String toString()
	{
		return "Dependency Licenses";
	}

}
