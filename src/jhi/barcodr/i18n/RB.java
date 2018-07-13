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

package jhi.barcodr.i18n;

import java.util.*;

/**
 * {@link RB} wraps the {@link ResourceBundle} of this application. Use {@link #getString(String, Object...)} and the constants of this class to
 * access the resources.
 *
 * @author Sebastian Raubach
 */
public class RB extends jhi.swtcommons.gui.i18n.RB
{
	public static final String APPLICATION_TITLE           = "application.title";
	public static final String APPLICATION_TITLE_NO_SPACES = "application.title.no.spaces";

	public static final String MENU_MAIN_FILE       = "menu.main.file";
	public static final String MENU_MAIN_FILE_EXIT  = "menu.main.file.exit";
	public static final String MENU_MAIN_HELP       = "menu.main.help";
	public static final String MENU_MAIN_HELP_ABOUT = "menu.main.help.about";

	public static final String DIALOG_TITLE_ABOUT       = "dialog.about.title";
	public static final String DIALOG_ABOUT_MESSAGE     = "dialog.about.message";
	public static final String DIALOG_ABOUT_COPYRIGHT   = "dialog.about.copyright";
	public static final String DIALOG_ABOUT_TAB_ABOUT   = "dialog.about.tab.about";
	public static final String DIALOG_ABOUT_TAB_LICENSE = "dialog.about.tab.license";

	public static final String ERROR_ABOUT_LICENSE = "error.about.license";

	public static final String URL_GITHUB = "url.github";
	public static final String URL_EMAIL  = "url.email";


	public static final List<Locale> SUPPORTED_LOCALES = new ArrayList<>();

	static
	{
		SUPPORTED_LOCALES.add(Locale.ENGLISH);
		SUPPORTED_LOCALES.add(Locale.GERMAN);
	}
}
