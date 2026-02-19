package administration.common

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(Exception::class)
    fun handleCommandExceptions(ex: Exception): ResponseEntity<String> {

        // Unwrapping exceptions to find the root CommandException
        var current: Throwable? = ex
        while (current != null) {
            if (current is CommandException) {
                val commandException = current // Immutable copy for smart casting in lambda
                logger.warn { "Business rule violation: ${commandException.message}" }
                return ResponseEntity.status(400).body(commandException.message)
            }
            current = current.cause
        }

        // For other unhandled exceptions, log the full error
        logger.error(ex) { "Unhandled exception occurred: ${ex.message}" }
        return ResponseEntity.status(500).body("Internal Server Error: ${ex.message}")
    }
}
