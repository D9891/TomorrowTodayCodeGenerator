case FunctionName(###FKTNAME) => {
    val result = plugin.###FKTNAME(###INSERTPARAMETERS)
    val rep = (
        ("apikey" -> "###INSERTKEYNAME")~
            ("function" -> "###FKTNAME")~
            ###OUTPUTS
        )
    sender ! NewRep("###INSERTKEYNAME", rep)
}