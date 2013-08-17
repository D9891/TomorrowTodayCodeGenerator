package TomorrowTodayCodeGenerator

import scala.reflect.io.{File, Path}
import tomorrowtoday_javadraft.ApiKey
import scala.collection.mutable.ListBuffer
import org.apache.commons.io.FileSystemUtils


/**
 *
 */
object App {
    var rootPath= Path("")
    var name= ""
    var iKeys = ListBuffer[ApiKey]()
    var oKeys = ListBuffer[ApiKey]()
    var iPaths = ListBuffer[String]()
    var oPaths = ListBuffer[String]()
    def main(args : Array[String]) {
        println(args(1))

        name = args(0)
        parseArgs(args)
        readKeys()
        createDirs()
        createCore()
    }

    def parseArgs(args : Array[String]){
        var i = 0
        var Export = false
        var Import = false
        args.foreach(a => a match {
            case "-p" => {rootPath=args(i+1);println("Set rootpath to: "+args(i+1));i=i+2}
            case "-i" => {Export=false;Import=true;i+=1}
            case "-e" => {Export=true;Import=false; i+=1}
            case _ => {
                if(Import) {iPaths+=a;println("Added to Importpaths: "+a)}
                else if(Export) {oPaths+=a;println("Added to Exportpaths: "+a)}
                i+=1
            }
        })
    }

    def readKeys(){
        iPaths.foreach(p=>{
            val key = scala.io.Source.fromFile(p).mkString
            iKeys+=new ApiKey(key)
        })
        oPaths.foreach(p=>{
            val key = scala.io.Source.fromFile(p).mkString
            oKeys+=new ApiKey(key)
        })
    }

    var pluginPath = Path("")
    var corePath = Path("")
    var keyPath = Path("")

    def createDirs(){

        rootPath.createDirectory(failIfExists = false)

        pluginPath = Path(rootPath+"/"+name)
        pluginPath.createDirectory(failIfExists = false)

        corePath = Path(pluginPath+"/core")
        corePath.createDirectory(failIfExists = false)

        keyPath = Path(pluginPath+"/Apikeys")
        keyPath.createDirectory(failIfExists = false)
    }

    def createCore(){
        val mainAppPath = Path(corePath+"/main.scala")
        val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/maindummy.scala"
        var mainapp = ListBuffer[String]()
        scala.io.Source.fromFile(dummy).getLines.foreach(l => mainapp+=l)

        mainapp=setPluginName(mainapp)
        mainapp.foreach(l=>println(l))

    }

    //Helpers

    def setPluginName(code: ListBuffer[String]):ListBuffer[String]={
        val newcode = ListBuffer[String]()
        code.foreach(l => newcode+=l.replace("###REPLACEWITHNAME###",name))
        return newcode
    }

}
