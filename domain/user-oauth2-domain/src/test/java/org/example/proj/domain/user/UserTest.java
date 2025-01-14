package org.example.proj.domain.user;

import org.example.proj.domain.base.JpaConfig;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rival
 * @since 2024-12-02
 */

@DataJpaTest
@Import(JpaConfig.class)
class UserTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TestEntityManager entityManager;







    @DisplayName("이메일 중복 테스트")
    @Test
    void same_email(){

        String email = "username@email.com";
        String password = "password123";

        AppUser appUser = AppUser.builder()
            .email(email)
            .password(password)
            .gender(AppUser.Gender.MALE)
            .build();


        appUserRepository.save(appUser);

        entityManager.flush();
        entityManager.clear();


        List<AppUser> users = appUserRepository.findAll();
        System.out.println(users);

        String email2 = "no-such-user@email.com";




        assertTrue(appUserRepository.existsByEmail(email));
        assertFalse(appUserRepository.existsByEmail(email2));

    }

    @DisplayName("1. AppUser Test")
    @Test
    void test_1() {

        String email = "username@email.com";
        String password = "password123";



        AppUser appUser = AppUser.builder()
            .email(email)
            .password(password)
            .gender(AppUser.Gender.MALE)
            .build();


        appUserRepository.save(appUser);


        List<AppUser> all = appUserRepository.findAll();

        System.out.println(all);

    }




    @DisplayName("소셜 유저 생성")
    @Test
    void create_social_user(){
        AppUser appUser = AppUser.builder()
            .gender(AppUser.Gender.MALE)
            .isSocial(true)
            .provider(AuthProvider.GOOGLE)
            .build();

        appUserRepository.save(appUser);

        List<AppUser> users = appUserRepository.findAll();

        System.out.println(users);
        users.forEach(user->assertTrue(user.isSocial()));

    }

    @DisplayName("2. Same Email")
    @Test
    void test_2() {

        String email = "username@email.com";
        String password = "password123";
        AppUser appUser = AppUser.builder().email(email).password(password).build();
        appUserRepository.save(appUser);

        entityManager.flush();
        entityManager.clear();

        List<AppUser> users = appUserRepository.findAll();

        System.out.println(users);


        AppUser user2 = AppUser.builder().email(email).password(password).build();


        assertThrows(ConstraintViolationException.class,()->{
            appUserRepository.save(user2);
            entityManager.flush();
            entityManager.clear();

        });



    }




    @DisplayName("3. ")
    @Test
    void test_3(){
        String email = "username@email.com";
        String password = "password123";
        AppUser appUser = AppUser.builder().email(email).password(password).build();

        appUserRepository.save(appUser);


        appUser.softDelete();


        entityManager.flush();
        entityManager.clear();

        List<AppUser> users = appUserRepository.findAll();


        System.out.println(users);


    }
}