package TomorrowTodayCodeGenerator

import scala.reflect.io.{File, Path}
import scala.collection.mutable.ArrayBuffer


/**
 *
 */
object App {
    var rootPath= Path("")
    var name= ""
    var iKeys = ArrayBuffer[ApiKey]()
    var oKeys = ArrayBuffer[ApiKey]()
    var iPaths = ArrayBuffer[String]()
    var oPaths = ArrayBuffer[String]()
    def main(args : Array[String]) {
        println(args(1))

        name = args(0)
        parseArgs(args)
        readKeys()
        createDirs()
        createMain()
        createKeys()
        createCore()
        createPlugin()
        createPom()
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

        pluginPath = Path(rootPath+"/"+name+"/src/main/scala/")
        pluginPath.createDirectory(failIfExists = false)

        corePath = Path(pluginPath+"/core")
        corePath.createDirectory(failIfExists = false)

        keyPath = Path(pluginPath+"/Apikeys")
        keyPath.createDirectory(failIfExists = false)
    }

    def createMain(){
        val mainAppPath = Path(corePath+"/main.scala").toFile
        val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/maindummy.scala"
        var mainapp = ArrayBuffer[String]()
        scala.io.Source.fromFile(dummy).getLines.foreach(l => mainapp+=l)

        mainapp=setPluginName(mainapp)
        //mainapp.foreach(l=>println(l))

        writeCodeToFile(mainAppPath,mainapp)

    }

    def createKeys(){
        iKeys.foreach(k => createKey(k))
    }

    def createKey(k: ApiKey){
        val thisKeyPath = Path(keyPath+"/"+k.name+".scala").toFile
        val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/keydummy.scala"
        var code = ArrayBuffer[String]()
        scala.io.Source.fromFile(dummy).getLines.foreach(l => code+=l)
        code = setApiKeyName(code,k.name)
        code = setApiKeyText(code,k.getKeyString)
        code = setPluginName(code)

        var methodsIndex = 0
        var i = 0
        code.foreach(l => {
            if(l.contains("###METHODS")) methodsIndex = i
            i+=1
        })
        k.functions.foreach(fkt =>
            createImportFunction(fkt, k.name).foreach(l =>{
                code.insert(methodsIndex,l)
                methodsIndex+=1
            })
        )
        code = cleanUp(code)
        //code.foreach(l => println(l))
        writeCodeToFile(thisKeyPath,code)
    }

    def createCore(){
        val mainCore = Path(corePath+"/"+name+"core.scala").toFile
        val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/coredummy.scala"
        var code = ArrayBuffer[String]()
        scala.io.Source.fromFile(dummy).getLines.foreach(l => code+=l)

        code=setPluginName(code)
        createCaseClasses()
        createDoStuff()
        createRecv()
        createKeyHandleGetters()
        code = cleanUp(code)
        //code.foreach(l=>println(l))


        writeCodeToFile(mainCore,code)

        def createCaseClasses(){
            oKeys.foreach(k => createCaseClassForKey(k, false))
            iKeys.foreach(k => createCaseClassForKey(k, true))
        }

        def createCaseClassForKey(k: ApiKey, in: Boolean){
            k.functions.foreach(fkt => createCaseClass(fkt, in))
        }

        def createCaseClass(fkt: fkt, in : Boolean){
            var cClass = "case class "
            if (in) cClass+="R"
            cClass+=fkt.name+"("
            fkt.inputs.foreach(in => {
                var t = "String"
                in.datatype match {
                    case "int" => t="Int"
                    case _ => //

                }

                cClass+=in.name+": "+t+")"
            })
            var target = 0
            var i = 1
            code.foreach(l=> {
                if(l.contains("###ADDCASECLASSESHERE")) target=i
                i+=1
            })
            code.insert(target,cClass)
        }

        def createDoStuff(){
            oKeys.foreach(k => createKeyDos(k))
        }

        def createKeyDos(k: ApiKey){
            k.functions.foreach(fkt => createFktDo(fkt,k))
        }

        def createFktDo(fkt: fkt,k: ApiKey){
            val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/dodummy.scala"
            var docode = ArrayBuffer[String]()
            scala.io.Source.fromFile(dummy).getLines.foreach(l => docode+=l)
            docode = setApiKeyName(docode,k.name)
            docode = setFktName(docode,fkt.name)
            var target = findTarget(docode,"###OUTPUTS")
            var j=0
            if (fkt.outputs.length>1){
                fkt.outputs.foreach(o => {
                    var out = "         (\""
                    out+=o.name
                    out+="\" -> result._"
                    out+=j+1
                    out+=")"
                    if (j<fkt.outputs.length) out += "~"
                    docode.insert(target,out)
                    target +=1
                })
            }
            else if (fkt.outputs.length==1){
                var out = "         (\""
                out+=fkt.outputs(0).name
                out+="\" -> result.toString)"
                docode.insert(target,out)
            }
            j=1
            var inputs = ""
            fkt.inputs.foreach(in =>{
                var input = "request.extract["
                input+=fkt.name
                input+="]."+in.name
                if (j<fkt.inputs.length) input+=", "
                inputs+=input
            })
            var newcode = ArrayBuffer[String]()
            docode.foreach(l=>newcode+=l.replace("###INSERTPARAMETERS",inputs))
            docode=newcode
            var dotarget = findTarget(code,"###ADDDOCASESHERE")
            docode.foreach(l=>{
                code.insert(dotarget,l)
                dotarget+=1
            })
        }

        def createRecv(){
            iKeys.foreach(k=>createRecvForApi(k))
        }

        def createRecvForApi(k: ApiKey){
            k.functions.foreach(fkt => createFktRecv(fkt, k))
        }

        def createFktRecv(fkt: fkt, k: ApiKey){
            val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/recvdummy.scala"
            var docode = ArrayBuffer[String]()
            scala.io.Source.fromFile(dummy).getLines.foreach(l => docode+=l)
            docode = setFktName(docode,fkt.name)

            var j=1
            var inputs = ""
            fkt.inputs.foreach(in =>{
                var input = "reply.extract[R"
                input+=fkt.name
                input+="]."+in.name
                if (j<fkt.inputs.length) input+=", "
                inputs+=input
            })
            val newcode = ArrayBuffer[String]()
            docode.foreach(l=>newcode+=l.replace("###INSERTPARAMETERS",inputs))
            docode=newcode

            var dotarget = findTarget(code,"###ADDRECVCASESHERE")
            docode.foreach(l=>{
                code.insert(dotarget,l)
                dotarget+=1
            })

        }

        def createKeyHandleGetters(){
            iKeys.foreach(k => createHandleGetter(k))
        }

        def createHandleGetter(k: ApiKey){
            val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/keyhandlegetterdummy.scala"
            var docode = ArrayBuffer[String]()
            scala.io.Source.fromFile(dummy).getLines.foreach(l => docode+=l)
            docode = setApiKeyName(docode,k.name)
            var dotarget = findTarget(code,"###ADDKEYHANDLEGETTERS")
            docode.foreach(l=>{
                code.insert(dotarget,l)
                dotarget+=1
            })
        }
    }

    def createPlugin(){
        val plugin = Path(pluginPath+"/"+name+".scala").toFile
        var code = loadDummyCode("/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/plugindummy.scala")
        code=setPluginName(code)
        createImplementedMethods()
        createKeyHandles()
        createImportedReceivers()
        code=cleanUp(code)
        code.foreach(l=>println(l))
        writeCodeToFile(plugin,code)

        def createImplementedMethods(){
            oKeys.foreach(k=> createImplementedKeyMethods(k))
        }

        def createImplementedKeyMethods(k: ApiKey){
            k.functions.foreach(fkt => createImplementedFktMethod(fkt,k))
        }

        def createImplementedFktMethod(fkt: fkt,k: ApiKey){
            var docode = loadDummyCode("/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/pluginmethoddummy.scala")
            docode = setFktName(docode,fkt.name)

            var j=1
            var inputs = ""
            fkt.inputs.foreach(in =>{
                var Input = in.name+": "

                in.datatype match {
                    case "string" => Input+="String"
                    case "int" => Input+="Int"
                    case _ => Input+"String"

                }

                if (j<fkt.inputs.length) Input+=", "
                inputs+=Input
            })
            docode=replaceCode("###INPUTS", docode,inputs)

            j=1
            var outtypes = "String"
            if(fkt.outputs.length==1){
                fkt.outputs(0).datatype match {
                    case "int" => {
                        outtypes="Int"
                        insertAtTarget(docode,ArrayBuffer[String]("var "+fkt.outputs(0).name+" = 0"),"###ADDVARS")
                    }
                    case _ => {
                        outtypes="String"
                        insertAtTarget(docode,ArrayBuffer[String]("var "+fkt.outputs(0).name+" = \"\""),"###ADDVARS")
                    }


                }
            }
            else if (fkt.outputs.length>1){
                    outtypes="("
                    fkt.outputs.foreach(o =>{
                        o.datatype match {
                            case "int" => {
                                outtypes+="Int"
                                insertAtTarget(docode,ArrayBuffer[String]("var "+o.name+" = 0"),"###ADDVARS")
                            }
                            case _ => {
                                outtypes+="String"
                                insertAtTarget(docode,ArrayBuffer[String]("var "+o.name+" = \"\""),"###ADDVARS")
                            }
                        }
                        if (j<fkt.outputs.length) outtypes+=", "
                    })
                    outtypes+=")"
            }

            docode=replaceCode("###OUTPUTTYPE", docode,outtypes)

            j=1
            var outputs = ""
            if(fkt.outputs.length==1){
                outputs += fkt.outputs(0).name

            }
            else if (fkt.outputs.length>1){
                outputs+="("
                    fkt.outputs.foreach(o =>{

                        outputs+=o.name
                        if (j<fkt.outputs.length) outputs+=", "
                    })
                outputs+=")"
            }

            docode=replaceCode("###OUTPUTS", docode,outputs)

            code=insertAtTarget(code,docode,"###ADDMETHODS")

        }

        def createKeyHandles(){
            var docode= ArrayBuffer[String]()
            iKeys.foreach(k=> docode+="var "+k.name+" = get"+k.name+"Handle")
            insertAtTarget(code,docode,"###ADDMAIN")
        }

        def createImportedReceivers(){
            iKeys.foreach(k => k.functions.foreach(f=> createImportedFktMethod(f,k)))
        }

        def createImportedFktMethod(fkt: fkt,k: ApiKey){
            var docode = loadDummyCode("/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/pluginreceivemethoddummy.scala")
            docode = setFktName(docode,fkt.name)

            var j=1
            var inputs = ""
            fkt.inputs.foreach(in =>{
                var Input = in.name+": "

                in.datatype match {
                    case "string" => Input+="String"
                    case "int" => Input+="Int"
                    case _ => Input+"String"

                }

                if (j<fkt.inputs.length) Input+=", "
                inputs+=Input
            })
            docode=replaceCode("###INPUTS", docode,inputs)
            code=insertAtTarget(code,docode,"###ADDMETHODS")
        }
    }

    def createPom(){
        val pom = Path(rootPath+"/"+name+"/pom.xml").toFile
        var code = loadDummyCode("/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/pomdummy.xml")
        code=setPluginName(code)
        writeCodeToFile(pom,code)

    }


    //Helpers
    def replaceCode(target:String, code: ArrayBuffer[String], docode: String): ArrayBuffer[String] = {
        val newcode = ArrayBuffer[String]()
        code.foreach(l=>newcode+=l.replace(target,docode))
        return newcode
    }

    def insertAtTarget(code: ArrayBuffer[String],docode: ArrayBuffer[String],target: String):ArrayBuffer[String]={
        var dotarget = findTarget(code,target)
        docode.foreach(l=>{
            code.insert(dotarget,l)
            dotarget+=1
        })
        return code
    }

    def loadDummyCode(path: String):ArrayBuffer[String]={
        var code = ArrayBuffer[String]()
        scala.io.Source.fromFile(path).getLines.foreach(l => code+=l)
        return code
    }

    def cleanUp(code: ArrayBuffer[String]): ArrayBuffer[String]={
        var newCode = ArrayBuffer[String]()
        code.foreach(l=>{
            newCode+=l.replace("###INPUTS","")
                .replace("###METHODS","")
                .replace("###ADDKEYHANDLEGETTERS","")
                .replace("###ADDDOCASESHERE","")
                .replace("###OUTPUTS","")
                .replace("###ADDRECVCASESHERE","")
                .replace("###ADDMETHODS","")
                .replace("###ADDMAIN","")
                .replace("###ADDVARS","")
                .replace("###ADDCASECLASSESHERE","")
        })
        return newCode
    }
    def setFktName(code: ArrayBuffer[String], fktname: String): ArrayBuffer[String] = {
        val newcode = ArrayBuffer[String]()
        code.foreach(l => newcode+=l.replace("###FKTNAME",fktname))
        return newcode
    }
    def findTarget(code: ArrayBuffer[String],targetString: String): Int={
        var i = 0
        var target = 0
        code.foreach(l => {
            if(l.contains(targetString)) target = i
            i+=1
        })
        return target
    }
    def createImportFunction(fkt: fkt, keyName: String): ArrayBuffer[String] = {
        val dummy = "/home/joern/programming/zyre/TomorrowTodayCodeGenerator/res/dummys/keymethoddummy.scala"
        var code = ArrayBuffer[String]()
        scala.io.Source.fromFile(dummy).getLines.foreach(l => code+=l)
        var newcode = ArrayBuffer[String]()

        code = setFktName(code, fkt.name)
        code = setApiKeyName(code, keyName)
        var i = 0
        var target = 0
        code.foreach(l => {
            if(l.contains("###INPUTS")) target = i
            i+=1
        })
        var j = 1
        fkt.inputs.foreach(in => {
            var tilde = "~"
            if(j==fkt.inputs.length) tilde = ""
            j+=1
            code.insert(target,"( \""+in.name+"\" -> "+in.name+")"+tilde)
            target+=1
            })

        return code
    }

    def setApiKeyText(code: ArrayBuffer[String], keyString: String):ArrayBuffer[String] = {
        var newcode = ArrayBuffer[String]()
        code.foreach(l => newcode+=l.replace("###INSERTKEY","\""+keyString
            .replace("\n","")
            .replace("\"","\\\"").trim()+"\""))
        return newcode
    }

    def setApiKeyName(code: ArrayBuffer[String], keyName: String):ArrayBuffer[String] = {
        var newcode = ArrayBuffer[String]()
        code.foreach(l => newcode+=l.replace("###INSERTKEYNAME",keyName))
        return newcode
    }

    def writeCodeToFile(file: File, code: ArrayBuffer[String]){
        file.createFile(failIfExists = false)
        val writer = file.writer()
        code.foreach(l => writer.append(l+"\n"))
        writer.close()
    }

    def setPluginName(code: ArrayBuffer[String]):ArrayBuffer[String]={
        var newcode = ArrayBuffer[String]()
        code.foreach(l => newcode+=l.replace("###REPLACEWITHNAME###",name))
        return newcode
    }

}
