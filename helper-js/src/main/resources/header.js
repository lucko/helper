load("nashorn:mozilla_compat.js");

var __load = load;
var load = function(file) {
    __load(rsd + file);
    depend(rsd + file);
};

var importWildcardPackage = function(name) {
    var packages = resolvePackageWildcard(name);
    for (var i in packages) {
        importPackage(packages[i]);
    }
};

importWildcardPackage("java.lang.*");
importWildcardPackage("java.util.*");
importWildcardPackage("org.bukkit.*");
importWildcardPackage("me.lucko.helper.*");