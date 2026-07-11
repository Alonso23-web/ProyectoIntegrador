package proyecto.nuevaases.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);

        model.addAttribute("message", ex.getMessage());
        StringBuilder traceBuilder = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            traceBuilder.append(element.toString()).append("\n");
        }
        model.addAttribute("trace", traceBuilder.toString());
        return "error/500";
    }
}
