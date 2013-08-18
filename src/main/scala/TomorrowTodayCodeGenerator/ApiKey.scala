package TomorrowTodayCodeGenerator

import scala.collection.mutable.ListBuffer
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import scala.actors.Actor

/**
 * Created with IntelliJ IDEA.
 * User: joern
 * Date: 8/17/13
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */
class ApiKey(KeyString: String){
    implicit val formats = DefaultFormats
    private val apiKey = parse(KeyString).extract[Key]

    def getKeyString = KeyString

    def name: String = apiKey.apikey

    def functions = apiKey.functions

    def getFunction(number: Int): fkt = apiKey.functions(number)


}

case class Key(apikey:String, functions: List[fkt])
case class fkt(name: String, inputs: List[input], outputs: List[output])
case class input(name: String, datatype: String)
case class output(name: String, datatype: String)


