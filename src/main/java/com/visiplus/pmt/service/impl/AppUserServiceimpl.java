package com.visiplus.pmt.service.impl;

import com.visiplus.pmt.entity.AppUser;
import com.visiplus.pmt.repository.AppUserRepository;
import com.visiplus.pmt.service.AppUserService;
import org.springframework.stereotype.Service;

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
}
