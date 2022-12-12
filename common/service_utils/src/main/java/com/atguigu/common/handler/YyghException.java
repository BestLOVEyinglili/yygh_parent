package com.atguigu.common.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class YyghException extends RuntimeException {
    private Integer code;
    private String msg;
}
