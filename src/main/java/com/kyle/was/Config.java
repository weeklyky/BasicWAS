package com.kyle.was;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    private static final String DEFAULT = "default";
    private int port;

    private Map<String, ConfigForHost> hosts;

    private Config() {
        InputStream test = Config.class.getClassLoader().getResourceAsStream("server.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(test));
        JsonParser parser = new JsonParser();

        JsonObject root = parser.parse(reader).getAsJsonObject();

        this.port = root.get("port").getAsInt();
        JsonElement hostRaw = root.get("hosts");
        Gson gson = new Gson();
        hosts = gson.fromJson(hostRaw, new TypeToken<Map<String, ConfigForHost>>() {}.getType());

        

    }
    public static Config getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Map<Integer,String> getErrorPages(String host) {
        if(this.hosts.containsKey(host)) {
            return hosts.get(host).errorPages;
        }else{
            return hosts.get(DEFAULT).errorPages;
        }
    }

    public int getPort() {
        return port;
    }

    public String getIndexPage(String host) {
        if(this.hosts.containsKey(host)){
            return this.hosts.get(host).indexPage;
        }else{
            return this.hosts.get(DEFAULT).indexPage;

        }
    }
    public String getHttpRoot(String host) {
        if(this.hosts.containsKey(host)){
            return this.hosts.get(host).httpRoot;
        }else{
            return this.hosts.get(DEFAULT).httpRoot;

        }
    }

    private static class LazyHolder {
        private static final Config INSTANCE = new Config();
    }

    @Override
    public String toString() {
        return "Config{" +
                "port=" + port +
                ", hosts=" + hosts +
                '}';
    }

    private static class ConfigForHost {
        private String httpRoot;
        private String indexPage;
        private Map<Integer,String> errorPages = new HashMap<Integer,String>();

        @Override
        public String toString() {
            return "ConfigForHost{" +
                    "httpRoot='" + httpRoot + '\'' +
                    ", errorPages=" + errorPages +
                    '}';
        }
    }
}