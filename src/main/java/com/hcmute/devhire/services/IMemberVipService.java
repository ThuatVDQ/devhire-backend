package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.MemberVip;
import com.hcmute.devhire.entities.User;

import java.util.Optional;

public interface IMemberVipService {
    Optional<MemberVip> getActiveVipByUser(User user);
}
