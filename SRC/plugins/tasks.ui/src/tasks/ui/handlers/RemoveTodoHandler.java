package tasks.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import tasks.model.Task;
import tasks.model.TaskService;

public class RemoveTodoHandler {
    @Execute
	public void execute(TaskService taskService,
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<Task> tasks,
            @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
        if (tasks != null) {
			tasks.forEach(t -> taskService.delete(t.getId()));
        } else {
            MessageDialog.openInformation(shell, "Deletion not possible", "No todo selected");
        }
    }
}