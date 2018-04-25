package net.tngroup.restkafka.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class ConfigService {

    @Autowired
    Environment env;

    private static Properties properties = new Properties();

    public String getProperty(String key)
    {
        if(!properties.stringPropertyNames().contains(key)) {
            String property;
            String envProperty = System.getenv(key.toUpperCase()
                    .replace(".","_").replace("-","_"));
            if(envProperty!=null) property = envProperty;
            else property = env.getProperty(key);
            properties.setProperty(key, property);
        }
        return properties.getProperty(key);
    }

}
