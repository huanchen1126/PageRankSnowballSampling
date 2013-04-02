import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ApproximatePageRankSnowBallSampling {
  public static HashMap<String, Double> p = null;

  public static HashMap<String, Double> r = null;

  public static String dataPath = null;

  public static double alpha = 0.0;

  public static double epsilon = 0.0;

  public static String start = null;

  public static void init(String path, String s, double a, double e) {
    dataPath = path;
    alpha = a;
    epsilon = e;
    p = new HashMap<String, Double>();
    r = new HashMap<String, Double>();
    start = s;
    r.put(start, (double) 1);
  }

  /**
   * compute the personalized pagerank wrt start
   * */
  public static void calculatePr() {
    boolean hasUpdate = true;
    while (hasUpdate) {
      /* continue until no vertex satisfy the condition */
      hasUpdate = false;
      /* get the current keys in r */
      String[] keys = r.keySet().toArray(new String[r.keySet().size()]);
      for (int i = 0; i < keys.length; i++) {
        String key = keys[i];
        /* get the ending vertex of edges */
        String[] ends = getData(key);
        if (ends != null) {
          int degree = ends.length;
          if (r.get(key) / degree > epsilon) {
            updateRP(ends, key);
            /* continue */
            hasUpdate = true;
          }
        } else {
          /* if this key has no out degree */
          updateRP(ends, key);
        }
      }
    }
    for (String key : p.keySet()) {
      System.out.println(key + ":" + p.get(key));
    }
  }

  /**
   * helper method for computing pagerank update the P and R
   * 
   * @param ends
   * @param key
   */
  public static void updateRP(String[] ends, String key) {
    /* update p */
    if (p.containsKey(key))
      p.put(key, p.get(key) + r.get(key) * alpha);
    else
      p.put(key, r.get(key) * alpha);
    /* update r */
    double tmp = r.get(key);
    r.put(key, (double) 0);

    /* if this key has out degree */
    if (ends != null) {
      int degree = ends.length;
      for (String end : ends) {
        if (r.containsKey(end))
          r.put(end, r.get(end) + (1 - alpha) * tmp / degree);
        else
          r.put(end, (1 - alpha) * tmp / degree);
      }
    }
  }

  /**
   * read data
   * 
   * @param key
   * @return
   */
  public static String[] getData(String key) {
    FileInputStream fis = null;
    BufferedReader br = null;
    try {
      fis = new FileInputStream(dataPath);
      br = new BufferedReader(new InputStreamReader(fis));
      String line = "";
      while ((line = br.readLine()) != null) {
        int ind = line.indexOf('\t');
        String k = line.substring(0, ind);
        if (k.equals(key)) {
          return line.substring(ind + 1).split("\t");
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        br.close();
        fis.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * do snow ball sampling
   * 
   * @return
   */
  public static Set<String> snowBallSampling() {
    Set<String> result = new HashSet<String>();
    /* add the initial node to the set */
    result.add(start);

    ArrayList<Map.Entry<String, Double>> nodes = new ArrayList<Map.Entry<String, Double>>(
            p.entrySet());
    Collections.sort(nodes, new Comparator<Map.Entry<String, Double>>() {
      public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
        if (o1.getValue() > o2.getValue())
          return -1;
        if (o1.getValue() < o2.getValue())
          return 1;
        return 0;
      }
    });
    /* get the edges in the initial set */
    String[] ends = getData(start);
    /* key is a node, value is number of edges with key as ending node */
    HashMap<String, Long> edges = new HashMap<String, Long>();
    /* update the edges */
    for (String end : ends) {
      if (edges.containsKey(end))
        edges.put(end, edges.get(end) + 1);
      else
        edges.put(end, (long) 1);
    }

    long volume = ends.length;
    long boundary = ends.length;
    double c1 = (double) boundary / volume;
    for (Entry<String, Double> node : nodes) {
      String k = node.getKey();
      if (k.equals(start))
        continue;
      String[] tmpends = getData(k);
      volume = volume + (tmpends == null ? 0 : tmpends.length);
      boundary = boundary - (edges.containsKey(k) ? edges.get(k) : 0);
      if (tmpends != null) {
        for (String end : tmpends) {
          if (!result.contains(end)) {
            boundary++;
          }
        }
      }
      double c2 = (double) boundary / volume;
      System.out.println(node.getKey() + ":" + node.getValue() + " pre:" + c1 + " cur:" + c2);
      /* if c2<c1 update the result set and the edges counter */
      if (c2 < c1) {
        c1 = c2;
        /* update the result set */
        result.add(k);
        /* update the edges counter */
        if (tmpends != null) {
          for (String end : tmpends) {
            if (edges.containsKey(end))
              edges.put(end, edges.get(end) + 1);
            else
              edges.put(end, (long) 1);
          }
        }
      }
    }
    for (String s : result)
      System.out.println(s);
    return result;
  }

  /**
   * generate GDF graph for visualization
   */
  public static void generateGDF(Set<String> set) {

  }

  public static void main(String[] args) {
    init("/Users/huanchen/Documents/pr_data/test.adj", "A", 0.3, 1e-5);
    calculatePr();
    snowBallSampling();
  }
}
