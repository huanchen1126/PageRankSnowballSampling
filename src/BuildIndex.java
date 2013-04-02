import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Build index for the data
 * */
public class BuildIndex {
  public static void buildIndex(String input, String indexPath) {
    RandomAccessFile raf = null;
    FileWriter fw = null;
    try {
      raf = new RandomAccessFile(input, "r");
      fw = new FileWriter(indexPath);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    BufferedWriter bw = new BufferedWriter(fw);
    String line = null;
    try {
      long pos = raf.getFilePointer();
      while ((line = raf.readLine()) != null) {
        int end = line.indexOf('\t');
        String node = line.substring(0, end);
        bw.write(node + "\t" + pos + "\n");
        pos = raf.getFilePointer();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        raf.close();
        bw.close();
        fw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
