package com.uga.stayanalytics.service;

import com.uga.stayanalytics.model.Review;
import com.uga.stayanalytics.repository.ReviewDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewDao reviewDao;

    public ReviewService(ReviewDao reviewDao) { this.reviewDao = reviewDao; }

    public void submit(long userId, String contentId, int rating, String comment) {
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 1 and 10.");
        }
        reviewDao.upsert(userId, contentId, rating, comment);
    }

    public List<Review> forContent(String contentId) {
        return reviewDao.findByContent(contentId);
    }

    public ReviewDao.CommunityStats statsFor(String contentId) {
        return reviewDao.communityStats(contentId);
    }
}
