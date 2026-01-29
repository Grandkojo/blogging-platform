package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;

public class PostService {
    private PostDAO postDAO;

    public PostService(PostDAO postDAO){
        this.postDAO = postDAO;
    }

    public String createPost(Post post) throws DatabaseQueryException{
        if (post.getStatus().equals("PUBLISHED")){
            post.setIsPublish(true);
        } else {
            post.setIsPublish(false);
        }

        return postDAO.create(post);
    }

    public List<PostRecord> getUserPosts(String userId) throws DatabaseQueryException{
        return postDAO.getAll(userId);
    }

    public PostRecord getPost(String postId, String userId) throws DatabaseQueryException, PostNotFoundException{
        return postDAO.getByID(postId, userId);
    };

    public PostRecord getPost(String postId) throws DatabaseQueryException, PostNotFoundException{
        return postDAO.getByID(postId);
    };

    public List<PostRecord> getPosts() throws DatabaseQueryException{
        return postDAO.getAll();
    }

    public void updatePost(Post post) throws DatabaseQueryException, PostNotFoundException{
        if (post.getStatus().equals("PUBLISHED")){
            post.setIsPublish(true);
        } else {
            post.setIsPublish(false);
        }
        postDAO.edit(post);   
    }

    public void deletePost(String postId, String userId) throws DatabaseQueryException, PostNotFoundException{
        postDAO.delete(postId, userId);
    }

}
