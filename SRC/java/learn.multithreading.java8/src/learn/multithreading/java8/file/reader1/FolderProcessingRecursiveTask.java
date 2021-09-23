package learn.multithreading.java8.file.reader1;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class FolderProcessingRecursiveTask extends RecursiveTask<List<String>>
{
   private static final long serialVersionUID = 1L;
   //This attribute will store the full path of the folder this task is going to process.
   private final String      path;
   //This attribute will store the name of the extension of the files this task is going to look for.
   private final String      extension;

   private final String containsText;

   //Implement the constructor of the class to initialize its attributes
   public FolderProcessingRecursiveTask(String path, String extension, String containsText)
   {
      this.path = path;
      this.extension = extension;
      this.containsText=containsText;
   }

   //Implement the compute() method. As you parameterized the RecursiveTask class with the List<String> type, 
   //this method has to return an object of that type.
   @Override
   protected List<String> compute()
   {
      //List to store the names of the files stored in the folder.
      List<String> list = new ArrayList<String>();
      //FolderProcessingRecursiveTask tasks to store the subtasks that are going to process the subfolders stored in the folder
      List<FolderProcessingRecursiveTask> tasks = new ArrayList<FolderProcessingRecursiveTask>();
      List<FileProcessingRecursiveTask> filetasks = new ArrayList<FileProcessingRecursiveTask>();
      //Get the content of the folder.
      File file = new File(path);
      File content[] = file.listFiles();
      //For each element in the folder, if there is a subfolder, create a new FolderProcessingRecursiveTask object 
      //and execute it asynchronously using the fork() method.
      if (content != null)
      {
         for (int i = 0; i < content.length; i++)
         {
            if (content[i].isDirectory())
            {
               FolderProcessingRecursiveTask task = new FolderProcessingRecursiveTask(content[i].getAbsolutePath(), extension,containsText);
               task.fork();
               tasks.add(task);
            }
            //Otherwise, compare the extension of the file with the extension you are looking for using the checkFile() method 
            //and, if they are equal, store the full path of the file in the list of strings declared earlier.
            else
            {
               if (checkFile(content[i].getName()))
               {
                   FileProcessingRecursiveTask task = new FileProcessingRecursiveTask(content[i].getAbsolutePath(), containsText);
                   getPool().execute(task);
                   task.fork();
                   filetasks.add(task);
                }
            }
         }
      }
      //If the list of the FolderProcessingRecursiveTask subtasks has more than 50 elements, 
      //write a message to the console to indicate this circumstance.
      if (tasks.size() > 50)
      {
         System.out.printf("%s: %d tasks ran.\n", file.getAbsolutePath(), tasks.size());
      }
      //add to the list of files the results returned by the subtasks launched by this task.
      addResultsFromFileTasks(list, filetasks);
      addResultsFromTasks(list, tasks);
      //Return the list of strings
      return list;
   }

   //For each task stored in the list of tasks, call the join() method that will wait for its finalization and then will return the result of the task. 
   //Add that result to the list of strings using the addAll() method.
   private void addResultsFromTasks(List<String> list, List<FolderProcessingRecursiveTask> tasks)
   {
      for (FolderProcessingRecursiveTask item : tasks)
      {
         list.addAll(item.join());
      }
   }
   
   //For each task stored in the list of tasks, call the join() method that will wait for its finalization and then will return the result of the task. 
   //Add that result to the list of strings using the addAll() method.
   private void addResultsFromFileTasks(List<String> list, List<FileProcessingRecursiveTask> tasks)
   {
      for (FileProcessingRecursiveTask item : tasks)
      {
         list.addAll(item.join());
      }
   }

   //This method compares if the name of a file passed as a parameter ends with the extension you are looking for.
   private boolean checkFile(String name)
   {
      return name.endsWith(extension);
   }
}