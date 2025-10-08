package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.model.PostsDto;
import Proconnect.backProconnect.repository.PostsRepository;

@Service
public class PostsService {
    private final PostsRepository postsRepository;

    public PostsService(PostsRepository postsRepository){
        this.postsRepository = postsRepository;
    }

    public String AddPost(PostsDto postsDto){
        try {
            if(postsDto == null){
                return "Posts cannot be null";
            }
            Posts posts = new Posts(postsDto.getId(), postsDto.getUserId(), postsDto.getContent(), postsDto.getTitle(), postsDto.getImageUrl(), postsDto.getDate());
            postsRepository.save(posts);
            return "Posts Add Successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public String EditPostById(Long Id, PostsDto newPost){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return "Id cannot be null or zero";
            }
            if(newPost == null){
                return "Post cannot be null";
            }
            Optional<Posts> Findpost = postsRepository.findById(Id);
            if(!Findpost.isPresent()){
                return "Post does not exits";
            }
            Posts post = Findpost.get();
            post.setContent(newPost.getContent());
            post.setImageUrl(newPost.getImageUrl());
            post.setTitle(newPost.getTitle());
            postsRepository.save(post);
            return "Edited Post";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public List<Posts> GetAllPosts(){
        return postsRepository.findAll();
    }

    public Posts GetPostsById(Long Id){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return null;
            }
            Optional<Posts> post = postsRepository.findById(Id);
            return post.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean DeletePostById(Long Id){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return false;
            }
            postsRepository.deleteById(Id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
