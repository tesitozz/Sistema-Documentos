package org.documentos.documentos.controladores;

import org.documentos.documentos.servicios.BizException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(BizException.class)
    public String handleBiz(BizException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/biz-error"; // crea una vista simple que muestre ${error}
    }
}