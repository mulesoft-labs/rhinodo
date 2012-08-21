importPackage(java.io);
importPackage(org.apache.commons.io);

exports.readdirSync = function (dir) {
     var lst=new File(dir).listFiles();
     var newLst = [];
     var i;

     for(i = 0; i < lst.length; i++) {
         newLst.push(String(lst[i].getName()));
     }

     return newLst;

};


exports.readFile = function(file, encoding, callback) {
    var lines = IOUtils.readLines(new FileInputStream(new File(file)), encoding);
    var buffer = "";
    var i = 0;

    for (i = 0; i < lines.size(); i++ ) {
        if ( i == 0 ) {
            buffer += lines.get(i);
        } else {
            buffer += '\n' + lines.get(i);
        }
    }

    if (callback) {
        callback(null, String(buffer));
    }
};
