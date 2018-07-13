package jhi.barcodr.util;

import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public enum BarcodrParameter implements Parameter
{
	PATH_IN(String.class),
	PATH_OUT(String.class);

	private Class<?> type;

	private BarcodrParameter(Class<?> type)
	{
		this.type = type;
	}

	@Override
	public Class<?> getType()
	{
		return type;
	}
}
