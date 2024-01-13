package com.jjl.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class MyFansVO {
    private String fanId;
    private String nickname;
    private String face;
    private boolean isFriend = false;
}
