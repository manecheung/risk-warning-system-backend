package org.example.riskwarningsystembackend.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.example.riskwarningsystembackend.common.RestResult;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 一个优雅且健壮的解决方案，用于处理单页应用（SPA）的路由回退及API错误。
 * <p>
 * 它通过实现 Spring Boot 的 ErrorController 接口来捕获所有未被处理的请求。
 * 1.  如果请求的状态码是 404 (NOT_FOUND)，它会判断这可能是一个前端路由请求，
 * 因此将请求转发到 /index.html，交由前端路由框架处理。
 * 2.  如果请求是其他错误（如 500, 403 等），它会返回一个标准的 JSON 错误响应体，
 * 这与整个应用的 RESTful API 风格保持一致，避免了视图解析的问题。
 * <p>
 * 此方法与 Spring Boot 3.x 的默认路径匹配策略完全兼容，且逻辑清晰、健壮。
 */
@Controller
public class SpaErrorController implements ErrorController {

    /**
     * 处理所有未被映射的请求错误。
     * <p>
     * 如果是 404 错误，则转发到前端入口页面 index.html，以支持 SPA 路由；
     * 否则构建统一格式的 JSON 错误响应。
     *
     * @param request HTTP 请求对象，包含错误相关信息
     * @return 若为 404 则返回视图转发字符串，否则返回 ResponseEntity 包含错误信息
     */
    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        // 从请求属性中获取状态码
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // 使用 Java 16+ 的模式匹配特性，代码更简洁安全
        if (status instanceof Integer statusCode) {
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                // 404 Not Found 错误，执行转发到 index.html，交由前端路由处理
                return "forward:/index.html";
            }

            // 对于所有其他HTTP错误，调用辅助方法构建并返回一个标准的JSON响应
            return buildErrorResponse(statusCode, request);
        }

        // 默认情况（例如状态码不是Integer），返回一个通用的服务器内部错误JSON响应
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), request);
    }

    /**
     * 构建一个标准的 JSON 错误响应。
     *
     * @param statusCode HTTP 状态码
     * @param request    HTTP 请求对象，用于获取详细错误信息
     * @return 包含 RestResult 和 HTTP 状态的 ResponseEntity 对象
     */
    @ResponseBody // 明确这个辅助方法总是返回 JSON 响应体
    private ResponseEntity<RestResult<Object>> buildErrorResponse(int statusCode, HttpServletRequest request) {
        // 在此上下文中，statusCode 总是有效的，所以 HttpStatus.resolve 不会返回 null
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);

        String message = httpStatus.getReasonPhrase();

        // 尝试从请求属性中获取由 Spring Boot 提供的更具体的错误消息
        Object errorMessageAttr = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (errorMessageAttr instanceof String && !((String) errorMessageAttr).isEmpty()) {
            message = (String) errorMessageAttr;
        }

        // 使用您项目中的 RestResult 结构来封装错误信息
        RestResult<Object> errorResult = RestResult.failure(httpStatus.value(), message);

        return new ResponseEntity<>(errorResult, httpStatus);
    }
}
