package com.jjl.exceptions;

import com.jjl.grace.result.ResponseStatusEnum;

import java.util.Deque;
import java.util.List;

/**
 * 优雅的处理异常，统一封装
 */
public class GraceException {

    public static void display(ResponseStatusEnum responseStatusEnum) {
        throw new MyCustomException(responseStatusEnum);
    }

}
