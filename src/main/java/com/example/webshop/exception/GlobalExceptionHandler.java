package com.example.webshop.exception;

import com.example.webshop.config.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Centralized exception handling for all MVC controllers.
 * Security exceptions (AccessDeniedException) are intentionally not handled here,
 * they are processed by Spring Security.
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final Messages messages;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        return errorView("error/404", HttpStatus.NOT_FOUND, messages.get(ex.getMessageKey()));
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusiness(BusinessException ex) {
        return errorView("error/4xx", HttpStatus.BAD_REQUEST, messages.get(ex));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        return errorView("error/4xx", HttpStatus.PAYLOAD_TOO_LARGE, messages.get("upload.error.size"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return errorView("error/4xx", HttpStatus.CONFLICT, messages.get("error.data.integrity"));
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ModelAndView handleLockFailure(PessimisticLockingFailureException ex) {
        log.warn("Lock acquisition failed: {}", ex.getMessage());
        return errorView("error/4xx", HttpStatus.CONFLICT, messages.get("error.concurrent"));
    }

    private ModelAndView errorView(String view, HttpStatus status, String detail) {
        ModelAndView mav = new ModelAndView(view);
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("errorDetail", detail);
        return mav;
    }
}
