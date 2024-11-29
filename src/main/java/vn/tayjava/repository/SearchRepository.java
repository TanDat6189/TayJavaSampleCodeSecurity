package vn.tayjava.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import vn.tayjava.dto.respond.PageResponse;
import vn.tayjava.model.Address;
import vn.tayjava.model.User;
import vn.tayjava.repository.criteria.SearchCriteria;
import vn.tayjava.repository.criteria.UserSearchCriteriaQueryConsumer;
import vn.tayjava.repository.specification.SpecSearchCriteria;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static vn.tayjava.repository.specification.SearchOperation.*;

@Repository
public class SearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> getAllUsersWithSortByColumnAndSearch(int pageNo, int pageSize, String search, String sortBy) {

        StringBuilder sqlQuery = new StringBuilder("SELECT new vn.tayjava.dto.respond.UserDetailResponse(u.id, u.firstName, u.lastName, u.email, u.phone) FROM User u WHERE 1=1");
        if(StringUtils.hasLength(search)) {
            sqlQuery.append(" AND lower(u.firstName) LIKE lower(:firstName)");
            sqlQuery.append(" OR lower(u.lastName) LIKE lower(:lastName)");
            sqlQuery.append(" OR lower(u.email) LIKE lower(:email)");
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                sqlQuery.append(String.format("ORDER BY u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }

        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);
        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("firstName", String.format("%%%s%%", search));
            selectQuery.setParameter("lastName", String.format("%%%s%%", search));
            selectQuery.setParameter("email", String.format("%%%s%%", search));
        }

        List users = selectQuery.getResultList();
        System.out.println(users);

        //Query list user

        //Query record number
        StringBuilder sqlCountQuery = new StringBuilder("SELECT COUNT(*) FROM User u");
        if(StringUtils.hasLength(search)) {
            sqlCountQuery.append(" WHERE lower(u.firstName) LIKE lower(?1)");
            sqlCountQuery.append(" OR lower(u.lastName) LIKE lower(?2)");
            sqlCountQuery.append(" OR lower(u.email) LIKE lower(?3)");
        }

        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());
        if (StringUtils.hasLength(search)) {
            selectCountQuery.setParameter(1, String.format("%%%s%%", search));
            selectCountQuery.setParameter(2, String.format("%%%s%%", search));
            selectCountQuery.setParameter(3, String.format("%%%s%%", search));
        }
        Long totalElements = (Long) selectCountQuery.getSingleResult();
        System.out.println(totalElements);

        Page<?> page = new PageImpl<Object>(users, PageRequest.of(pageNo, pageSize), totalElements);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(page.stream().toList())
                .build();
    }

    public PageResponse<?> advanceSearchUser(int pageNo, int pageSize, String sortBy, String address, String... search) {
        //firstName:T, lastName:T
        List<SearchCriteria> criteriaList = new ArrayList<>();

        //1. Get user list
        if (search != null) {
            for (String s: search) {
                //firstName:value
                Pattern pattern = Pattern.compile("(\\w+?)(:|>|<)(.*)");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    //todo
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        //2. Get amount of records
        List<User> users = getUsers(pageNo, pageSize, criteriaList, sortBy, address);

        Long totalElements = getTotalElements(criteriaList, address);


        return PageResponse.builder()
                .pageNo(pageNo) //offset = vị trí bảng ghi trong danh sách
                .pageSize(pageSize)
                .totalPage(totalElements.intValue()) //total Elements
                .items(users)
                .build();
    }
    private List<User> getUsers(int pageNo, int pageSize, List<SearchCriteria> criteriaList, String sortBy, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        //Address search condition
        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        if (StringUtils.hasLength(address)) {
            Join<Address, User> addressUserJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressUserJoin.get("city"), "%" + address + "%");
//            Tìm kiếm trên tất cả field của Address (nghiên cứu thêm)
            query.where(predicate, addressPredicate);
        }
        else {
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();

            query.where(predicate);
        }

        //sort
        if(StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                //todo
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("desc")) {
                    query.orderBy(criteriaBuilder.desc(root.get(columnName)));
                }
                else {
                    query.orderBy(criteriaBuilder.asc(root.get(columnName)));
                }

            }
        }

        return entityManager.createQuery(query).setFirstResult(pageNo).setMaxResults(pageSize).getResultList();
    }

    private Long getTotalElements(List<SearchCriteria> criteriaList, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer queryConsumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        if (StringUtils.hasLength(address)) {
            Join<Address, User> addressUserJoin = root.join("addresses");
            Predicate addressPredicate = criteriaBuilder.like(addressUserJoin.get("city"), "%" + address + "%");
            query.select(criteriaBuilder.count(root));
            query.where(predicate, addressPredicate);
        }
        else {
            criteriaList.forEach(queryConsumer);
            predicate = queryConsumer.getPredicate();
            query.select(criteriaBuilder.count(root));
            query.where(predicate);
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    public PageResponse getUserJoinedAddress(Pageable pageable, String[] user, String[] address) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);
        Join<Address, User> addressUserJoin = userRoot.join("addresses");

        //Build query
        List<Predicate> userPre = new ArrayList<>();
        List<Predicate> addressPre = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
        for(String u: user) {
            Matcher matcher = pattern.matcher(u);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                Predicate predicate = toUserPredicate(userRoot, builder, criteria);
                userPre.add(predicate);
            }
        }

        for(String a: address) {
            Matcher matcher = pattern.matcher(a);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                Predicate predicate = toAddressPredicate(addressUserJoin, builder, criteria);
                addressPre.add(predicate);
            }
        }

        Predicate userPredicateArr = builder.or(userPre.toArray(new Predicate[0]));
        Predicate addressPredicateArr = builder.or(addressPre.toArray(new Predicate[0]));
        Predicate finalPre = builder.and(userPredicateArr, addressPredicateArr);

        query.where(finalPre);

        List<User> users = entityManager.createQuery(query)
                .setFirstResult(pageable.getPageNumber())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long count = count(user, address);

        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(count)
                .items(users)
                .build();
    }

    private Long count(String[] user, String[] address) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<User> userRoot = query.from(User.class);
        Join<Address, User> addressUserJoin = userRoot.join("addresses");

        //Build query
        List<Predicate> userPre = new ArrayList<>();
        List<Predicate> addressPre = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
        for(String u: user) {
            Matcher matcher = pattern.matcher(u);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                Predicate predicate = toUserPredicate(userRoot, builder, criteria);
                userPre.add(predicate);
            }
        }

        for(String a: address) {
            Matcher matcher = pattern.matcher(a);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                Predicate predicate = toAddressPredicate(addressUserJoin, builder, criteria);
                addressPre.add(predicate);
            }
        }

        Predicate userPredicateArr = builder.or(userPre.toArray(new Predicate[0]));
        Predicate addressPredicateArr = builder.or(addressPre.toArray(new Predicate[0]));
        Predicate finalPre = builder.and(userPredicateArr, addressPredicateArr);

        query.select(builder.count(userRoot));
        query.where(finalPre);

        return entityManager.createQuery(query).getSingleResult();
    }

    public Predicate toUserPredicate(Root<User> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue().toString() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString());
            case CONTAINS -> builder.notEqual(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
        };
    }

    public Predicate toAddressPredicate(Join<Address, User> addressRoot, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(addressRoot.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(addressRoot.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(addressRoot.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(addressRoot.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(addressRoot.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(addressRoot.get(criteria.getKey()), criteria.getValue().toString() + "%");
            case ENDS_WITH -> builder.like(addressRoot.get(criteria.getKey()), "%" + criteria.getValue().toString());
            case CONTAINS -> builder.notEqual(addressRoot.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
        };
    }
}

//Tìm kiếm trên tất cả field của Address (nghiên cứu thêm, cần phải chú ý rằng các trường trong Address phải đều có kiểu String)
//Predicate addressPredicate = criteriaBuilder.conjunction() ;
//Field[] addressFields = Address.class.getDeclaredFields();
//            for (Field field: addressFields) {
//addressPredicate = criteriaBuilder.or(addressPredicate, criteriaBuilder.like(addressUserJoin.get(field.getName()), "%" + address + "%"));
//        }
