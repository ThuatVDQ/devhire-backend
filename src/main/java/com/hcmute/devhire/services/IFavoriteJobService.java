package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;

import java.util.List;

public interface IFavoriteJobService {
    boolean addFavorite(Job job, User user );
    void removeFavorite(User user, Job job);
    List<FavoriteJob> getFavoriteJobs(User user);
}
