package top.asimov.jprool.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ResponseBody
  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<String> processServiceException(ServiceException serviceException) {
    log.error(serviceException.getLocalizedMessage(), serviceException);
    return ResponseEntity.ok().body(serviceException.getLocalizedMessage());
  }

}
