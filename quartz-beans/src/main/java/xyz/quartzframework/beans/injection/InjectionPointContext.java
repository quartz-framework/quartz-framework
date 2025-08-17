package xyz.quartzframework.beans.injection;

import org.springframework.beans.factory.config.DependencyDescriptor;

public final class InjectionPointContext {

  private static final ThreadLocal<DependencyDescriptor> CURRENT = new ThreadLocal<>();

  public static void set(DependencyDescriptor dd) {
    CURRENT.set(dd);
  }

  public static DependencyDescriptor get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}