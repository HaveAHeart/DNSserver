fun main(args: Array<String>) {
    when (args[0]) {
        "-s" -> {
            val server = Server()
            server.run()
        }
        "-c" -> {
            val client = Client()
            client.run()
        }
    }
}
