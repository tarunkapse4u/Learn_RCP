
package tasks.ui.handlers;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;

@SuppressWarnings("restriction")
public class TaskOpenFileHandler
{
    public TaskOpenFileHandler()
    {
        System.out.println("Initialized");
    }

	@Execute
    public void execute()
    {

        File file = openFile();
        if (file != null)
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            try
            {
                IFileStore fileStore = EFS.getStore(file.toURI());
                IEditorInput input = new FileStoreEditorInput(fileStore);
                page.openEditor(input, ExtensionBasedTextEditor.GENERIC_EDITOR_ID);
            }
            catch (CoreException e)
            {
                e.printStackTrace();
            }
            
            MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "TEXTEDITOR",
                    "In open toolbar and action error marker and content assist is not implemented.");
        }
    }

    @CanExecute
    public boolean check()
    {
        return true;
    }

    /** Show OpenFile dialog to select a file */
    private File openFile()
    {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        // opens dialog to select file
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.*" });
        dialog.setFilterNames(new String[] { "All files" });
        String fileSelected = dialog.open();
        if (fileSelected != null && fileSelected.length() > 0)
        {
            // Perform action, opens the file
            System.out.println("Selected file: " + fileSelected);
            return new File(fileSelected);
        }
        return null;
    }

}
