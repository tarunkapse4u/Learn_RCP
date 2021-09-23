package tasks.services.internal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;

import tasks.events.TaskEventConstants;
import tasks.model.Task;
import tasks.model.TaskService;

public class TransientTaskServiceImpl implements TaskService
{

    @Inject // <.>
    public IEventBroker broker;

    private static AtomicInteger current = new AtomicInteger(1);
    private List<Task> tasks;

    public TransientTaskServiceImpl()
    {
        tasks = createTestData();
    }

    @Override
    public void consume(final Consumer<List<Task>> taskConsumer)
    {
        // Simulate Server access delay
        //		try {
        //			TimeUnit.SECONDS.sleep(5);
        //		} catch (InterruptedException e) {
        //			e.printStackTrace();
        //		}
        // always pass a new copy of the data
        taskConsumer.accept(tasks.stream().map(Task::copy).collect(Collectors.toList()));
    }

    // create or update an existing instance of object
    @Override
    public synchronized boolean update(final Task newTask)
    {
        // hold the Optional object as reference to determine, if the object is
        // newly created or not
        Optional<Task> taskOptional = findById(newTask.getId());

        // get the actual object or create a new one
        Task task = taskOptional.orElse(new Task(current.getAndIncrement()));
        task.setSummary(newTask.getSummary());
        task.setDescription(newTask.getDescription());
        task.setDone(newTask.isDone());
        task.setDueDate(newTask.getDueDate());

        if (!taskOptional.isPresent())
        {
            tasks.add(task);
            Map<String, Object> map = new HashMap<>();
            map.put(TaskEventConstants.TOPIC_TASKS_NEW, TaskEventConstants.TOPIC_TASKS_NEW);
            map.put(Task.FIELD_ID, task.getId());

            broker.post(TaskEventConstants.TOPIC_TASKS_NEW, map); // <.>
        }
        else
        {
            Map<String, Object> map = new HashMap<>();
            map.put(TaskEventConstants.TOPIC_TASKS, TaskEventConstants.TOPIC_TASKS_UPDATE);
            map.put(Task.FIELD_ID, task.getId());
            broker.post(TaskEventConstants.TOPIC_TASKS_UPDATE, map); // <.>
        }
        return true;
    }

    @Override
    public Optional<Task> get(final long id)
    {
        return findById(id).map(Task::copy);
    }

    @Override
    public boolean delete(final long id)
    {
        Optional<Task> deletedTask = findById(id);
        deletedTask.ifPresent(t -> {
            tasks.remove(t);

            Map<String, Object> map = new HashMap<>();
            map.put(TaskEventConstants.TOPIC_TASKS, TaskEventConstants.TOPIC_TASKS_DELETE);
            map.put(Task.FIELD_ID, t.getId());
            broker.post(TaskEventConstants.TOPIC_TASKS_DELETE, map); // <.>
        });

        return deletedTask.isPresent();
    }

    // Example data, change if you like
    private List<Task> createTestData()
    {
        List<Task> list1 = new ArrayList<>();
        list1.add(create("Application model", "Dynamics"));
        list1.add(create("Application model", "Flexible and extensible"));
        list1.add(create("DI", "@Inject as programming mode"));
        list1.add(create("OSGi", "Services"));
        list1.add(create("SWT", "Widgets"));
        list1.add(create("JFace", "Especially Viewers!"));
        list1.add(create("CSS Styling", "Style your application"));
        list1.add(create("Eclipse services", "Selection, model, Part"));
        list1.add(create("Renderer", "Different UI toolkit"));
        list1.add(create("Compatibility Layer", "Run Eclipse 3.x"));
        return new ArrayList<>(list1);
    }

    private Task create(final String summary, final String description)
    {
        return new Task(current.getAndIncrement(), summary, description, false, LocalDate.now().plusDays(current.longValue()));
    }

    private Optional<Task> findById(final long id)
    {
        return tasks.stream().filter(t -> t.getId() == id).findAny();
    }

    @Override
    public List<Task> getAll()
    {
        return tasks.stream().map(Task::copy).collect(Collectors.toList());
    }

}
