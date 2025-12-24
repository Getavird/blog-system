package com.blog.service.impl;

import com.blog.dao.FollowMapper;
import com.blog.dao.UserMapper;
import com.blog.entity.Follow;
import com.blog.entity.vo.FollowVO;
import com.blog.entity.vo.FollowCountVO;
import com.blog.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {
    
    @Autowired
    private FollowMapper followMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public boolean follow(Integer followerId, Integer followingId) {
        try {
            System.out.println("🤝 关注用户 - 关注者: " + followerId + ", 被关注者: " + followingId);
            
            // 1. 验证参数
            if (followerId.equals(followingId)) {
                throw new RuntimeException("不能关注自己");
            }
            
            // 2. 检查被关注用户是否存在
            if (userMapper.findById(followingId) == null) {
                throw new RuntimeException("被关注用户不存在");
            }
            
            // 3. 检查是否已经关注
            Follow existingFollow = followMapper.selectFollow(followerId, followingId);
            if (existingFollow != null) {
                if (existingFollow.getStatus() == 1) {
                    throw new RuntimeException("已经关注该用户");
                } else {
                    // 重新关注（之前取消过）
                    int result = followMapper.reFollow(followerId, followingId);
                    if (result > 0) {
                        System.out.println("✅ 重新关注成功");
                        return true;
                    }
                }
            }
            
            // 4. 新增关注记录
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            follow.setStatus(1);
            
            int result = followMapper.insert(follow);
            if (result > 0) {
                System.out.println("✅ 关注成功");
                return true;
            }
            
            return false;
            
        } catch (RuntimeException e) {
            System.err.println("❌ 关注用户异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 关注用户系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("关注失败，请稍后重试");
        }
    }
    
    @Override
    public boolean unfollow(Integer followerId, Integer followingId) {
        try {
            System.out.println("🚫 取消关注 - 关注者: " + followerId + ", 被关注者: " + followingId);
            
            // 1. 验证参数
            if (followerId.equals(followingId)) {
                throw new RuntimeException("不能取消关注自己");
            }
            
            // 2. 检查是否已经关注
            Follow existingFollow = followMapper.selectFollow(followerId, followingId);
            if (existingFollow == null) {
                throw new RuntimeException("未关注该用户");
            }
            
            if (existingFollow.getStatus() == 0) {
                throw new RuntimeException("已经取消关注");
            }
            
            // 3. 软删除（修改状态）
            int result = followMapper.cancelFollow(followerId, followingId);
            if (result > 0) {
                System.out.println("✅ 取消关注成功");
                return true;
            }
            
            return false;
            
        } catch (RuntimeException e) {
            System.err.println("❌ 取消关注异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ 取消关注系统异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("取消关注失败，请稍后重试");
        }
    }
    
    @Override
    public boolean isFollowing(Integer followerId, Integer followingId) {
        try {
            int count = followMapper.checkFollow(followerId, followingId);
            return count > 0;
        } catch (Exception e) {
            System.err.println("❌ 检查关注状态异常: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<FollowVO> getFollowingList(Integer currentUserId, Integer page, Integer size) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            
            // 查询关注列表
            List<FollowVO> followingList = followMapper.selectFollowingList(currentUserId, currentUserId, offset, size);
            
            System.out.println("📋 获取关注列表成功 - 用户: " + currentUserId + 
                             ", 数量: " + followingList.size());
            
            return followingList;
            
        } catch (Exception e) {
            System.err.println("❌ 获取关注列表异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取关注列表失败");
        }
    }
    
    @Override
    public List<FollowVO> getFollowerList(Integer currentUserId, Integer page, Integer size) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            
            // 查询粉丝列表
            List<FollowVO> followerList = followMapper.selectFollowerList(currentUserId, currentUserId, offset, size);
            
            System.out.println("📋 获取粉丝列表成功 - 用户: " + currentUserId + 
                             ", 数量: " + followerList.size());
            
            return followerList;
            
        } catch (Exception e) {
            System.err.println("❌ 获取粉丝列表异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取粉丝列表失败");
        }
    }
    
    @Override
    public FollowCountVO getFollowCount(Integer userId) {
        try {
            FollowCountVO countVO = new FollowCountVO();
            countVO.setFollowingCount(getFollowingCount(userId));
            countVO.setFollowerCount(getFollowerCount(userId));
            return countVO;
        } catch (Exception e) {
            System.err.println("❌ 获取关注数量异常: " + e.getMessage());
            return new FollowCountVO(); // 返回空对象而不是null
        }
    }
    
    @Override
    public int getFollowingCount(Integer userId) {
        try {
            return followMapper.countFollowing(userId);
        } catch (Exception e) {
            System.err.println("❌ 获取关注数量异常: " + e.getMessage());
            return 0;
        }
    }
    
    @Override
    public int getFollowerCount(Integer userId) {
        try {
            return followMapper.countFollowers(userId);
        } catch (Exception e) {
            System.err.println("❌ 获取粉丝数量异常: " + e.getMessage());
            return 0;
        }
    }
}