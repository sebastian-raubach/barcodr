package jhi.barcodr.gui.dialog;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public class OptionDialog extends I18nDialog
{
	private List<String> options;
	private int          selection = 0;

	public OptionDialog(Shell shell, List<String> options)
	{
		super(shell);

		this.options = options;
	}

	public OptionDialog(Shell shell, String... options)
	{
		super(shell);

		this.options = Arrays.asList(options);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);

		newShell.setText("Choose option");
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

		Group group = new Group(composite, SWT.NONE);
		group.setText("Options");

		for (String option : options)
		{
			Button button = new Button(group, SWT.RADIO);
			button.setText(option);
			button.addListener(SWT.Selection, event -> selection = options.indexOf(option));
		}

		GridLayoutUtils.useValues(1, false).applyTo(composite);
		GridLayoutUtils.useValues(1, false).applyTo(group);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(group);

		return composite;
	}

	public int getSelection()
	{
		return selection;
	}
}
