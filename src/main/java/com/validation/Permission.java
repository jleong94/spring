package com.validation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {
    String resource();
    PermissionType[] permissions();
    
    enum PermissionType {
        READ, WRITE, DELETE
    }
}
