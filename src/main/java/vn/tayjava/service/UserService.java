package vn.tayjava.service;

import jakarta.mail.MessagingException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import vn.tayjava.dto.request.UserRequestDTO;
import vn.tayjava.dto.respond.PageResponse;
import vn.tayjava.dto.respond.UserDetailResponse;
import vn.tayjava.model.User;
import vn.tayjava.util.UserStatus;

import java.io.UnsupportedEncodingException;

public interface UserService {

    UserDetailsService userDetailService();

    long saveUser(UserRequestDTO request) throws MessagingException, UnsupportedEncodingException;

    long saveUser(User user);

    void updateUser(long userId, UserRequestDTO request);

    void changeStatus(long userId, UserStatus status);

    void deleteUser(long userId);

    UserDetailResponse getUser(long userId);

    User getByUsername(String username);

    User getByEmail(String email);

    PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy);

    PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);

    PageResponse<?> getAllUsersWithSortByColumnAndSearch(int pageNo, int pageSize, String search, String sortBy);

    PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String address, String... search);

    PageResponse<?> advanceSearchWithSpecification(Pageable pageable, String[] user, String[] address);

    void confirmUser(int userId, String secretCode);


    //    int addUser(UserRequestDTO requestDTO);
}
