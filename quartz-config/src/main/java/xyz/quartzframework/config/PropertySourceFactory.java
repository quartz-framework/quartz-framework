package xyz.quartzframework.config;

public interface PropertySourceFactory {

    PropertySource get(String name);

}