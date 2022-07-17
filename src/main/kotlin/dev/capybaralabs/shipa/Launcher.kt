package dev.capybaralabs.shipa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Launcher {

    init {
    	println("Henlo")
    }
}

fun main(args: Array<String>) {
	runApplication<Launcher>(*args)
}
