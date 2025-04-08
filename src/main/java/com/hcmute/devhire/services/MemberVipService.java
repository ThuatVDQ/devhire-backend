package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.MemberVip;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.MemberVipRepository;
import com.hcmute.devhire.utils.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberVipService implements IMemberVipService {
    private final MemberVipRepository memberVipRepository;

    @Override
    public Optional<MemberVip> getActiveVipByUser(User user) {
        Date now = new Date();
        return memberVipRepository.findFirstByUserAndStatusAndSignDayBeforeAndExpireDayAfter(
                user, Status.ACTIVE, now, now
        );
    }
}
