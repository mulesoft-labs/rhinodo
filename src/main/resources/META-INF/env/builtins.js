importPackage(java.lang);

process = {};
process.platform = 'darwin';
process.env = {};

var console = {};

console.log = function(x) {
    System.out.println(x);
};