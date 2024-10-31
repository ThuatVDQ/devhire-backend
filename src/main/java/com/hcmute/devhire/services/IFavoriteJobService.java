package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.FavoriteJob;
import com.hcmute.devhire.entities.Job;

public interface IFavoriteJobService {
    FavoriteJob createFavoriteJob(Job job, Long userId) throws Exception;
}
