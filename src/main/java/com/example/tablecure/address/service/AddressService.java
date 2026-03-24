package com.example.tablecure.address.service;

import com.example.tablecure.entity.Address;
import com.example.tablecure.entity.User;
import com.example.tablecure.address.repository.AddressRepository;
import com.example.tablecure.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public Address saveAddress(String email, Address address) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        address.setUser(user);

        return addressRepository.save(address);
    }
}