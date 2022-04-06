package com.example.redditclone.repository;

import com.example.redditclone.model.UserComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<UserComment, Long> {
}
