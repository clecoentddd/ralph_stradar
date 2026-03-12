package stradar.support.internal

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import stradar.common.CommandException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CommandException::class)
    fun handleCommandException(ex: CommandException): ResponseEntity<Map<String, String?>> {
        val body = mapOf("message" to ex.message, "status" to "error", "type" to "CommandException")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
            ex: IllegalStateException
    ): ResponseEntity<Map<String, String?>> {
        val body =
                mapOf(
                        "message" to ex.message,
                        "status" to "error",
                        "type" to "IllegalStateException"
                )
        // If it's a technical IllegalStateException, maybe still 500,
        // but let's return 400 if it looks like a validation error
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}
