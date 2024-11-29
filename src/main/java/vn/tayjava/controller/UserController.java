package vn.tayjava.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tayjava.configuration.Translator;
import vn.tayjava.dto.request.UserRequestDTO;
import vn.tayjava.dto.respond.ResponseData;
import vn.tayjava.dto.respond.ResponseError;
import vn.tayjava.dto.respond.ResponseSuccess;
import vn.tayjava.dto.respond.UserDetailResponse;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.service.UserService;
import vn.tayjava.util.UserStatus;

import java.io.IOException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
@Tag(name = "User Controller")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

//    @PostMapping(value = "/", headers = "apiKey=v1.0")
//    @RequestMapping(method = POST, path = "/", headers = "apiKey=v1.0")
    @Operation(method = "POST", summary = "Add user", description = "API create new user")
    @PostMapping("/")
    public ResponseData<Long> addUser(@Valid @RequestBody UserRequestDTO user) {
//        System.out.println("Request add user = " + user.getFirstName());
////        log.info("Request add user = {} {}", user.getFirstName(), user.getLastName());
//        try {
////            userService.addUser(user);
//            return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), 1);
//        } catch (ResourceNotFoundException e) {
//            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Save fail!!!");
//        }
        log.info("Request add user = {} {}", user.getFirstName(), user.getLastName());

        try {
            long userId = userService.saveUser(user);
            return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), userId);
        } catch (Exception e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Add user fail");
        }
    }

//    Update tất cả thông tin
    @Operation(summary = "Update user", description = "API update user")
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser(@PathVariable @Min(1) long userId, @Valid @RequestBody UserRequestDTO user) {

        try {
            log.info("Request update userId = {}", userId);
            userService.updateUser(userId, user);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.upd.success"));
        } catch (Exception e) {
            log.error("errorMessage = {}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "User updated fail");
        }
    }

//    Update một vài thông tin
    @Operation(summary = "Update some properties", description = "API update some properties of user")
    @PatchMapping("/{userId}")
    public ResponseData<?> changeStatus(@Min(1) @PathVariable long userId, @RequestParam(required = false) UserStatus status) {

        try {
            log.info("Request change user status, userId = {}", userId);
            userService.changeStatus(userId, status);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.change.success"));
        } catch (Exception e) {
            log.error("errorMessage = {}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Change status fail");
        }
    }

//    Confirm user by email
    @Operation(summary = "Confirm user", description = "API confirm email of user by email")
    @GetMapping("/confirm/{userId}")
    public ResponseData<?> confirmUser(@Min(1) @PathVariable int userId, @RequestParam String secretCode, HttpServletResponse response) throws IOException {

        try {
            log.info("Confirm user userId = {}, secretCode = {}", userId, secretCode);
            userService.confirmUser(userId, secretCode);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User confirmed!");
        } catch (Exception e) {
            log.error("errorMessage = {}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Confirmation was failure");
        }
        finally {
            response.sendRedirect("https://www.youtube.com/watch?v=dzDdwMt1k60&list=PLqMvN7uBIdIAq6PzDyXtqtKaRGbpLy9Mm&index=22&t=3s");
        }
    }

//    Xóa một record
    @Operation(summary = "Delete user", description = "API delete user")
    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser(@PathVariable @Min(value = 1, message = "userId must be greater than 0") long userId) {

        try {
            log.info("Request delete userId = {}", userId);
            userService.deleteUser(userId);
            return new ResponseData<>(HttpStatus.NO_CONTENT.value(), Translator.toLocale("user.del.success"));
        } catch (Exception e) {
            log.error("errorMessage = {}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Delete user fail");
        }
    }

//    Lấy một record
    @Operation(summary = "Get info of user", description = "API get info of user")
    @GetMapping("/{userId}")
    public ResponseData<UserDetailResponse> getUser(@Min(1) @PathVariable long userId) {
        try {
            log.info("Request get user detail, userId = {}", userId);
            return new ResponseData<>(HttpStatus.OK.value(), "user",  userService.getUser(userId));
        } catch (ResourceNotFoundException e) {
            log.error("errorMessage = {}", e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @Operation(summary = "Get user list", description = "API get user list")
    @GetMapping("/list")
    public ResponseData<?> getAllUsers(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
            @RequestParam(required = false) String sortBy) {
        log.info("Request get all user list");
        return new ResponseData<>(HttpStatus.OK.value(), "user list", userService.getAllUsersWithSortBy(pageNo, pageSize, sortBy));
    }

    @Operation(summary = "Get list of users with sort by multiple columns", description = "Send a request via this API to get user list by pageNo, pageSize and sort by multiple columns")
    @GetMapping("/list-with-sort-by-multiple-columns")
    public ResponseData<?> getAllUsersWithSortByMultipleColumns(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
            @RequestParam(required = false) String... sorts) {
        log.info("Request get all user list with sort by mutiple columns");
        return new ResponseData<>(HttpStatus.OK.value(), "user list", userService.getAllUsersWithSortByMultipleColumns(pageNo, pageSize, sorts));
    }

    @Operation(summary = "Get list of users with sort by column and search", description = "Send a request via this API to get user list by pageNo, pageSize and sort by column and search")
    @GetMapping("/list-with-sort-by-multiple-columns-search")
    public ResponseData<?> getAllUsersWithSortByColumnAndSearch(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy) {
        log.info("Request get all user list with sort by column and search");
        return new ResponseData<>(HttpStatus.OK.value(), "user list", userService.getAllUsersWithSortByColumnAndSearch(pageNo, pageSize, search, sortBy));
    }

    @Operation(summary = "Get list of users and search with paging and sorting by criterira", description = "Send a request via this API to get user list by pageNo, pageSize, and sort by criteria")
    @GetMapping("/advance-search-by-criteria")
    public ResponseData<?> advanceSearchByCriteria(@RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "20", required = false) int pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String... search) {

        log.info("Request advance search with criteria and paging and sorting");
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.advanceSearchByCriteria(pageNo, pageSize, sortBy, address, search));
    }

    @Operation(summary = "Get list of users and search with paging and sorting by specification", description = "Send a request via this API to get user list by page and search with user and address")
    @GetMapping("/advance-search-with-specification")
    public ResponseData<?> advanceSearchWithSpecification(Pageable pageable,
                                                   @RequestParam(required = false) String[] user,
                                                   @RequestParam(required = false) String[] address) {

        log.info("Request advance search query by specification");
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.advanceSearchWithSpecification(pageable, user, address));
    }

}

//@Operation(summary = "summary", description = "description", responses = {
//        @ApiResponse(responseCode = "201", description = "User added successfully",
//                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                        examples = @ExampleObject(name = "ex name", summary = "ex summary",
//                                value = """
//                                {
//                                    "status": 201,
//                                    "message": "User added successful",
//                                    "data": 1
//                                }
//                                """
//                        )
//                )
//        )
//})
