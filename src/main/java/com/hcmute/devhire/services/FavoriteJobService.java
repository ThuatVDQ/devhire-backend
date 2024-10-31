package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.FavoriteJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteJobService implements IFavoriteJobService {
    @Autowired
    private FavoriteJobRepository favoriteJobRepository;

    public FavoriteJob addFavorite(User user, Job job) {
        // Kiểm tra xem công việc đã được yêu thích hay chưa
        FavoriteJob existingFavorite = favoriteJobRepository.findByUserAndJob(user, job);
        if (existingFavorite != null) {
            return existingFavorite; // Nếu đã yêu thích, trả về thông tin yêu thích hiện có
        }

        // Nếu chưa yêu thích, tạo mới
        FavoriteJob favoriteJob = new FavoriteJob();
        favoriteJob.setUser(user);
        favoriteJob.setJob(job);
        return favoriteJobRepository.save(favoriteJob);
    }

    public void removeFavorite(User user, Job job) {
        FavoriteJob favoriteJob = favoriteJobRepository.findByUserAndJob(user, job);
        if (favoriteJob != null) {
            favoriteJobRepository.delete(favoriteJob);
        }
    }
}
