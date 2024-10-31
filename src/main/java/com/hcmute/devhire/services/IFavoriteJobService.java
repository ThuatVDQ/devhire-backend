package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;

public interface IFavoriteJobService {
    FavoriteJob addFavorite(User user, Job job);
    void removeFavorite(User user, Job job);
}
