(function() {

    importPackage(java.io);

    exports.join = function() {
      var path = "";

      for(var i = 0; i < arguments.length; i++) {
        if (i == 0 && arguments.length > 1 ) {
            path += arguments[i];

        } else {
            path += "/" + arguments[i];
        }
      }
        return path;
    };


    exports.dirname = function(file) {
        return ("" + new File(file).getParent());
    };

    var basename;
    exports.basename = basename = function(p, ext) {
        if ( ext !== undefined ) {
            return basename(p).slice(0,-(ext.length));
        }

        var path =(new File(p)).getPath();
        var i = path.lastIndexOf(File.separator);
        return ("" + path.substr(i+1));
    };

    exports.resolve = function(path) {
        importPackage(org.apache.commons.io);
        importPackage(java.lang);

        return ("" + FilenameUtils.concat(System.getProperty("user.dir"), path));
    };

    exports.relative = function(from, to) {
        importPackage(java.io);
        return new File(from).toURI().relativize(new File(to).toURI()).getPath();
    };

    exports.extname = function(file) {
        importPackage(org.apache.commons.io);
        return "." + FilenameUtils.getExtension(file);
    };

    exports.normalize = function(path) {
        importPackage(java.net);
        return "" + new URI(path).normalize().getPath();
    };


}());