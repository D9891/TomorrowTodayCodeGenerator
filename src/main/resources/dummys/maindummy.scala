package ###REPLACEWITHNAME###


import tomorrowtoday_javadraft.Core

/**
 * Autogerated Core File
 */
object App {

    def main(args : Array[String]) {

        //Get a core ref
        val core = new Core()

        //Load plugin with core ref
        val plugin = new  ###REPLACEWITHNAME###core(core)
        plugin.start()

        //Give pluginref to core
        core.plugin = plugin

        //Launch core
        core.start()
    }
}
