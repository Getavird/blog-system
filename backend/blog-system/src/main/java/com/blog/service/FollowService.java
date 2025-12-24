package com.blog.service;

import com.blog.entity.vo.FollowVO;
import com.blog.entity.vo.FollowCountVO;
import java.util.List;

public interface FollowService {
    
    /**
     * 关注用户
     */
    boolean follow(Integer followerId, Integer followingId);
    
    /**
     * 取消关注
     */
    boolean unfollow(Integer followerId, Integer followingId);
    
    /**
     * 检查是否关注
     */
    boolean isFollowing(Integer followerId, Integer followingId);
    
    /**
     * 获取关注列表（我关注的人）
     */
    List<FollowVO> getFollowingList(Integer currentUserId, Integer page, Integer size);
    
    /**
     * 获取粉丝列表（关注我的人）
     */
    List<FollowVO> getFollowerList(Integer currentUserId, Integer page, Integer size);
    
    /**
     * 获取关注数量统计
     */
    FollowCountVO getFollowCount(Integer userId);
    
    /**
     * 获取关注数量（关注数）
     */
    int getFollowingCount(Integer userId);
    
    /**
     * 获取粉丝数量（粉丝数）
     */
    int getFollowerCount(Integer userId);
}