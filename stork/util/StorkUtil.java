package stork.util;

import java.util.*;
import java.util.regex.*;

// A bunch of static utility functions, how fun!

public class StorkUtil {
  // Some pre-compiled regexes.
  public static final Pattern
    regex_ws   = Pattern.compile("\\s+"),
    regex_csv  = Pattern.compile("\\s*(,\\s*)+"),
    regex_norm = Pattern.compile("[^a-z_0-9\\Q-_+,.\\E]+"),
    regex_path = Pattern.compile("[^^]/+");

  private StorkUtil() { /* I sure can't be instantiated. */ }

  // String functions
  // ----------------
  // All of these functions should take null and treat it like "".

  // Normalize a string by lowercasing it, replacing spaces with _,
  // and removing characters other than alphanumerics or: -_+.,
  public static String normalize(String s) {
    if (s == null) return "";

    s = s.toLowerCase();
    s = regex_norm.matcher(s).replaceAll(" ").trim();
    s = regex_ws.matcher(s).replaceAll("_");

    return s;
  }

  // Split a CSV string into an array of normalized strings.
  public static String[] splitCSV(String s) {
    String[] a = regex_csv.split(normalize(s), 0);
    return (a == null) ? new String[0] : a;
  }

  // Collapse a string array back into a CSV string.
  public static String joinCSV(Object... sa) {
    return joinWith(", ", sa);
  }

  // Join a string with spaces.
  public static String join(Object... sa) {
    return joinWith(" ", sa);
  }

  // Join a string array with a delimiter.
  public static String joinWith(String del, Object... sa) {
    StringBuffer sb = new StringBuffer();
    
    if (del == null) del = "";

    if (sa != null && sa.length != 0) {
      sb.append(sa[0]);
      for (int i = 1; i < sa.length; i++)
        if (sa[i] != null) sb.append(del+sa[i]);
    } return sb.toString();
  }

  // Wrap a paragraph to some number of characters.
  public static String wrap(String str, int w) {
    StringBuffer sb = new StringBuffer();
    String line = "";

    for (String s : regex_ws.split(str)) {
      if (!line.isEmpty() && line.length()+s.length() >= w) {
        if (sb.length() != 0) sb.append('\n');
        sb.append(line);
        line = s;
      } else {
        line = (line.isEmpty()) ? s : line+' '+s;
      }
    }

    if (!line.isEmpty()) {
      if (sb.length() != 0) sb.append('\n');
      sb.append(line);
    }

    return sb.toString();
  }


  // Path functions
  // --------------
  // Functions that operate on path strings. Like string functions, should
  // treat null inputs as an empty string.

  // Split a path into its components. The first element will be a slash
  // if it's an absolute path, and the last element will be an empty
  // string if this path represents a directory.
  public static String[] splitPath(String path) {
    return regex_path.split((path != null) ? path : "", -1);
  }

  // Get the basename from a path string.
  public static String basename(String path) {
    if (path == null) return "";

    int i = path.lastIndexOf('/');

    return (i == -1) ? path : path.substring(i+1);
  }

  // Get the dirname from a path string, including trailing /.
  public static String dirname(String path) {
    if (path == null) return "";

    int i = path.lastIndexOf('/');

    return (i == -1) ? "" : path.substring(0, i);
  }
}
