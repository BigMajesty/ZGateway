package com.study.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.study.common.utils.PropertiesUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName ConfigLoader
 * @Description
 * @Author
 * @Date 2024-07-15 18:28
 * @Version
 */
@Slf4j
public class ConfigLoader {
    private static final String CONFIG_FILE = "file_gateway.properties";
    private static final String ENV_PREFIX = "env_gateway.";
    private static final String JVM_PREFIX = "jvm_gateway.";

    private static final ConfigLoader INSTANCE = new ConfigLoader();

    private ConfigLoader() {}

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    private Config config;

    public static Config getConfig() {
        return INSTANCE.config;
    }

    /**
     * 加载方式，优先级高的会覆盖优先级低的 运行参数 -> jvm参数 -> 环境变量 -> 配置文件 -> 配置对象默认值
     * 
     * @param args
     * @return
     */
    public Config load(String args[]) {
        // 配置对象默认值
        config = new Config();

        // 配置文件
        loadFromConfigFile();

        // 环境变量
        loadFromEnv();

        // jvm参数
        loadFromJVM();

        // 运行参数
        loadFromArgs(args);

        return config;
    }

    private void loadFromConfigFile() {
        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (inputStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
                PropertiesUtils.properties2Object(properties, config);
            } catch (IOException e) {
                log.warn("load config file {} error", CONFIG_FILE, e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();;
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }

    }

    private void loadFromEnv() {
        Map<String, String> env = System.getenv();
        Properties properties = new Properties();
        properties.putAll(env);
        PropertiesUtils.properties2Object(properties, config, ENV_PREFIX);
    }

    private void loadFromJVM() {
        Properties properties = System.getProperties();
        PropertiesUtils.properties2Object(properties, config, JVM_PREFIX);
    }

    private void loadFromArgs(String args[]) {
        if (args != null && args.length > 0) {
            Properties properties = new Properties();
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    properties.put(arg.substring(2, arg.indexOf("=")),
                            arg.substring(arg.indexOf("=") + 1));
                }
            }
            PropertiesUtils.properties2Object(properties,config);
        }
    }

}
