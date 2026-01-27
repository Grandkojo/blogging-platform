package com.blogging_platform.dao.interfaces;

import java.util.List;
import java.util.UUID;

import com.blogging_platform.model.Post;

public interface PostDAO {

    void create(Post post);

    void getByID(UUID id);

    List<Post> getAll();

    List<Post> search(String keyword);

    void edit(UUID id);

    void delete(UUID id);
}
