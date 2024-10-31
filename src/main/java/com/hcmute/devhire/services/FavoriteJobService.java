package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.FavoriteJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteJobService implements IFavoriteJobService {
    private final FavoriteJobRepository favoriteJobRepository;
    private final IUserService userService;
    @Override
    public FavoriteJob createFavoriteJob(Job job, Long userId) throws Exception {
        if (job == null) {
            throw new Exception("Job not found");
        }
        User user = userService.findById(userId);
        if (user == null) {
            throw new Exception("User not found");
        }
        FavoriteJob favoriteJob = FavoriteJob.builder()
                .job(job)
                .user(user)
                .build();
        return favoriteJobRepository.save(favoriteJob);
    }
}
