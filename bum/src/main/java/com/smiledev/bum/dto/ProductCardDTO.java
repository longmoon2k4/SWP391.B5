package com.smiledev.bum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardDTO {
    private int id;
    private String name;
    private String shortDescription;
    private String demoVideoUrl;
    private int viewCount;
    private double averageRating;
    private List<ProductPackageDTO> packages;
}
