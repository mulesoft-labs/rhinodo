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
        return new File(file).getParent();
    };


}());