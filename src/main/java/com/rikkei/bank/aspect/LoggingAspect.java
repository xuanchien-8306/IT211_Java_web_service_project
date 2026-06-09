package com.rikkei.bank.aspect;

import com.rikkei.bank.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // Định dạng thời gian in ra log cho đẹp
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Pointcut("execution(* com.rikkei.bank.service.impl.TransactionServiceImpl.transfer(..))")
    public void transferOperation() {}

    @AfterReturning(pointcut = "transferOperation()", returning = "result")
    public void logSuccessfulTransfer(JoinPoint joinPoint, Object result) {
        try {
            // 1. Lấy username của người đang thao tác từ Token
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            // 2. Lấy tham số (TransferRequest) mà người dùng truyền vào Controller
            TransferRequest request = extractTransferRequest(joinPoint);

            // 3. Ghi log chuẩn định dạng hệ thống
            if (request != null) {
                log.info("\n================ [AUDIT LOG - SUCCESS] ================" +
                                "\nTime        : {}" +
                                "\nUser        : {}" +
                                "\nFrom Account: {}" +
                                "\nTo Account  : {}" +
                                "\nAmount      : {} VNĐ" +
                                "\n=======================================================",
                        LocalDateTime.now().format(FORMATTER),
                        username,
                        request.getFromAccount(),
                        request.getToAccount(),
                        request.getAmount());
            }
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi ghi Audit Log: {}", e.getMessage());
        }
    }

    @AfterThrowing(pointcut = "transferOperation()", throwing = "exception")
    public void logFailedTransfer(JoinPoint joinPoint, Throwable exception) {
        try {
            String username = "UNKNOWN";
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                username = SecurityContextHolder.getContext().getAuthentication().getName();
            }

            TransferRequest request = extractTransferRequest(joinPoint);

            if (request != null) {
                log.warn("\n================ [AUDIT LOG - FAILED] =================" +
                                "\nTime        : {}" +
                                "\nUser        : {}" +
                                "\nFrom Account: {}" +
                                "\nTo Account  : {}" +
                                "\nAmount      : {} VNĐ" +
                                "\nReason      : {}" + // In ra lý do thất bại (exception message)
                                "\n=======================================================",
                        LocalDateTime.now().format(FORMATTER),
                        username,
                        request.getFromAccount(),
                        request.getToAccount(),
                        request.getAmount(),
                        exception.getMessage());
            }
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi ghi Audit Log: {}", e.getMessage());
        }
    }

    private TransferRequest extractTransferRequest(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof TransferRequest) {
                return (TransferRequest) arg;
            }
        }
        return null;
    }
}
