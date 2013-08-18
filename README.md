TomorrowTodayCodeGenerator
==========================

This is a code generator to generate generic scala plugins for the TomorrowTodayFramework from a json describtor file of the form:

{
  "apikey":"TestApi",
  "functions":[
	  {"name":"echo",
		  "inputs":[
			  {"name":"msg","datatype":"string"}
			  ]
  		,"outputs":[
	  		{"name":"answer","datatype":"string"}
		  	]
	  	}
	  ]
}

Install
=======

1. Download and install the TomorrowToday framework at https://github.com/joernweissenborn/sTomorrowToday

2. Download the source

3. in the source main directory issue "mvn install"

Usage
=====

1. cd into target/ dir

2. create a file named "TestApi.json" and paste in the content of sample descriptor from above

3. run (with FOLDER beeing a folder of your choice) 
    
    java -jar TomorrowTodayCodeGenerator-1.0-SNAPSHOT.jar testimporter -p FOLDER -i TestKey.json
    
4. run (with FOLDER beeing a folder of your choice) 
    
    java -jar TomorrowTodayCodeGenerator-1.0-SNAPSHOT.jar testexporter -p FOLDER -e TestKey.json

5. at the newly created foldes, open the files testexporter.scala and testimporter.scala

6. change the importers method "main" so that it calls TestKey.echo every second.

7. change the importers method "receiveecho" to printout "answer".

8. in the exporter class, change the method "echo" to return a string of your choice

9. compile your plugins with maven

10. start up both and see happens :)
