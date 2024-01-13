package com.jjl.mo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.util.Date;
import java.util.Map;

@Data
@Document("message")
    public class MessageMO {
        @Id
        private String id;
        @Field("fromUserId")
        private String fromUserId;
        @Field("fromNickname")
        private String fromNickname;
        @Field("fromFace")
        private String fromFace;
        @Field("toUserId")
        private String toUserId;
        @Field("msgType")
        private Integer msgType;
        @Field("msgContent")
        private Map msgContent;
        @Field("createTime")
        private Date createTime;
}
