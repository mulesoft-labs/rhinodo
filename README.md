# Rhinodo

### What is Rhinodo? 
The basic idea behind Rhinodo is to enable running 'node.js' modules in the JVM. So it can be said that it's a compatibility layer that allows you to run 'node' using Mozilla's 'rhino' Javascript implementation. So, it can be said that Rhinodo is to Node what JRuby is to Ruby.

### How to build
In order to build, execute the following command:

    mvn clean install

### How to run
Once build just set the `RHINODO_HOME` variable to where the `uberjar` can be found. Usually, that would be you `target` folder.

```sh
export RHINODO_HOME='path/to/rhinodo/target'
```

And after that just run:

```sh
./rhinodo
```

In case you want to connect to it for remote debuggig, you can use:
```sh
./rhinodo-debug
``` 

and point your IDE to port 5555.

## Useful info

### Which native modules are (partially) supported?
Currently, the modules are created on demand as we need them. Most of the functions in the `fs` module had been implemented. Also, `vm` and `path` module are pretty complete too.

### How are the native modules implemented?
Some native modules are implemented using Java, others using Javascript.

### How Node.js async model has been implemented?
A queue is used to keep track of all the callback functions that need to be executed. When the queue is empty the program has ended as there is no more code to execute :)

### What's the roadmap?
The idea is that in a near future all the necessary `.js` files will be obtained from the official node repo. 
In order to do that, the plan to add the repository as a Git submodule (it won't be an easy task). Once the node
repo is embedded, it can be used to run node.js tests.

### What has been done using it?

  * [brunch-maven-plugin](https://github.com/mulesoft/brunch-maven-plugin)
  * [jshint-maven-plugin](https://github.com/mulesoft/jshint-maven-plugin)
  * [recess-maven-plugin](https://github.com/mulesoft/recess-maven-plugin)

##Misc 

### Authors
Alberto Pose (@thepose)

### License
Copyright 2012 MuleSoft, Inc. Licensed under the Common Public Attribution License (CPAL), Version 1.0. 
    
Happy hacking!
