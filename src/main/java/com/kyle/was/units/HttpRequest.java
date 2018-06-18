package com.kyle.was.units;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private static final Pattern QueryStringExtract =  Pattern.compile("\\??&?(?<key>[^=]*)=(?<value>[^&]*)");
    
    private String queryString;
    private String host;
    private String method;
    private String location;
    private String referer;
    private String requestBody;
    private Map<String, List<String>> parameters;
    private String protocol;
    private byte[] body;

    private HttpRequest() {
    }

    public String getQueryString(){
        return queryString;
    }
    public String getHost() {
        return host;
    }

    public String getMethod() {
        return method;
    }

    public String getLocation() {
        return location;
    }

    public String getReferer() {
        return referer;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public static Builder builder(){
        return new Builder();
    }


    public String getParameter(String name) {
        //for the first one
        if(parameters.containsKey(name)){
            return parameters.get(name).get(0);
        }
        return null;
    }


    public String[] getParameterValues(String name) {
        if(parameters.containsKey(name)){
            String [] ret= new String[parameters.get(name).size()];
            return parameters.get(name).toArray(ret);
        }
        return null;
    }


    public static class Builder{
        private HttpRequest instance;

        public Builder() {
            instance = new HttpRequest();
        }

        public Builder host(String host){
            instance.host = host;
            return this;
        }

        public Builder location(String location){
            instance.location = location;
            return this;

        }
        public Builder method(String method){
            instance.method = method;
            return this;
        }

        public Builder queryString(String queryString){
            instance.queryString = queryString;
            instance.parameters = parseQueryString(queryString);
            logger.debug("Query {}",instance.parameters);
            return this;
        }

        public Builder protocol(String protocol) {
            instance.protocol = protocol;
            return this;

        }

        public HttpRequest build() {
            return instance;
        }

        public Builder header(Map<String, String> header) {
            return this.host(header.get("HOST"));
        }

        public Builder body(byte[] body) {
            instance.body = body;
            return this;
        }
    }

    static Map<String, List<String>> parseQueryString(String queryString){
        Matcher matcher = QueryStringExtract.matcher(queryString);
        Map<String, List<String>> map = new LinkedHashMap<>();
        while(matcher.find()) {
            String key = matcher.group("key");
            String value = matcher.group("value");

            if(map.containsKey(key)){
                map.get(key);
            }
            else{
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(value);
        }

        return map;
    }

}
