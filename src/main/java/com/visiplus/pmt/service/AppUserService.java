package com.visiplus.pmt.service;

import com.visiplus.pmt.entity.AppUser;

public interface AppUserService {
    AppUser registerAppUser(AppUser appUser);
    AppUser loginAppUser(String email, String password);
    AppUser findUserById(Long userId);
}
