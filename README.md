# Rhinodo

A Node.js compatibility layer on top of Rhino (BETA).

# How to build?

    mvn clean install

# Useful info

## What native modules are (partially) supported?
Currently, the modules are created on demand. Some of the methods in `fs` had been implemented.

## How Node.js threading model has been implemented?
A queue where the callback functions are added. When the queue is empty the program has ended :)

# TODO
    
  * Run modules from javascript (no need to have the scripts in a real folder).


# Authors
Alberto Pose (@thepose)

# License
Copyright 2012 MuleSoft, Inc.

Licensed under the Common Public Attribution License (CPAL), Version 1.0.
    
## Happy hacking!
