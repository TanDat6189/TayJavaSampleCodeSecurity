package vn.tayjava.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.tayjava.dto.request.AddressDTO;
import vn.tayjava.dto.request.UserRequestDTO;
import vn.tayjava.dto.respond.PageResponse;
import vn.tayjava.dto.respond.UserDetailResponse;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.model.Address;
import vn.tayjava.model.User;
import vn.tayjava.repository.SearchRepository;
import vn.tayjava.repository.UserRepository;
import vn.tayjava.repository.specification.SpecSearchCriteria;
import vn.tayjava.repository.specification.UserSpec;
import vn.tayjava.repository.specification.UserSpecificationBuilder;
import vn.tayjava.service.MailService;
import vn.tayjava.service.UserService;
import vn.tayjava.util.Gender;
import vn.tayjava.util.UserStatus;
import vn.tayjava.util.UserType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final SearchRepository searchRepository;

//    private final MailService mailService;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public UserDetailsService userDetailService() {
        return username -> userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public long saveUser(UserRequestDTO request) throws MessagingException, UnsupportedEncodingException {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateofBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getType().toUpperCase()))
                .build();
        request.getAddresses().forEach(a ->
            user.saveAddresses(Address.builder()
                    .apartmentNumber(a.getApartmentNumber())
                    .floor(a.getFloor())
                    .building(a.getBuilding())
                    .streetNumber(a.getStreetNumber())
                    .street(a.getStreet())
                    .city(a.getCity())
                    .country(a.getCountry())
                    .addressType(a.getAddressType())
                    .build()
            )
        );

        userRepository.save(user);

        if (user.getId() != null) {
            // send email confirm here
//            mailService.sendConfirmLink(user.getEmail(), user.getId(), "secretCode");

            String message = String.format("%s,%s,%s", user.getEmail(), user.getId(), "code@123");
            kafkaTemplate.send("confirm-account-topic", message);
        }

        log.info("User has saved");
        return user.getId();
    }

    @Override
    public long saveUser(User user) {

        userRepository.save(user);
        return user.getId();
    }

    @Override
    public void updateUser(long userId, UserRequestDTO request) {
        User user = getUserById(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateofBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if (!request.getEmail().equals(user.getEmail())) {
            //Check email from database if not exist then allow update email otherwise throw exception
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        user.setType(UserType.valueOf(request.getType().toUpperCase()));
        user.setAddresses(convertToAddress(request.getAddresses()));
        userRepository.save(user);

        log.info("User updated successfully");
    }

    @Override
    public void changeStatus(long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("Status changed");
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
        log.info("User deleted, userId = {}", userId);
    }

    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if(pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();

        //Nếu có giá trị
        if (StringUtils.hasLength(sortBy)) {
            // firstName:asc:desc
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if(matcher.group(3).equalsIgnoreCase("ASC")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }
                else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }


        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Page<User> users = userRepository.findAll(pageable);

        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()
        ).toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts) {
        int page = 0;
        if(pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> orders = new ArrayList<>();

        for (String sortBy: sorts) {
            // firstName:asc:desc
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if(matcher.group(3).equalsIgnoreCase("ASC")) {
                    orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }
                else {
                    orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }


        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));

        Page<User> users = userRepository.findAll(pageable);

        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()
        ).toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSortByColumnAndSearch(int pageNo, int pageSize, String search, String sortBy) {
        return searchRepository.getAllUsersWithSortByColumnAndSearch(pageNo, pageSize, search, sortBy);
    }

    @Override
    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String address, String... search) {
        return searchRepository.advanceSearchUser(pageNo, pageSize, sortBy, address, search);
    }

    @Override
    public PageResponse<?> advanceSearchWithSpecification(Pageable pageable, String[] user, String[] address) {

        Page<User> users = null;

        List<User> list = new ArrayList<>();

        if (user != null && address != null) {
            //Tim kiem tren user va address ---> join table
            return searchRepository.getUserJoinedAddress(pageable, user, address);
        }
        else if (user != null && address == null) {
            //Tim kiem tren bang user -> khong can join sang bang address

//            Specification<User> spec = UserSpec.hasFirstName("T");
//            Specification<User> genderSpec = UserSpec.notEqualGender(Gender.MALE);
//            Specification<User> finalSpec = spec.and(genderSpec);

            UserSpecificationBuilder builder = new UserSpecificationBuilder();
            for (String s: user) {
                Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
                Matcher matcher = pattern.matcher(s);
                if(matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }

            list = userRepository.findAll(builder.build());

            return PageResponse.builder()
                    .pageNo(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalPage(10)
                    .items(list)
                    .build();
        }
        else {
            users = userRepository.findAll(pageable);
        }

        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(users.getTotalPages())
                .items(list)
                .build();
    }

    @Override
    public void confirmUser(int userId, String secretCode) {
        log.info("Confirmed!");


    }

    private Set<Address> convertToAddress(Set<AddressDTO> addresses) {
        Set<Address> result = new HashSet<>();
        addresses.forEach(a ->
                result.add(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build()
                )
        );
        return result;
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

//    @Override
//    public int addUser(UserRequestDTO requestDTO) {
//        System.out.println("Save user to db");
//        if (!requestDTO.getFirstName().equals("Tay")) {
//            throw new ResourceNotFoundException("Tay khong ton tai");
//        }
//
//        return 0;
//    }
}
