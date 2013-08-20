package ###REPLACEWITHNAME###

//This imports are necessary so please leave them
import scala.actors.Actor
//End of default import

//Your plugin needs a core reference since its a child of type plugin, you may not modify the classdeclaration
class ###REPLACEWITHNAME###(core:Actor) extends plugin(core) {

    //*** Here you find your prepared methods
    ###ADDMETHODS

    //*** This is your main routine
    def main(){
        ###ADDMAIN
    }
}