package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.service.AppUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserServiceimpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserServiceimpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public AppUser registerAppUser(AppUser appUser) {

        if (!appUser.getEmail().contains("@") || !appUser.getEmail().contains(".")){
            throw new RuntimeException("Invalid email format");
        }

        if (appUserRepository.findByEmail(appUser.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        return appUserRepository.save(appUser);
    }

    @Override
    public AppUser loginAppUser(String email, String password) {
        Optional<AppUser> appUser = appUserRepository.findByEmail(email);

        if (appUser.isPresent() && appUser.get().getPassword().equals(password)) {
            return appUser.get();
        } else {
            throw new RuntimeException("Invalid email or password");
        }
    }

    @Override
    public AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId).orElse(null);
    }
}
