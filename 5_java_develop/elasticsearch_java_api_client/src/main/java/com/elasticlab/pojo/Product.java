package com.elasticlab.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengzw
 * @description 商品实体类
 * @since 2022/8/1
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    String id;
    String name;
    double price;
}
