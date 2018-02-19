# Jedi IO

## Status
[![Download](https://api.bintray.com/packages/raisercostin/maven/jedi-node/images/download.svg)](https://bintray.com/raisercostin/maven/jedi-node/_latestVersion)
[![Build Status](https://travis-ci.org/raisercostin/jedi-node.svg?branch=master)](https://travis-ci.org/raisercostin/jedi-node)
[![Codacy Badge](https://www.codacy.com/project/badge/5cc4b6b21f694317ab8beec05342c7b5)](https://www.codacy.com/app/raisercostin/jedi-node)
[![codecov](https://codecov.io/gh/raisercostin/jedi-node/branch/master/graph/badge.svg)](https://codecov.io/gh/raisercostin/jedi-node)
<!--[![codecov.io](http://codecov.io/github/raisercostin/jedi-node/coverage.svg?branch=master)](http://codecov.io/github/raisercostin/jedi-node?branch=master)-->

## Description
Scala uniform, fluent access to files, urls and other resources API. Fluent for java too.

## Features

# Usage
## Samples
 ```
  val a = Nodes.loadYaml("a.yaml")
  val b = Nodes.loadHocon("b.conf")
  val c = Nodes.loadXml("c.xml")
  
  val ab = a.addChild("b",b)
  val abc = c.children.last.addChild("ab",ab)
  
  Nodes.saveFreemind(abc,"abc.mm")
 ```

For more samples see [LocationsTest.scala](src/test/scala/org/raisercostin/util/io/LocationsTest.scala)

## Library
 - from sbt

 ```
 libraryDependencies += "org.raisercostin" %% "jedi-node" % "0.1"
 ```
 - maven resolver at bintray - http://dl.bintray.com/raisercostin/maven

 ```
 resolvers += "raisercostin repository" at "http://dl.bintray.com/raisercostin/maven"
 ```

# Development

Projects that are using jedi-node:
 
## Backlog

### 2018-02-019 - Create Nodes abstraction


## Hierarchical Map
A container of keys in form a.b.c is needed.
The value could be multivalue eventually typed : Seq(value1,value2,value3).
Given a container and a key prefix another container should be returned with partial keys prefix removed.
A refereence to full key might be useful. A relativeKey concept might be useful?
A save/load from hocon, yaml would be nice.
A business wrapper around a Config should be easy to use.
