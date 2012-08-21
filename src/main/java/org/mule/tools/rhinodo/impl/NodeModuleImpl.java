package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.api.NodeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class NodeModuleImpl implements NodeModule {

    private URI path;
    private String name;

    public NodeModuleImpl(String rootDirectory) {
        if ( rootDirectory == null ) {
            throw new IllegalArgumentException("Error validating rootDirectory");
        }

        ClassLoader classLoader = getClass().getClassLoader();
        URL root = classLoader.getResource(rootDirectory);

        if ( root == null ) {
            throw new IllegalStateException("Error: path not found.");
        }

        File packageJson = new File(root.getFile(), "package.json");

        if( !packageJson.exists() ) {
            throw new IllegalStateException(String.format("Error: package.json not found at [%s].", packageJson));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> map;
        try {
            map = objectMapper.readValue(packageJson, Map.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error: trying to parse package.json.");
        }

        String main1 = map.get("main");
        if (main1.startsWith("./")) {
            main1 = main1.substring(2);
        }
        File main = new File(root.getFile(), main1);

        if(!main.exists() && !main.toString().endsWith(".js")) {
            main = new File(main +".js");
            if(!main.exists()) {
                throw new IllegalStateException(String.format("Error: Module [%s] not found.", main));
            }
        }

        this.name = map.get("name");
        this.path = main.toURI();
    }

    public NodeModuleImpl(String moduleName, URI path) {
        this.name = moduleName;
        this.path = path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeModuleImpl that = (NodeModuleImpl) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
