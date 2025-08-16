package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDTO;
import org.example.riskwarningsystembackend.dto.user.UserCreateDTO;
import org.example.riskwarningsystembackend.dto.user.UserDTO;
import org.example.riskwarningsystembackend.dto.user.UserUpdateDTO;
import org.example.riskwarningsystembackend.entity.User;
import org.example.riskwarningsystembackend.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器，提供用户信息的增删改查接口。
 */
@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserService userService;

    /**
     * 构造方法注入用户服务。
     *
     * @param userService 用户业务逻辑服务
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页获取用户列表，支持关键词搜索。
     *
     * @param page     当前页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @param keyword  搜索关键词（可选）
     * @return 包含分页用户数据的统一响应结果
     */
    @GetMapping
    public RestResult<PaginatedResponseDTO<UserDTO>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        // 构造分页请求对象，按ID降序排列
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());
        PaginatedResponseDTO<UserDTO> users = userService.getUsers(pageable, keyword);
        return RestResult.success(users);
    }

    /**
     * 根据用户ID获取用户详情。
     *
     * @param id 用户ID
     * @return 若用户存在则返回用户信息，否则返回404状态码
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestResult<UserDTO>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(RestResult.success(user)))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

    /**
     * 创建新用户。
     *
     * @param createDTO 用户创建传输对象
     * @return 返回创建成功的用户ID及HTTP状态码201
     */
    @PostMapping
    public ResponseEntity<RestResult<Map<String, Long>>> createUser(@RequestBody UserCreateDTO createDTO) {
        User newUser = userService.createUser(createDTO);
        return new ResponseEntity<>(new RestResult<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), Map.of("id", newUser.getId())), HttpStatus.CREATED);
    }

    /**
     * 更新指定ID的用户信息。
     *
     * @param id        用户ID
     * @param updateDTO 用户更新传输对象
     * @return 若更新成功返回空的成功响应，否则返回404错误
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestResult<Void>> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updateDTO) {
        return userService.updateUser(id, updateDTO)
                .map(user -> ResponseEntity.ok(RestResult.<Void>success()))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

    /**
     * 删除指定ID的用户。
     *
     * @param id 用户ID
     * @return 若删除成功返回成功响应，否则返回404错误
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResult<Void>> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(RestResult.success());
        } else {
            return new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }
}
