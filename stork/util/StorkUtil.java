package stork.util;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.URI;

// A bunch of static utility functions, how fun!

public abstract class StorkUtil {
  // Some pre-compiled regexes.
  public static final Pattern
    regex_ws   = Pattern.compile("\\s+"),
    regex_csv  = Pattern.compile("\\s*(,\\s*)+"),
    regex_norm = Pattern.compile("[^a-z_0-9\\Q-_+,.\\E]+"),
    regex_path = Pattern.compile("[^^]/+");

  // Static imports
  // --------------
  // Static methods with small names meant to be statically imported
  // into classes
  public static class Static {
    // Join objects into a string.
    public static String J(Object... sa) {
      return join(sa);
    }

    // Join objects into a string with delimiter.
    public static String JW(String d, Object... sa) {
      return joinWith(d, sa);
    }

    // Print debugging output.
    public static void D(Object... o) {
      System.out.println(join(o));
    }
  }

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
    if (s == null) s = "";
    String[] a = regex_csv.split(s, 0);
    for (int i = 0; i < a.length; i++)
      a[i] = normalize(a[i]);
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

  // File system functions
  // ---------------------
  // Functions to get information about the local file system.
  
  // Get an XferList a local path. If it's a directory, does a
  // recursive listing.
  public static XferList list(String path) throws Exception {
    File file = new File(path);

    if (!file.isDirectory())
      return new XferList(path, path, file.length());
    
    XferList list = new XferList(path, path);
    LinkedList<String> wl = new LinkedList<String>();
    wl.add("");

    // The working list will contain path names of directories relative
    // to the root path passed to this function.
    while (!wl.isEmpty()) {
      String s = wl.pop();
      for (File f : new File(path, s).listFiles()) {
        if (f.isDirectory()) {
          wl.add(s+f.getName());
          list.add(s+f.getName());
        } else {
          list.add(s+f.getName(), f.length());
        }
      }
    }

    return list;
  }

  // Get the size of a file.
  public static long size(String path) throws Exception {
    return new File(path).length();
  }

  // Miscellaneous functions
  // -----------------------
  // Make a URI from string, throws if there's a parse error.
  public static URI makeURI(String uri) {
    try {
      URI u = new URI(uri).normalize();

      if (u.getScheme() == null || u.getScheme().isEmpty())
        throw new FatalEx("no protocol specified");

      return u;
    } catch (Exception e) {
      throw new FatalEx("couldn't parse URI: "+e.getMessage(), e);
    }
  }

  // Normalize a path. Squash /'s, resolve .'s and ..'s, and fix
  // home directory shorthand.
  public static String normalizePath(String path) {
    try {
      path = new URI(null, null, path, null).normalize().getPath();
    } catch (Exception e) {
      throw new FatalEx("invalid path");
    } if (path.startsWith("/~")) {
      path = path.substring(1);
    } return path;
  }

  // Convert a byte array into a formatted string.
  public static String formatBytes(byte[] bytes, String fmt) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
      sb.append(String.format(fmt, b));
    return sb.toString();
  }
}
