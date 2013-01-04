# Rhinodo

### What is Rhinodo? 
The basic idea behind Rhinodo is to enable running 'node.js' modules in the JVM. So it can be said that it's a compatibility layer that allows you to run 'node' using Mozilla's 'rhino' Javascript implementation.

### How to build?
In order to build, execute the following command:

    mvn clean install

## Useful info

### Which native modules are (partially) supported?
Currently, the modules are created on demand as we need them. Most of the functions in the `fs` module had been implemented. Also, `vm` and `path` module are pretty complete too.

### How are the native modules implemented?
Some native modules are implemented using Java, others using Javascript.

### How Node.js async model has been implemented?
A queue is used to keep track of all the callback functions that need to be executed. When the queue is empty the program has ended as there is no more code to execute :)

##Misc 

### Authors
Alberto Pose (@thepose)

### License
Copyright 2012 MuleSoft, Inc. Licensed under the Common Public Attribution License (CPAL), Version 1.0. 
    
### Happy hacking!
