package org.mule.tools.rhinodo.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class NodeModuleImpl implements NodeModule {

    private URI path;
    private String name;

    public static NodeModuleImpl fromJar(Class<?> klass, String rootDirectory, String destDir) {
        if ( rootDirectory == null ) {
            throw new IllegalArgumentException("Error validating rootDirectory");
        }

        if ( destDir == null ) {
            throw new IllegalArgumentException("Error validating destDir");
        }

        URI root = getRoot(klass, rootDirectory);

        if ( !"jar".equals(root.getScheme())) {
            throw new IllegalArgumentException("URI must have jar scheme");
        }

        JarURIHelper jarURIHelper;

        try {
            jarURIHelper = new JarURIHelper(root);
            jarURIHelper.copyToFolder(destDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new NodeModuleImpl(klass, destDir + File.separator + jarURIHelper.getInsideJarRelativePath());
    }


    private NodeModuleImpl(Class<?> klass, String rootDirectory) {
        if ( rootDirectory == null ) {
            throw new IllegalArgumentException("Error validating rootDirectory");
        }

//        URI root = getRoot(klass, rootDirectory);
        URI root = new File(rootDirectory + "/").toURI();

        URI packageJson;
        try {
            packageJson = new URI(root.toString() + "package.json");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        boolean exists;
        if ("file".equals(packageJson.getScheme())) {
            exists = new File(packageJson).exists();
        } else if ("jar".equals(packageJson.getScheme())) {
            throw new RuntimeException("jar scheme not supported");
//            exists = new JarURIHelper(packageJson).exists();
        } else {
            throw new IllegalStateException(String.format("Error: scheme [%s] not supported.",packageJson.getScheme()) );
        }

        if( !exists ) {
            throw new IllegalStateException(String.format("Error: package.json not found at [%s].", packageJson));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,String> map;
        try {
            InputStream inputStream = packageJson.toURL().openStream();
            map = objectMapper.readValue(inputStream, Map.class);
            IOUtils.closeQuietly(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error: trying to parse package.json.");
        }

        String main1 = map.get("main");
        if (main1.startsWith("./")) {
            main1 = main1.substring(2);
        }

        try {
            this.path = new URI(root.toString() + main1);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.name = map.get("name");
    }

    private static URI getRoot(Class<?> klass, String rootDirectory) {
        ClassLoader classLoader = klass.getClassLoader();
        URI root;
        try {
            root = classLoader.getResource(rootDirectory).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if ( root == null ) {
            throw new IllegalStateException("Error: path not found.");
        }

        try {
            root = new URI(root.toString() + "/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return root;
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
