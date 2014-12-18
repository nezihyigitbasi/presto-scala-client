##scala-presto

scala-presto is a Scala client for the [Presto](http://prestodb.io/) SQL engine. The client exposes three easy to use interfaces: a cursor interface,
a callback interface, and an actor interface.

##Usage

###Cursor Usage

####Print the raw query results
```
client.submitQuery(query).foreach {
  queryResult => println(queryResult)
}
```

####Print each list of data in the results
```
client.submitQuery(query).filter {
  result => result.getData != null
}.foreach {
  result => result.getData.asScala.foreach {
    println
  }
}
```

####Can also use for-comprehensions
```
val data = for {
  queryResult <- client.submitQuery(query) if queryResult.getData != null
} yield queryResult.getData.asScala

val nRows = data.foldLeft(0)((c,list) => c + list.size)
println(s"${nRows} rows retrieved")
```

###Callback Usage

```
client.submitQuery(query,
  queryResult => {
    println(s"got result ${queryResult}")
  })
```

###Actor Usage
```
class ClientActor (prestoActor: ActorRef) extends Actor {
  def receive = {
    case QUERY_RESULTS (queryResults) => {
      if(queryResults.getData != null) {
        queryResults.getData.asScala.map(data => {
          println(data)
        })
      }
      if (queryResults.getNextUri == null) { //no more results
        println("Shutting down actor system")
        context.system.shutdown()
      }
    }
    case SUBMIT_QUERY (query) => {
      prestoActor ! SUBMIT_QUERY(query)
    }
    case _ =>  println("Unknown message")
  }
}

object ActorSample extends BaseSample{
  def main(args: Array[String]) {
    val client = createPrestoClient(args)
    val query = args(0)
    val schema = args(1)
    val catalog = args(2)

    println(s"Will run query=${query} on schema=${schema} catalog=${catalog}")
    val system = ActorSystem("PrestoTestActorSystem")
    val prestoActor = system.actorOf(Props(new PrestoActor(client.config)), name = "presto_actor")
    val prestoClientActor = system.actorOf(Props(new ClientActor(prestoActor)), name = "client_actor")
    prestoClientActor ! SUBMIT_QUERY(query)
  }
}
```

###Other Functionality

####Query Statistics

```
val cursor = client.submitQuery(query)
cursor.foreach {
  queryResult => println(queryResult)
}

println("Query statistics:")
client.getQueryStatistics(queryId, statistics =>
  for ((key,value) <- statistics)
    println(s"${key} => ${value}")
)
```

##Using the Library
Add the following dependency to your build script
```
"com.github.nezihyigitbasi" %% "presto-scala-client" % "0.1"
```

##Building from the Source

```
sbt doc package
```