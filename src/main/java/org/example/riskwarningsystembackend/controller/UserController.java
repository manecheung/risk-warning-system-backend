package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.PaginatedResponseDto;
import org.example.riskwarningsystembackend.dto.UserCreateDTO;
import org.example.riskwarningsystembackend.dto.UserDTO;
import org.example.riskwarningsystembackend.dto.UserUpdateDTO;
import org.example.riskwarningsystembackend.entity.User;
import org.example.riskwarningsystembackend.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<RestResult<PaginatedResponseDto<UserDTO>>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());
        PaginatedResponseDto<UserDTO> users = userService.getUsers(pageable, keyword);
        return ResponseEntity.ok(RestResult.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResult<UserDTO>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(RestResult.success(user)))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<RestResult<Map<String, Long>>> createUser(@RequestBody UserCreateDTO createDTO) {
        User newUser = userService.createUser(createDTO);
        return new ResponseEntity<>(new RestResult<>(ResultCode.CREATED.getCode(), ResultCode.CREATED.getMessage(), Map.of("id", newUser.getId())), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestResult<Void>> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updateDTO) {
        return userService.updateUser(id, updateDTO)
                .map(user -> ResponseEntity.ok(RestResult.<Void>success()))
                .orElseGet(() -> new ResponseEntity<>(RestResult.failure(ResultCode.NOT_FOUND), HttpStatus.NOT_FOUND));
    }

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
