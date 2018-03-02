# Jedi Nodes

## Status
[![Download](https://api.bintray.com/packages/raisercostin/maven/jedi-nodes/images/download.svg)](https://bintray.com/raisercostin/maven/jedi-nodes/_latestVersion)
[![Build Status](https://travis-ci.org/raisercostin/jedi-nodes.svg?branch=master)](https://travis-ci.org/raisercostin/jedi-nodes)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8bb2b84c03de4e6f8de91e594bfe5929)](https://www.codacy.com/app/raisercostin/jedi-nodes)
[![codecov](https://codecov.io/gh/raisercostin/jedi-nodes/branch/master/graph/badge.svg)](https://codecov.io/gh/raisercostin/jedi-nodes)
<!--[![codecov.io](http://codecov.io/github/raisercostin/jedi-nodes/coverage.svg?branch=master)](http://codecov.io/github/raisercostin/jedi-nodes?branch=master)-->

## Description
Scala (and java) fluent interface to json, xml, hocon, conf, properties, freemind.
There is an `SNode` entity that is used as the common entity

## Features
- A **node** should keep?
  - path to it?
  - root?
  - internal data?
  - data including self?
- add external formatters/parsers/connectors
  - xml
    - scala
    - java
  - json
    - rapture
  - freemind xml
  - hocon
    - typesafe config library
  - java properties
- add extractors (as visitors?)
  - jquery/css selectors
  - xpath
- add schema validators
  - general schemas: https://json-schema-everywhere.github.io/yaml
  - xsd validator
  - dtd validator
  - json schema
    - http://json-schema.org/
  - yaml schema
    - best to reuse json schema
    - http://doctrine.readthedocs.io/en/latest/en/manual/yaml-schema-files.html
    - https://stackoverflow.com/questions/5060086/yaml-schema-validation
    - https://json-schema-everywhere.github.io/yaml

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

# Usage

Projects that are using jedi-node:
 
## Backlog
- see HierarchicalMultimap in jedi-io

## Hierarchical Map
A container of keys in form a.b.c is needed.
The value could be multivalue eventually typed : Seq(value1,value2,value3).
Given a container and a key prefix another container should be returned with partial keys prefix removed.
A refereence to full key might be useful. A relativeKey concept might be useful?
A save/load from hocon, yaml would be nice.
A business wrapper around a Config should be easy to use.

# Development

 - to configure release
   ```bintrayChangeCredentials```
 - to publish current version with all scala versions (2.10,2.11,2.12):
   ```+publish```
 - to release
   ```sbt> release skip-tests```

# History
### 2018-02-19
- Create Nodes abstraction

### 2018-02-25
- Add JSON and YAML validators 
   