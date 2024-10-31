package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.FavoriteJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteJobService implements IFavoriteJobService {
    private final FavoriteJobRepository favoriteJobRepository;

    public boolean addFavorite(Job job, User user) {
        FavoriteJob existingFavorite = favoriteJobRepository.findByUserAndJob(user, job);
        if (existingFavorite != null) {
            return false;
        }

        FavoriteJob favoriteJob = new FavoriteJob();
        favoriteJob.setUser(user);
        favoriteJob.setJob(job);
        favoriteJobRepository.save(favoriteJob);
        return true;
    }

    public void removeFavorite(User user, Job job) {
        FavoriteJob favoriteJob = favoriteJobRepository.findByUserAndJob(user, job);
        if (favoriteJob != null) {
            favoriteJobRepository.delete(favoriteJob);
        }
    }

    @Override
    public List<FavoriteJob> getFavoriteJobs(User user) {
        return favoriteJobRepository.findByUser(user);
    }
}
