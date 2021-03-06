# Scala clippy

[![Build Status](https://travis-ci.org/softwaremill/scala-clippy.svg?branch=master)](https://travis-ci.org/softwaremill/scala-clippy)
[![Dependencies](https://app.updateimpact.com/badge/634276070333485056/clippy.svg?config=compile)](https://app.updateimpact.com/latest/634276070333485056/clippy)

Did you ever see a Scala compiler error such as:

````scala
[error] TheNextFacebook.scala:16: type mismatch;
[error]  found   : akka.http.scaladsl.server.StandardRoute
[error]  required: akka.stream.scaladsl.Flow[akka.http.scaladsl.model.HttpRequest,akka.http.scaladsl.model.HttpResponse,Any]
[error]   Http().bindAndHandle(r, "localhost", 8080)
`````

and had no idea what to do next? Well in this case, you need to provide an implicit instance of an `ActorMaterializer`,
but the compiler isn't smart enough to be able to tell you that. Luckily, **ScalaClippy is here to help**!

Just add the compiler plugin, and you'll see this additional helpful message:

````scala
[error]  Clippy advises: did you forget to define an implicit akka.stream.ActorMaterializer?
[error]  It allows routes to be converted into a flow.
````

# Adding the plugin

In your SBT build file, add:

````scala
addCompilerPlugin("com.softwaremill.clippy" % "plugin" % "0.1" cross CrossVersion.full)
````

Upon first use, the plugin will download the advice dataset from `https://scala-clippy.org` and store it in the 
`$HOME/.clippy` directory. The dataset will be updated at most once a day, in the background. You can customize the
dataset URL and local store by using the `-P:clippy:url=` and `-P:clippy:store=` compiler options. 

# Contributing advice

Scala Clippy is only as good as its advice database. Help other users by submitting a fix for a compilation error that 
you have encountered!

It will only take you a couple of minutes, no registration required. Just head over to 
[https://scala-clippy.org](https://scala-clippy.org)! Thanks!

# Contributing to the project

You can also help developing the plugin and/or the UI for submitting new advices! The module structure is:

* `model` - code shared between the UI and the plugin. Contains basic model case classes, such as `CompilationError` + parser
* `plugin` - the compiler plugin which actually displays the advices and matches errors agains the database of known errors
* `tests` - tests for the compiler plugin. Must be a separate project, as it requires the plugin jar to be ready
* `ui` - the ui server project in Play
* `ui-client` - the Scala.JS client-side code
* `ui-shared` - code shared between the UI server and UI client (but not needed for the plugin)

# Heroku deployment

Locally:

* Install the Heroku Toolbelt
* link the local git repository with the Heroku application: `heroku git:remote -a scala-clippy`
* run `sbt deployHeroku` to deploy the current code as a fat-jar

Currently deployed on `https://www.scala-clippy.org`