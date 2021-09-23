package ide.editor.tasks;

import static ide.editor.tasks.Util.getActiveEditor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class TodoMarkerDocumentSetupExample implements IDocumentSetupParticipant {

	private static final String TODO_PROPERTY = "todoProperty";

	@Override
	public void setup(final IDocument document) {
		document.addDocumentListener(new IDocumentListener() {

			private Job markerJob;

			@Override
			public void documentChanged(final DocumentEvent event) {
				IEditorPart activeEditor = getActiveEditor();

				if (activeEditor != null) {
					IEditorInput editorInput = activeEditor.getEditorInput();
					IResource adapter = editorInput.getAdapter(IResource.class);
					if (markerJob != null) {
						markerJob.cancel();
					}
					markerJob = Job.create("Adding Marker", (ICoreRunnable) monitor -> createMarker(event, adapter));
					markerJob.setUser(false);
					markerJob.setPriority(Job.DECORATE);
					// set a delay before reacting to user action to handle continuous typing
					markerJob.schedule(500);
				}
			}

			@Override
			public void documentAboutToBeChanged(final DocumentEvent event) {
				// not needed
			}
		});
	}

	private void createMarker(final DocumentEvent event, final IResource adapter) throws CoreException {
		String docText = event.getDocument().get();
		try {
			List<IMarker> markers = Arrays.asList(adapter.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE));
			for (String todoProperty : TodoPropertiesContentAssistProcessor.PROPOSALS) {

				Optional<IMarker> findAny = markers.stream()
						.filter(m -> todoProperty.equals(m.getAttribute(TODO_PROPERTY, ""))).findAny();
				if (docText.contains(todoProperty) && findAny.isPresent()) {
					findAny.get().delete();
				} else if (!docText.contains(todoProperty) && !findAny.isPresent()) {
					createMarker(adapter, IMarker.SEVERITY_INFO, todoProperty + " property is not set", todoProperty);
				}

			}
//			String[] lines = docText.split("\\n");
//			for (String line : lines) {
//				String[] split = line.split(":");
//				try {
//					String todoProperty = split[0];
//					if (todoProperty.trim().isEmpty()) {
//						createMarker(adapter, IMarker.SEVERITY_ERROR, split[1] + " property not set", todoProperty);
//					}
//				} catch (ArrayIndexOutOfBoundsException e) {
//					// ignore
//				}
//
//				try {
//					String todoPropertyValue = split[1];
//					if (todoPropertyValue.trim().isEmpty()) {
//						createMarker(adapter, IMarker.SEVERITY_ERROR, split[0] + " property not set", todoPropertyValue);
//					}
//				} catch (ArrayIndexOutOfBoundsException e) {
//					// ignore
//				}
//
//			}
		} catch (NullPointerException e) {
			// ignore
		}
	}

	private void createMarker(final IResource adapter, int severity, String msg, String todoPropertyValue)
			throws CoreException {
		IMarker marker = adapter.createMarker(IMarker.PROBLEM);
		marker.setAttribute(IMarker.MESSAGE, msg);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.LOCATION, "Missing line");
		marker.setAttribute(TODO_PROPERTY, todoPropertyValue);
	}

}
