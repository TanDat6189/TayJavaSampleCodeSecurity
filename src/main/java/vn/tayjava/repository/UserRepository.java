package vn.tayjava.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tayjava.model.Role;
import vn.tayjava.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername (String username);

    @Query(value = "SELECT r FROM Role r inner join UserHasRole ur on r.id = ur.user.id WHERE ur.user.id =:userId")
    List<Role> getAllByUserId(Long userId);

    //@Query(value = "SELECT * FROM tbl_user", nativeQuery = true)
//    @Query(value = "select u from User u inner join Address a on u.id = a.userId where a.city=:city")
//    List<User> getAllUser(@Param("city") String city);

    // -- Distinct --
    // @Query(value = "SELECT distinct FROM User u WHERE u.firstName=:firstName AND u.lastName=:lastName")
//    List<User> findDistinctByFirstNameAndLastName(String firstName, String lastName);

    // -- Single field --
    // @Query(value = "SELECT * FROM User u WHERE u.email= ?1")
//    List<User> findByEmail(String email);

    // -- OR --
    // @Query(value = "SELECT * FROM User u WHERE u.firstName=:name OR u.lastName=:name")
//    List<User> findByFirstNameOrLastName(String firstName, String lastName);

    // -- Is, Equals --
    // @Query(value = "SELECT * FROM User u WHERE u.firstName=:name")
//    List<User> findByFirstNameIs(String name);
//    List<User> findByFirstNameEquals(String name);
//    List<User> findByFirstName(String name);

    // --Between --
    // @Query(value = "SELECT * FROM User u WHERE u.createdAt BETWEEN ?1 AND ?2")
//    List<User> findByCreatedAtBetween(Date startDate, Date endDate);

    // -- LessThan --
    // @Query(value = "SELECT * FROM User u WHERE u.age  < :age")
//    List<User> findByAgeLessThan(int age);
//    List<User> findByAgeLessThanEquals(int age);
//    List<User> findByAgeGreaterThan(int age);
//    List<User> findByAgeGreaterThanEquals(int age);

    // -- Before va After --
    // @Query(value = "SELECT * FROM User u WHERE u.createAt  < :date")
//    List<User> findByCreatedAtBefore(Date date);
//    List<User> findByCreatedAtAfter(Date date);

    // --IsNull, Null --
    // @Query(value = "SELECT * FROM User u WHERE u.age is null")
//    List<User> findByAgeIsNull();

    // --NotNull, Null --
    // @Query(value = "SELECT * FROM User u WHERE u.age is not null")
//    List<User> findByAgeNotNull();

    // -- Like --
    // @Query(value = "SELECT * FROM User u WHERE u.lastName like %:firstName%")
//    "%" + lastName + "%"
//    List<User> findByLastNameLike(String lastName);

    // -- Like --
    // @Query(value = "SELECT * FROM User u WHERE u.lastName not like %:firstName%")
//    "%" + lastName + "%"
//    List<User> findByLastNameNotLike(String lastName);

    // -- StartingWith --
    // @Query(value = "SELECT * FROM User u WHERE u.lastName not like :lastName%")
//    List<User> findByLastNameStartingWith(String lastName);

    // -- EndingWith --
    // @Query(value = "SELECT * FROM User u WHERE u.lastName not like %:lastName")
//    List<User> findByLastNameEndingWith(String lastName);

    // -- Containing --
    // @Query(value = "SELECT * FROM User u WHERE u.lastName not like %:name%")
//    List<User> findByLastNameContaining(String name);

    // -- Not --
    // @Query(value = "SELECT * FROM User u WHERE u.lastName <> :name")
//    List<User> findByLastNameNot(String name);

    // -- In --
    //@Query(value = "SELECT * FROM User u WHERE u.age in (18,25,30)")
//    List<User> findByAgeIn(Collection<Integer> ages);

    // -- Not In --
    //@Query(value = "SELECT * FROM User u WHERE u.age not in (18,25,30)")
//    List<User> findByAgeNotIn(Collection<Integer> ages);

    // --True/False --
    //@Query(value = "SELECT * FROM User u WHERE u.activated=true")
//    List<User> findByActivated(Boolean activated);
//    List<User> findByActivatedTrue();
//    List<User> findByActivatedFalse();

    // -- IgnoreCase --
    // @Query(value = "SELECT * FROM User u WHERE LOWER(u.lastName) <> LOWER(:name)")
//    List<User> findByFirstNameIgnoreCase(String name);

    // -- OrderBy --
//    List<User> findByFirstNameOrderByIdAsc(String name);
//    List<User> findByFirstNameOrderByCreatedAtDesc(String name);

    // -- AllIgnoreCase --
//    List<User> findByFirstNameAndLastNameAllIgnoreCase(String firstName, String lastName);
}
