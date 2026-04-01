package com.cts.eduLink.application.repositoryTest;

import com.cts.eduLink.application.entity.Role;
import com.cts.eduLink.application.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
@DataJpaTest
@Transactional
public class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;

    private Optional<Role> role;
    private List<Role> roleList;

    @BeforeEach
    public void setUp(){
        role = Optional.of(new Role());
        Optional<Role> role = Optional.of(new Role());
        role.get().setRoleName("Student");
        roleRepository.save(role.get());
        roleList = new ArrayList<>();
    }
    @Test
    public void getRoleList_200(){
        roleList = roleRepository.findAll();
        for(Role r:roleList){
            log.info("{}",r.getRoleName());
        }
        assertFalse(roleList.isEmpty());
    }
}
