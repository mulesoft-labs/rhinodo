exports.type = function() {
   return "Darwin"; // TODO implement properly
}
exports.platform = function() {
    importPackage(java.lang);
    return "" + System.getProperty("os.name");
}