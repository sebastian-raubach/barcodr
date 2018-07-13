package jhi.barcodr.gui;

import com.google.zxing.*;
import com.google.zxing.Reader;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.*;
import com.google.zxing.oned.*;
import com.google.zxing.qrcode.*;

import net.coobird.thumbnailator.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.fieldassist.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.*;

import jhi.barcodr.gui.dialog.*;
import jhi.barcodr.i18n.*;
import jhi.barcodr.util.PropertyReader;
import jhi.barcodr.util.*;
import jhi.swtcommons.gui.*;
import jhi.swtcommons.util.StringUtils;

/**
 * @author Sebastian Raubach
 */
public class Barcodr extends RestartableApplication
{
	/** Indicates whether the application is run form a jar or not */
	public static boolean WITHIN_JAR;

	private Composite         composite;
	private Text              currentBarcode;
	private ControlDecoration barcodeDecorator;
	private Image             currentImage;
	private Label             imageLabel;
	private Button            isBarcode;
	private Button            nextImage;

	private BarcodeFormat format = BarcodeFormat.CODE_128;

	private String barcode;
	private int    index;

	private File in;
	private File out;

	private Reader                            reader;
	//	private GenericMultipleBarcodeReader      greader;
	private Hashtable<DecodeHintType, Object> decodeHints;
	private File[]                            imageFiles;

	private Listener filter;

	public static void main(String[] args)
	{
		/* Check if we are running from within a jar or the IDE */
		WITHIN_JAR = Barcodr.class.getResource(Barcodr.class.getSimpleName() + ".class").toString().startsWith("jar");

		new Barcodr();
	}

	@Override
	protected PropertyReader getPropertyReader()
	{
		return new PropertyReader();
	}

	@Override
	protected void onStart()
	{
		index = -1;
		shell.setText(RB.getString(RB.APPLICATION_TITLE));
		composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		currentBarcode = new Text(composite, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		data.horizontalSpan = 2;
		currentBarcode.setLayoutData(data);

		currentBarcode.addListener(SWT.Modify, event -> {
			barcode = currentBarcode.getText();
			System.out.println(barcode);
		});

		barcodeDecorator = new ControlDecoration(currentBarcode, SWT.TOP | SWT.RIGHT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		Image img = fieldDecoration.getImage();
		barcodeDecorator.setDescriptionText("No barcode found!");
		barcodeDecorator.setImage(img);
		barcodeDecorator.hide();

		imageLabel = new Label(composite, SWT.CENTER);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		imageLabel.setLayoutData(data);
		imageLabel.addListener(SWT.Resize, new Listener()
		{
			long lastResize = System.currentTimeMillis();

			@Override
			public void handleEvent(Event event)
			{
				long currentResize = System.currentTimeMillis();
				lastResize = currentResize;
				Display.getDefault().timerExec(100, () -> {
					if (lastResize == currentResize)
						loadCurrentImage();
				});
			}
		});

		isBarcode = new Button(composite, SWT.PUSH);
		isBarcode.setText("Get barcode");
		isBarcode.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		nextImage = new Button(composite, SWT.PUSH);
		nextImage.setText("Next");
		nextImage.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		isBarcode.addListener(SWT.Selection, event -> {
			isBarcode.setEnabled(false);
			nextImage.setEnabled(false);
			getBarcode(imageFiles[index]);

			if (StringUtils.isEmpty(barcode))
			{
				removeFilter();
				OptionDialog options = new OptionDialog(shell, "Try harder", "Manual entry");

				if (options.open() == Window.OK)
				{
					switch (options.getSelection())
					{
						case 0:
							decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
							getBarcode(imageFiles[index]);

							if (!StringUtils.isEmpty(barcode))
							{
								nextImage();
								// Break here, because we don't need the manual input
								break;
							}
							// ELSE DON'T BREAK HERE
						case 1:

							InputDialog dialog = new InputDialog(shell, "Manual input", "Please type in the barcode", null, null);

							if (dialog.open() == Window.OK)
								barcode = dialog.getValue();

							nextImage();
							break;
					}
				}
				addFilters();
			}
			else
			{
				nextImage();
			}
			isBarcode.setEnabled(true);
			nextImage.setEnabled(true);
		});
		nextImage.addListener(SWT.Selection, event -> {
			isBarcode.setEnabled(false);
			nextImage.setEnabled(false);
			if (index >= 0)
			{
				try
				{
					processImage(imageFiles[index]);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (index + 1 == imageFiles.length)
				System.exit(0);

			nextImage();
			isBarcode.setEnabled(true);
			nextImage.setEnabled(true);
		});

		ImagePathDialog dialog = new ImagePathDialog(shell);

		if (dialog.open() == Window.OK)
		{
			in = dialog.getSource();
			out = dialog.getTarget();
			format = dialog.getFormat();

			switch (format)
			{
				case CODE_128:
					reader = new Code128Reader();
					break;
				case CODE_39:
					reader = new Code39Reader();
					break;
				case EAN_13:
					reader = new EAN13Reader();
					break;
				case EAN_8:
					reader = new EAN8Reader();
					break;
				case UPC_A:
					reader = new UPCAReader();
					break;
				case QR_CODE:
					reader = new QRCodeReader();
					break;
				case RSS_EXPANDED:
				default:
					reader = new MultiFormatReader();
					break;
			}

//			greader = new GenericMultipleBarcodeReader(reader);
			decodeHints = new Hashtable<>();

			out.mkdirs();

			imageFiles = loadImages();
		}

		createMenuBar();
	}

	/**
	 * Creates the {@link Menu}.
	 */
	private void createMenuBar()
	{
		Menu oldMenu = shell.getMenuBar();
		if (oldMenu != null && !oldMenu.isDisposed())
			oldMenu.dispose();

		Menu menuBar = new Menu(shell, SWT.BAR);
		Menu fileMenu = new Menu(menuBar);
		final Menu aboutMenu = new Menu(menuBar);

		/* File */
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(RB.getString(RB.MENU_MAIN_FILE));
		item.setMenu(fileMenu);

		/* Help */
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(RB.getString(RB.MENU_MAIN_HELP));
		item.setMenu(aboutMenu);

		/* File - Exit */
		addQuitMenuItemListener(RB.getString(RB.MENU_MAIN_FILE_EXIT), fileMenu, e -> shutdown());

		/* Help - About */
		addAboutMenuItemListener(RB.getString(RB.MENU_MAIN_HELP_ABOUT), aboutMenu, e -> new AboutDialog(shell).open());

		shell.setMenuBar(menuBar);
	}

	private void nextImage()
	{
		if (index >= 0)
		{
			boolean found = !StringUtils.isEmpty(barcode);

			if (found)
			{
				currentBarcode.setText(barcode);
				barcodeDecorator.hide();
			}
			else
			{
				currentBarcode.setText("");
				barcodeDecorator.show();
			}
		}

		index++;

		loadCurrentImage();
	}

	private void loadCurrentImage()
	{
		File image = imageFiles[index];

		if (currentImage != null && !currentImage.isDisposed())
			currentImage.dispose();

		Point size = imageLabel.getSize();

		try
		{
			BufferedImage bi = Thumbnails.of(image)
										 .height(size.y)
										 .width(size.x)
										 .keepAspectRatio(true)
										 .asBufferedImage();

			currentImage = new Image(null, Resources.Images.convertToSWT(bi));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		imageLabel.setImage(currentImage);
	}

	private void processImage(File file)
		throws IOException
	{
		if (StringUtils.isEmpty(barcode))
		{
			System.out.println(file.getAbsolutePath());
		}
		else
		{
			File target;

			int i = 1;
			do
			{
				target = new File(out, barcode + "-" + i + ".jpg");
				i++;
			} while (target.exists());

			Files.copy(file.toPath(), target.toPath());
		}
	}

	private void getBarcode(File file)
	{
		barcode = null;

		try
		{
			BufferedImage bfi = ImageIO.read(file);
			LuminanceSource ls = new BufferedImageLuminanceSource(bfi);
			BinaryBitmap bmp = new BinaryBitmap(new HybridBinarizer(ls));
			Result res = reader.decode(bmp, decodeHints);

			if (res != null)
			{
				barcode = res.getText();
			}
		}
		catch (IOException | NotFoundException | FormatException | ChecksumException e)
		{
			e.printStackTrace();
		}

		decodeHints.clear();
	}

	private File[] loadImages()
	{
		return in.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".jpg"));
	}

	@Override
	protected void onExit()
	{

	}

	@Override
	protected void initResources()
	{

	}

	@Override
	protected void disposeResources()
	{

	}

	@Override
	protected void onPostOpen()
	{
		super.onPostOpen();

		filter = event -> {
			if (!currentBarcode.isFocusControl())
			{
				if (event.character == 32)
				{
					nextImage.notifyListeners(SWT.Selection, new Event());
				}
				else if (event.keyCode == 'b')
				{
					isBarcode.notifyListeners(SWT.Selection, new Event());
				}
			}
		};

		addFilters();

		nextImage();
	}

	private void addFilters()
	{
		display.addFilter(SWT.KeyUp, filter);

		imageLabel.forceFocus();
	}

	private void removeFilter()
	{
		display.removeFilter(SWT.KeyUp, filter);
	}
}
