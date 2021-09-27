package com.sun.bean.dto;

import com.sun.bean.pojo.Dept;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeptDto extends Dept {

    private List<DeptDto> children;
}
