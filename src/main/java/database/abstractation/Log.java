package database.abstractation;

/**
 * Created by will on 13/01/16.
 */
public class Log {
  public static Level logLevel = Level.NONE;


  public static void v(Object... msg) {
    log(Level.VERBOSE, join(msg));
  }

  public static void d(Object... msg) {
    log(Level.DEBUG, join(msg));
  }

  public static void i(Object... msg) {
    log(Level.INFO, join(msg));
  }

  public static void w(Object... msg) {
    log(Level.WARN, join(msg));
  }

  public static void e(Object... msg) {
    log(Level.ERROR, join(msg));
  }

  private static void log(Level lvl, String msg) {
    if(logLevel.shouldLog(lvl)) {
      System.out.println(msg);
    }
  }

  private static String join(Object[] objs) {
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for(Object o : objs) {
      if(!isFirst) {
        sb.append(' ');
      }
      isFirst = false;
      sb.append(o == null ? "null" : o.toString());
    }
    return sb.toString();
  }

  enum Level {
    NONE(0), VERBOSE(1), DEBUG(2), INFO(3), WARN(4), ERROR(5);
    private int level;


    Level(int level) {
      this.level = level;
    }

    public boolean shouldLog(Level lvl) {
      return lvl.level <= level;
    }
  }
}
