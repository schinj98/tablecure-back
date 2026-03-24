package com.example.tablecure.address.repository;

import com.example.tablecure.entity.Address;
import com.example.tablecure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}