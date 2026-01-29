package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;

public interface PostDAO {

    void create(Post post) throws DatabaseQueryException;

    PostRecord getByID(String postId, String userId) throws DatabaseQueryException, PostNotFoundException;

    List<PostRecord> getAll() throws DatabaseQueryException;

    List<Post> search(String userId);

    void edit(Post post) throws DatabaseQueryException, PostNotFoundException;

    void delete(String postId, String userId) throws DatabaseQueryException, PostNotFoundException;

    List<PostRecord> getAll(String userId) throws DatabaseQueryException;

    PostRecord getByID(String postId) throws DatabaseQueryException, PostNotFoundException;
}
