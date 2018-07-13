/*
 *  Copyright 2017 Information and Computational Sciences,
 *  The James Hutton Institute.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jhi.barcodr.util;

import java.io.*;
import java.util.*;

import jhi.barcodr.gui.*;
import jhi.swtcommons.util.*;

/**
 * {@link PropertyReader} is a wrapper around {@link Properties} to read properties.
 *
 * @author Sebastian Raubach
 */
public class PropertyReader extends jhi.swtcommons.util.PropertyReader
{
	public static final String PROPERTY_PATH_IN  = "path.in";
	public static final String PROPERTY_PATH_OUT = "path.out";

	/** The name of the properties file (slash necessary for MacOS X) */
	private static final String PROPERTIES_FILE = "/barcodr.properties";

	private static final String PROPERTIES_FOLDER_NEW = "jhi-ics";

	private static File localFile;

	public PropertyReader()
	{
		super(PROPERTIES_FILE);
	}

	/**
	 * Loads the properties {@link File}. It will try to load the local file first (in home directory). If this file doesn't exist, it will fall back
	 * to the default within the jar (or the local file in the project during development).
	 *
	 * @throws IOException Thrown if the file interaction fails
	 */
	public void load()
		throws IOException
	{
		localFile = new File(new File(System.getProperty("user.home"), "." + PROPERTIES_FOLDER_NEW), PROPERTIES_FILE);

		InputStream stream = null;

		try
		{
			if (localFile.exists())
			{
				stream = new FileInputStream(localFile);
			}
			else
			{
				if (Barcodr.WITHIN_JAR)
					stream = PropertyReader.class.getResourceAsStream(PROPERTIES_FILE);
				else
					stream = new FileInputStream(new File("res", PROPERTIES_FILE));
			}

			properties.load(stream);
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		BarcodrParameterStore store = BarcodrParameterStore.getInstance();

		String value = getProperty(PROPERTY_PATH_IN);
		if (!StringUtils.isEmpty(value))
			store.put(BarcodrParameter.PATH_IN, value, String.class);

		value = getProperty(PROPERTY_PATH_OUT);
		if (!StringUtils.isEmpty(value))
			store.put(BarcodrParameter.PATH_OUT, value, String.class);
	}

	/**
	 * Stores the {@link Parameter}s from the {@link ParameterStore} to the {@link Properties} object and then saves it using {@link
	 * Properties#store(OutputStream, String)}.
	 *
	 * @throws IOException Thrown if the file interaction fails
	 */
	public void store()
		throws IOException
	{
		if (localFile == null)
			return;

		BarcodrParameterStore store = BarcodrParameterStore.getInstance();
		set(PROPERTY_PATH_IN, store.getAsString(BarcodrParameter.PATH_IN));
		set(PROPERTY_PATH_OUT, store.getAsString(BarcodrParameter.PATH_OUT));

		localFile.getParentFile().mkdirs();
		localFile.createNewFile();
		properties.store(new FileOutputStream(localFile), null);
	}
}
