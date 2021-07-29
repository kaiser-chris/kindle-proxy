package de.bahmut.kindleproxy.web;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.exception.NotFoundException;
import de.bahmut.kindleproxy.exception.ProxyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = { Throwable.class })
    public ModelAndView handleFallback(final Throwable e) {
        return createDefaultErrorView(
                "Unknown Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(value = { CalibrationException.class })
    public ModelAndView handleCalibrationException(final CalibrationException e) {
        return createDefaultErrorView(
                "Calibration Error",
                e.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(value = { ProxyException.class })
    public ModelAndView handleCalibrationException(final ProxyException e) {
        return createDefaultErrorView(
                "Proxy Error",
                e.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(value = { NotFoundException.class })
    public ModelAndView handleNotFoundException(final NotFoundException e) {
        return createDefaultErrorView(
                "Page not found",
                e.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    private ModelAndView createDefaultErrorView(
            final String title,
            final String reason,
            final HttpStatus status
    ) {
        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", title);
        modelAndView.addObject("reason", reason);
        modelAndView.setViewName("error/40x");
        modelAndView.setStatus(status);
        return modelAndView;
    }

}
