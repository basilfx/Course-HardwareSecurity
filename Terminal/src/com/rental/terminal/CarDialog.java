package com.rental.terminal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CarDialog extends Dialog {
    protected Object result;
    protected Shell shell;

    public CarDialog(Shell parent, int style) {
        super(parent, style);
    }

    public CarDialog(Shell parent) {
        this(parent, SWT.NONE);
    }

    public Object open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    protected void createContents() {
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setSize(450, 300);
        shell.setText("SWT Dialog");
    }
}
