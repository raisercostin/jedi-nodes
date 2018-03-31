scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// Comment to get more information during initialization
logLevel := Level.Info

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"


//release
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")

//ide
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.1.0")

//style
// Use the Scalariform plugin to reformat the code
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.6.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")


//bintray release
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

//tools
//task: dependencyUpdates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.0")
addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.8.2")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
//tasks: checkDuplicates # duplicate things on classpath
addSbtPlugin("org.scala-sbt" % "sbt-duplicates-finder" % "0.6.0")
//code coverage
//for scoverage
resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
//addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")
