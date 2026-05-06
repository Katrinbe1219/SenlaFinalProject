package org.example.core.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.StringResponse;
import org.jfree.data.general.SeriesException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO CanNotMakeExecuotions - only for hibernate
// TODO check each for status code with claude
// ловит то, что находится в MVC, но не фильтрах
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    private void makeLog(Exception ex){
        logger.error("Проблема дошла до RestControllerAdvice - " + ex.getClass()+  " :+  " + ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationError(MethodArgumentNotValidException ex) {
        List<String> error = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", error));
    }

    @ExceptionHandler(PermissionDenied.class)
    public ResponseEntity<StringResponse> handlePermissionDenied(PermissionDenied ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StringResponse> handlePermissionDenied(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(CsvInvalidStructure.class)
    public ResponseEntity<StringResponse> handlePermissionDenied(CsvInvalidStructure ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(NoDataForContentException.class)
    public ResponseEntity<StringResponse> handlePermissionDenied(NoDataForContentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, String>> handleRegistrationException(RegistrationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrors());
    }

    @ExceptionHandler(ManyIncorrectInputsException.class)
    public ResponseEntity<List< String>> handleRegistrationException(ManyIncorrectInputsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrors());
    }

    @ExceptionHandler(CanNotMakeExecution.class)
    public ResponseEntity<StringResponse> handleCanNotMakeExecution(CanNotMakeExecution ex) {
        makeLog(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new StringResponse("Внутренняя проблема, уже разбираемся"));
    }

    @ExceptionHandler(NotCorrectInput.class)
    public ResponseEntity<StringResponse> handleNotCorrectInput(NotCorrectInput ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(EntityAlreadyExist.class)
    public ResponseEntity<StringResponse> handleEntityAlreadyExists(EntityAlreadyExist ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(DoesNoeExist.class)
    public ResponseEntity<StringResponse> handleDoesNoeExist(DoesNoeExist ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }


    @ExceptionHandler(SeriesException.class)
    public ResponseEntity<StringResponse> handleSeriesException(SeriesException e) {
        logger.error("RestAdviceController " + e.getClass().getName() + " " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new StringResponse("Server problem, try later")
        );
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<StringResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<StringResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StringResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        logger.error("RestControllerAdvice  MethodArgumentTypeMismatchException: "+ ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new StringResponse("Parameters type is not valid")
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StringResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("RestControllerAdvice HttpMessageNotReadableException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse("Invalid credentials"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StringResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        logger.error("RestControllerAdvice MissingServletRequestParameterException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringResponse(ex.getMessage()));
    }



    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StringResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("RestControllerAdvice DataIntegrityViolationException : " + ex.getMessage());
        String message = ex.getMostSpecificCause().getMessage();
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        String fullMessage = message + " " + rootCause;

        if (fullMessage.contains("duplicate key") || fullMessage.contains("unique constraint")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StringResponse("DUPLICATE KEY " + extractDuplicateDetails(fullMessage))
            );
        }

        if (fullMessage.contains("foreign key")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StringResponse("FOREIGN KEY " + extractDuplicateDetails(fullMessage) + ": данный объект связан с другим - удаление запрещено")
            );
        }

        if (fullMessage.contains("null value")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
              new StringResponse("NULL VALUE " + extractColumnName(fullMessage))
            );
        }

        if (fullMessage.contains("value too long")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StringResponse("VALUE TOO LONG " + extractColumnName(fullMessage))
            );
        }



        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new StringResponse("Ошибка целостности данных - " + ex.getMostSpecificCause().getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StringResponse> handleException(Exception ex) {
        makeLog(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new StringResponse("Server problem, try later")
        );
    }

    private String extractConstraintName(String message){
        Pattern pattern = Pattern.compile("constraint \"(\\w+)\"");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : "unknown";
    }

    private String extractColumnName(String message){
        Pattern pattern = Pattern.compile("column \"(\\w+)\"");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : "unknown";
    }

    private String extractDuplicateDetails(String message) {
        Pattern pattern = Pattern.compile("Key \\((\\w+)\\)=\\((.*?)\\)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1) + " = " + matcher.group(2);
        }
        return "duplicate value";
    }
}
