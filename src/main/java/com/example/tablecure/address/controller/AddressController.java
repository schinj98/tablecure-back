package com.example.tablecure.address.controller;

import com.example.tablecure.address.repository.AddressRepository;
import com.example.tablecure.entity.Address;
import com.example.tablecure.entity.User;
import com.example.tablecure.address.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.tablecure.auth.repository.UserRepository;
import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;



    // ✅ GET ALL
    @GetMapping
    public List<Address> getAddresses(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return addressRepository.findByUser(user);
    }

    // ✅ ADD
    @PostMapping
    public Address addAddress(@RequestBody Address address,
                              Principal principal) {

        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        address.setUser(user);

        return addressRepository.save(address);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public Address updateAddress(@PathVariable Long id,
                                 @RequestBody Address updated,
                                 Principal principal) {

        Address address = addressRepository.findById(id)
                .orElseThrow();

        address.setFullName(updated.getFullName());
        address.setPhone(updated.getPhone());
        address.setStreet(updated.getStreet());
        address.setCity(updated.getCity());
        address.setState(updated.getState());
        address.setPincode(updated.getPincode());

        return addressRepository.save(address);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public String deleteAddress(@PathVariable Long id) {

        addressRepository.deleteById(id);
        return "Address deleted";
    }
}