package com.rookies4.myspringboot.repository;

import com.rookies4.myspringboot.entity.Customer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
//assertj 라이브러리의 Assertions 클래스
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@Transactional //원래 save()를 호출하면 db에 바로 데이터가 들어간다. 이는 db에 넣은 데이터를 롤백(삭제)하는 기능을 한다.
//Transactional이 있으면 setter()만 호출해도 db에 반영이 된다.
class CustomerRepositoryTest {
    @Autowired
    CustomerRepository customerRepository;

    @Test
    @Rollback(value = false)
    void testUpdateCustomer() {
        Customer customer =
                customerRepository.findByCustomerId("AC001")
                        .orElseThrow(() -> new RuntimeException("Customer Not Found"));
        customer.setCustomerName("마이둘리");
        customerRepository.save(customer);
    }

    @Test
    @Disabled
    void testNotFoundCustomer() {
        customerRepository.findByCustomerId("AC003")
                .orElseThrow(() -> new RuntimeException("Customer Not Found"));
    }

    @Test
    //Customer 조회
    void testFindCustomer() {
        //findById() 호출
        Optional<Customer> customerById = customerRepository.findById(1L);
        //assertThat(customerById).isNotEmpty();
        if (customerById.isPresent()) {
            Customer existCustomer = customerById.get();
            assertThat(existCustomer.getId()).isEqualTo(1L);
        }
        //Optional의 T orElseGet(Supplier) 고객번호(AC001)가 존재하는 경우
        Optional<Customer> customerByCustomerId = customerRepository.findByCustomerId("AC001");
        Customer ac001Customer = customerByCustomerId.orElseGet(() -> new Customer());
        assertThat(ac001Customer.getCustomerName()).isEqualTo("스프링부트");

        //고객번호(AC003)가 존재하지 않는 경우
        Customer notFoundCustomer =
                customerRepository.findByCustomerId("AC003").orElseGet(() -> new Customer());
        assertThat(notFoundCustomer.getCustomerName()).isNull();

    }

    @Test
    @Transactional//메소드에 transactional을 걸수도 있다.
    @Rollback(value = false)//롤백(데이터 삭제)하지 마세요!
    //@Disabled
    void testSaveCustomer() {
        //Given(준비단계)
        Customer customer = new Customer();
        customer.setCustomerId("AC003");
        customer.setCustomerName("스프링FW3");
        //When(실행단계)
        Customer savedCustomer = customerRepository.save(customer);
        //Then(겸증단계)
        //assertEquals(expected, actual)두 인자 순서가 바뀌어도 테스트가 통과됨.
        //등록된 Customer 엔티티가 Null이 아닌지를 검증
        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getCustomerName()).isEqualTo("스프링FW3");

    }
}