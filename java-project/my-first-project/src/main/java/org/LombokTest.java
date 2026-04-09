package org;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j  // 自动生成 log 对象
public class LombokTest {
    public static void main(String[] args) {
        User user = new User();
        user.setName("张三");  // 如果能调用 setter，说明 Lombok 生效了
        user.setAge(25);

        log.info(user.toString());  // 如果能用 log，说明 @Slf4j 生效了
    }
}

@Data  // 自动生成 getter/setter/toString
class User {
    private String name;
    private Integer age;
}