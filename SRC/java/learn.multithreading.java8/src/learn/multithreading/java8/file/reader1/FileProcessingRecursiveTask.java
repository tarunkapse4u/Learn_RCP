package learn.multithreading.java8.file.reader1;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileProcessingRecursiveTask extends RecursiveTask<List<String>>
{
   private static final long serialVersionUID = 1L;
   //This attribute will store the full path of the folder this task is going to process.
   private final String      path;
   //This attribute will store the name of the containsText of the files this task is going to look for.
   private final String      containsText;

   //Implement the constructor of the class to initialize its attributes
   public FileProcessingRecursiveTask(String path, String containsText)
   {
      this.path = path;
      this.containsText = containsText;
   }

   //Implement the compute() method. As you parameterized the RecursiveTask class with the List<String> type, 
   //this method has to return an object of that type.
   @Override
   protected List<String> compute()
   {
      //List to store the names of the files stored in the folder.
      List<String> list = new ArrayList<String>();
 	 try {
		 list.addAll(searchForPattern(path));
	} catch (IOException |NullPointerException e) {
		e.printStackTrace();
	}
      //add to the list of files the results returned by the subtasks launched by this task.
      //Return the list of strings
      return list;
   }


   //This method check if contains containsText you are looking for.
   private  List<String> searchForPattern(String name) throws IOException {
	    try (Stream<String> lines = Files.lines(Paths.get(name))) {
	    	return lines
				.filter(s -> s.contains(containsText))
				.collect(Collectors.toList());
	    }catch (Exception e) {
			// TODO: handle exception
		}
		return new ArrayList<String>();
	}
}