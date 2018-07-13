package jhi.barcodr.gui.dialog;

import com.google.zxing.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.*;

import jhi.barcodr.gui.*;
import jhi.barcodr.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public class ImagePathDialog extends I18nDialog
{
	private Text               source;
	private Text               target;
	private BarcodeComboViewer barcodes;

	private File          sourceFolder;
	private File          targetFolder;
	private BarcodeFormat format;


	private boolean isOkPressed = false;

	public ImagePathDialog(Shell shell)
	{
		super(shell);
	}

	@Override
	public boolean close()
	{
		if (!isOkPressed)
		{
			System.exit(0);
			return false;
		}
		else
		{
			isOkPressed = false;
			return super.close();
		}
	}

	@Override
	protected void okPressed()
	{
		if (sourceFolder != null && targetFolder != null)
		{
			BarcodrParameterStore store = BarcodrParameterStore.getInstance();
			store.put(BarcodrParameter.PATH_IN, sourceFolder.getAbsolutePath());
			store.put(BarcodrParameter.PATH_OUT, targetFolder.getAbsolutePath());
			format = barcodes.getSelectedItem();
			isOkPressed = true;
			super.okPressed();
		}
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);

		newShell.setText("Choose image folders");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Point getInitialLocation(Point initialSize)
	{
		return ShellUtils.getLocationCenteredTo(getParentShell(), initialSize);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);

		new Label(composite, SWT.NONE).setText("Image source folder");
		source = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		Button selectSource = new Button(composite, SWT.PUSH);
		selectSource.setText("Browse");

		selectSource.addListener(SWT.Selection, event -> {
			DirectoryDialog dialog = new DirectoryDialog(getShell());

			if (sourceFolder != null)
				dialog.setFilterPath(sourceFolder.getAbsolutePath());

			String path = dialog.open();

			if (!StringUtils.isEmpty(path))
			{
				source.setText(path);
				sourceFolder = new File(path);
			}
		});

		new Label(composite, SWT.NONE).setText("Image target folder");
		target = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		Button selectTarget = new Button(composite, SWT.PUSH);
		selectTarget.setText("Browse");

		selectTarget.addListener(SWT.Selection, event -> {
			DirectoryDialog dialog = new DirectoryDialog(getShell());

			if (targetFolder != null)
				dialog.setFilterPath(targetFolder.getAbsolutePath());
			else if (sourceFolder != null)
				dialog.setFilterPath(sourceFolder.getAbsolutePath());

			String path = dialog.open();

			if (!StringUtils.isEmpty(path))
			{
				target.setText(path);
				targetFolder = new File(path);
			}
		});

		new Label(composite, SWT.NONE).setText("Barcode type");
		barcodes = new BarcodeComboViewer(composite, SWT.READ_ONLY);

		GridLayoutUtils.useValues(3, false).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(source);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(selectSource);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(target);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(selectTarget);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(barcodes.getControl());

		BarcodrParameterStore store = BarcodrParameterStore.getInstance();

		String inPath = store.getAsString(BarcodrParameter.PATH_IN);
		if (!StringUtils.isEmpty(inPath) && new File(inPath).exists())
		{
			sourceFolder = new File(inPath);
			source.setText(sourceFolder.getAbsolutePath());
		}

		String outPath = store.getAsString(BarcodrParameter.PATH_OUT);
		if (!StringUtils.isEmpty(outPath) && new File(outPath).exists())
		{
			targetFolder = new File(outPath);
			target.setText(targetFolder.getAbsolutePath());
		}

		return composite;
	}

	public File getSource()
	{
		return sourceFolder;
	}

	public File getTarget()
	{
		return targetFolder;
	}

	public BarcodeFormat getFormat()
	{
		return format;
	}
}
