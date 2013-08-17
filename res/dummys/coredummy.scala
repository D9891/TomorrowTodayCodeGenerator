package ###REPLACEWITHNAME###

import tomorrowtoday_javadraft.{NewRep, PluginTrait}
import org.json4s._
import org.json4s.JsonDSL._
import scala.actors.Actor

//case class to parse name of fkt
case class FunctionName(function: String)

//Case classes for all functions  to process all req

###ADDCASECLASSESHERE

class ###REPLACEWITHNAME###core(core: Actor) extends PluginTrait{

private final val plugin = new ###REPLACEWITHNAME###(core)

//An Actor to asynchronely run the plugins main
private final val pluginmain = new pluginmain(core)
pluginmain.start()

def doStuff(request: JValue, peer: String) {
val name = request.extract[FunctionName]

println("doing stuff "+name)
name match {
###ADDDOCASESHERE
}
}

def recvStuff(reply: _root_.org.json4s.JValue, peer: String) {
val name = reply.extract[FunctionName]
name match {
###ADDRECVCASESHERE
}
}
}

class pluginmain(core: Actor) extends Actor {

    private val plugin = new ###REPLACEWITHNAME###(core)

    override def act(){
        plugin.main()
    }
}

class plugin(core: Actor){
    ###ADDKEYHANDLEGETTERS
}
