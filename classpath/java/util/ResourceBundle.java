package java.util;

import java.lang.reflect.Method;
import java.io.InputStream;
import java.io.IOException;

public abstract class ResourceBundle {
  protected String name;
  protected ResourceBundle parent;

  private static String replace(char a, char b, String s) {
    char[] array = new char[s.length()];
    for (int i = 0; i < array.length; ++i) {
      char c = s.charAt(i);
      array[i] = (c == a ? b : c);
    }
    return new String(array, 0, array.length, false);
  }

  private static ResourceBundle findProperties(String name, ClassLoader loader,
                                               ResourceBundle parent)
    throws IOException
  {
    InputStream in = loader.getResourceAsStream
      (replace('.', '/', name) + ".properties");
    if (in != null) {
      try {
        ResourceBundle r = new PropertyResourceBundle(in);
        r.name = name;
        r.parent = parent;
        return r;
      } finally {
        in.close();
      }
    } else {
      return null;
    }
  }

  private static ResourceBundle find(String name, ClassLoader loader,
                                     ResourceBundle parent)
    throws Exception
  {
    try {
      Class c = Class.forName(name, true, loader);
      if (c.isAssignableFrom(ResourceBundle.class)) {
        return (ResourceBundle) c.getConstructor().newInstance();
      }
    } catch (ClassNotFoundException ok) {
    } catch (NoSuchMethodException ok) { }

    return findProperties(name, loader, parent);
  }

  public static ResourceBundle getBundle(String name, Locale locale,
                                         ClassLoader loader)
  {
    try {
      ResourceBundle b = find(name, loader, null);

      if (locale.getLanguage() != null) {
        name = name + "_" + locale.getLanguage();
        ResourceBundle b2 = find(name, loader, b);
        if (b2 != null) b = b2;

        if (locale.getCountry() != null) {
          name = name + "_" + locale.getCountry();
          b2 = find(name, loader, b);
          if (b2 != null) b = b2;

          if (locale.getVariant() != null) {
            name = name + "_" + locale.getVariant();
            b2 = find(name, loader, b);
            if (b2 != null) b = b2;
          }
        }
      }
      return b;
    } catch (Exception e) {
      RuntimeException re = new MissingResourceException(name, name, null);
      re.initCause(e);
      throw re;
    }
  }

  public static ResourceBundle getBundle(String name, Locale locale) {
    return getBundle(name, locale,
                     Method.getCaller().getDeclaringClass().getClassLoader());
  }

  public static ResourceBundle getBundle(String name) {
    return getBundle(name, Locale.getDefault(),
                     Method.getCaller().getDeclaringClass().getClassLoader());
  }

  public Object getObject(String key) {
    for (ResourceBundle b = this; b != null; b = b.parent) {
      Object value = b.handleGetObject(key);
      if (value != null) {
        return value;
      }
    }
    throw new MissingResourceException(key, name, key);
  }

  public String getString(String key) {
    return (String) getObject(key);
  }

  protected abstract Object handleGetObject(String key);
}