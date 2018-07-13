/*
 * Copyright 2017 Sebastian Raubach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jhi.barcodr.util;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import jhi.barcodr.gui.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public class Resources
{
	/**
	 * Disposes all {@link Image}s that were loaded during execution (if they haven't already been disposed)
	 */
	public static void disposeResources()
	{
		Images.disposeAll();
		Colors.disposeAll();
	}

	/**
	 * {@link Fonts} is a utility class to handle {@link Font}s
	 *
	 * @author Sebastian Raubach
	 */
	public static class Fonts
	{
		/**
		 * Applies the given {@link Font} size to the given {@link Control}. A new {@link Font} instance is created internally, but disposed via a
		 * {@link Listener} for {@link SWT#Dispose} attached to the {@link Control}.
		 *
		 * @param control  The {@link Control}
		 * @param fontSize The {@link Font} size
		 */
		public static void applyFontSize(Control control, int fontSize)
		{
			/* Increase the font size */
			FontData[] fontData = control.getFont().getFontData();
			fontData[0].setHeight(fontSize);
			final Font font = new Font(control.getShell().getDisplay(), fontData[0]);
			control.setFont(font);

			control.addListener(SWT.Dispose, event ->
			{
				if (!font.isDisposed())
					font.dispose();
			});
		}
	}

	public static class Images
	{
		private static Map<String, Image> CACHE = new HashMap<>();

		public static Image LOGO       = getImage("img/logo.png");
		public static Image GITHUB     = getImage("img/github.png");
		public static Image EMAIL      = getImage("img/email.png");
		public static Image LOGO_SMALL = ResourceUtils.resize(getImage("img/logo.png", false), 100, 100);

		public static Image getImage(String path, boolean cache)
		{
			Image result = cache ? CACHE.get(path) : null;

			if (result == null)
			{
				if (Barcodr.WITHIN_JAR)
				{
					InputStream stream = Resources.class.getClassLoader().getResourceAsStream(path);
					if (stream != null)
					{
						result = new Image(null, stream);
					}
				}
				else
				{
					result = new Image(null, path);
				}

				if (result != null && cache)
					CACHE.put(path, result);
			}

			return result;
		}

		public static ImageData convertToSWT(BufferedImage bufferedImage)
		{
			if (bufferedImage.getColorModel() instanceof DirectColorModel)
			{
				DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
				PaletteData palette = new PaletteData(
					colorModel.getRedMask(),
					colorModel.getGreenMask(),
					colorModel.getBlueMask());
				ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
				for (int y = 0; y < data.height; y++)
				{
					for (int x = 0; x < data.width; x++)
					{
						int rgb = bufferedImage.getRGB(x, y);
						int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
						data.setPixel(x, y, pixel);
						if (colorModel.hasAlpha())
						{
							data.setAlpha(x, y, (rgb >> 24) & 0xFF);
						}
					}
				}
				return data;
			}
			else if (bufferedImage.getColorModel() instanceof IndexColorModel)
			{
				IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
				int size = colorModel.getMapSize();
				byte[] reds = new byte[size];
				byte[] greens = new byte[size];
				byte[] blues = new byte[size];
				colorModel.getReds(reds);
				colorModel.getGreens(greens);
				colorModel.getBlues(blues);
				RGB[] rgbs = new RGB[size];
				for (int i = 0; i < rgbs.length; i++)
				{
					rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
				}
				PaletteData palette = new PaletteData(rgbs);
				ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
				data.transparentPixel = colorModel.getTransparentPixel();
				WritableRaster raster = bufferedImage.getRaster();
				int[] pixelArray = new int[1];
				for (int y = 0; y < data.height; y++)
				{
					for (int x = 0; x < data.width; x++)
					{
						raster.getPixel(x, y, pixelArray);
						data.setPixel(x, y, pixelArray[0]);
					}
				}
				return data;
			}
			else if (bufferedImage.getColorModel() instanceof ComponentColorModel)
			{
				ComponentColorModel colorModel = (ComponentColorModel) bufferedImage.getColorModel();
				//ASSUMES: 3 BYTE BGR IMAGE TYPE
				PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
				ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
				//This is valid because we are using a 3-byte Data model with no transparent pixels
				data.transparentPixel = -1;
				WritableRaster raster = bufferedImage.getRaster();
				int[] pixelArray = new int[3];
				for (int y = 0; y < data.height; y++)
				{
					for (int x = 0; x < data.width; x++)
					{
						raster.getPixel(x, y, pixelArray);
						int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
						data.setPixel(x, y, pixel);
					}
				}
				return data;
			}
			return null;
		}

		public static Image getImage(String path)
		{
			return getImage(path, true);
		}

		public static void disposeAll()
		{
			CACHE.values()
				 .stream()
				 .filter(i -> !i.isDisposed())
				 .forEach(Image::dispose);
		}
	}

	/**
	 * {@link Colors} is a utility class to handle {@link Color}s
	 *
	 * @author Sebastian Raubach
	 */
	public static class Colors
	{
		/** Color cache */
		private static final Map<String, Color> CACHE = new HashMap<>();

		public static Color HIGHLIGHT = loadColor("#ffffff");

		/**
		 * Loads and returns the {@link Color} with the given hex
		 *
		 * @param color The color hex
		 * @return The {@link Color} object
		 */
		public static Color loadColor(String color)
		{
			Color newColor = CACHE.get(color);
			if (newColor == null || newColor.isDisposed())
			{
				java.awt.Color col;
				try
				{
					col = java.awt.Color.decode(color);
				}
				catch (Exception e)
				{
					col = java.awt.Color.WHITE;
				}
				int red = col.getRed();
				int blue = col.getBlue();
				int green = col.getGreen();

				newColor = new Color(null, red, green, blue);

				CACHE.put(color, newColor);
			}

			return newColor;
		}

		private static void disposeAll()
		{
			CACHE.values()
				 .stream()
				 .filter(col -> !col.isDisposed())
				 .forEach(Color::dispose);

			CACHE.clear();
		}
	}
}
