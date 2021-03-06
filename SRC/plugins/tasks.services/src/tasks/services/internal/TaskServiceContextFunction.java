package tasks.services.internal;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.osgi.service.component.annotations.Component;

import tasks.model.TaskService;

@Component(service = IContextFunction.class, property = "service.context.key=tasks.model.TaskService")
public class TaskServiceContextFunction extends ContextFunction {
    @Override
    public Object compute(IEclipseContext context, String contextKey) {
		TaskService todoService = ContextInjectionFactory.make(TransientTaskServiceImpl.class, context);

		MApplication app = context.get(MApplication.class);
		IEclipseContext appCtx = app.getContext();
		appCtx.set(TaskService.class, todoService);

		return todoService;
    }
}