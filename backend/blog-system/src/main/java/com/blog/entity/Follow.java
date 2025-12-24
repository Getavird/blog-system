package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Follow extends BaseEntity {
    private Integer id;
    private Integer followerId;   // 粉丝ID（关注者）
    private Integer followingId;  // 被关注者ID
    private Integer status;       // 状态：1-关注，0-取消关注
}
