package com.rookies4.myspringboot.exception.advice;


import com.rookies4.myspringboot.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class DefaultExceptionAdvice {

//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<ErrorObject> handleResourceNotFoundException(BusinessException ex) {
//        ErrorObject errorObject = new ErrorObject();
//        errorObject.setStatusCode(ex.getHttpStatus().value()); //404
//        errorObject.setMessage(ex.getMessage());
//
//        log.error(ex.getMessage(), ex);
//
//        return new ResponseEntity<ErrorObject>(errorObject, HttpStatusCode.valueOf(ex.getHttpStatus().value()));
//    }

    /*
        Spring6 버전에 추가된 ProblemDetail 객체에 에러정보를 담아서 리턴하는 방법
     */
    @ExceptionHandler(BusinessException.class)
    protected ProblemDetail handleException(BusinessException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(e.getHttpStatus());
        problemDetail.setTitle("Not Found");
        problemDetail.setDetail(e.getMessage());
        problemDetail.setProperty("errorCategory", "Generic");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    //숫자타입의 값에 문자열타입의 값을 입력으로 받았을때 발생하는 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleException(HttpMessageNotReadableException e) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("message", e.getMessage());
        result.put("httpStatus", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(RuntimeException.class)
//    protected ResponseEntity<ErrorObject> handleException(RuntimeException e) {
//        ErrorObject errorObject = new ErrorObject();
//        errorObject.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//        errorObject.setMessage(e.getMessage());
//
//        log.error(e.getMessage(), e);
//
//        return new ResponseEntity<ErrorObject>(errorObject, HttpStatusCode.valueOf(500));
//    }

    //html 페이지 오류와 rest api 오류 두가지를 처리
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, WebRequest request) {
        log.error(e.getMessage(), e);

        // REST API 요청인지 확인 (Accept 헤더에 application/json이 포함되었거나 /api/로 시작하는 경로인 경우)
        if (isApiRequest(request)) {
            ErrorObject errorObject = new ErrorObject();
            errorObject.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorObject.setMessage(e.getMessage());
            return new ResponseEntity<>(errorObject, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // Thymeleaf 요청인 경우 ModelAndView로 500.html 반환
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("error/500");
            modelAndView.addObject("error", e);
            return modelAndView;
        }
    }


    private boolean isApiRequest(WebRequest request) {
        // 요청 경로가 /api/로 시작하는지 확인
        String path = request.getDescription(false);
        System.out.println("===> " + path);
        if (path != null && path.startsWith("uri=/api/")) {
            return true;
        }
        // Accept 헤더 확인: rest Api인 경우 header 정보는 content-type: application/json임.
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return true;
        }
        return false;
    }
}
