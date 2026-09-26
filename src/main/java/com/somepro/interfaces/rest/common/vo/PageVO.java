package com.somepro.interfaces.rest.common.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 对外分页返回对象（VO，用户接口层）—— 不可变 record。
 *
 * 与领域层 {@code PageResult} 的分工：record 只序列化组件，领域 PageResult 不愿为 totalPages
 * 引入 Jackson 注解（那会破坏领域层零框架依赖），所以派生字段 totalPages 放在接口层。
 * 每行内容都带各自的业务编号（donorNo / unitNo），方便与纸质单据对号。
 */
public record PageVO<T>(List<T> content, long total, int pageNum, int pageSize, int totalPages)
        implements Serializable {
}
