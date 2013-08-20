def ###FKTNAME(msg: String)(){
    val req = (
        ("apikey" -> "###INSERTKEYNAME")~
            ("function" -> "###FKTNAME")~
            ###INPUTS
        )
    send(req)
}