package com.somepro.interfaces.rest.unit;

import com.somepro.application.unit.BloodUnitAppService;
import com.somepro.common.Result;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.shared.model.RhFactor;
import com.somepro.domain.unit.model.ComponentType;
import com.somepro.domain.unit.model.UnitListItem;
import com.somepro.domain.unit.model.UnitQuery;
import com.somepro.domain.unit.model.UnitStatus;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.unit.converter.BloodUnitVoConverter;
import com.somepro.interfaces.rest.unit.vo.BloodUnitVO;
import com.somepro.interfaces.rest.unit.vo.CreateUnitRequest;
import com.somepro.interfaces.rest.unit.vo.UpdateUnitRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 血袋接口（用户接口层）：登记、修改、查看、删除、组合条件分页查询。
 *
 * 登记时请求体不含血型 —— 系统读献血者档案带出。
 * 查询条件全部可空、可任意组合（编号、献血者编号/姓名、血型、Rh、成分、状态），
 * 都不填就是整份名单分页；每行带 unitNo，方便与纸质单对号。
 */
@RestController
@RequestMapping("/api/units")
public class BloodUnitController {

    private final BloodUnitAppService bloodUnitAppService;

    public BloodUnitController(BloodUnitAppService bloodUnitAppService) {
        this.bloodUnitAppService = bloodUnitAppService;
    }

    /** 登记新采的血袋：编号系统按年取号（BU-yyyy-序号），血型读档案带出，状态默认 COLLECTED。 */
    @PostMapping
    public Mono<Result<BloodUnitVO>> create(@Valid @RequestBody CreateUnitRequest request) {
        return bloodUnitAppService.create(BloodUnitVoConverter.toCmd(request))
                .map(BloodUnitVoConverter::toVo)
                .map(Result::ok);
    }

    /** 改登记信息：成分、容量、采集/失效时刻、温度档可更正，status 传了一并调整。 */
    @PutMapping("/{id}")
    public Mono<Result<BloodUnitVO>> update(@PathVariable Long id,
                                            @Valid @RequestBody UpdateUnitRequest request) {
        return bloodUnitAppService.update(id, BloodUnitVoConverter.toCmd(request))
                .map(BloodUnitVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping("/{id}")
    public Mono<Result<BloodUnitVO>> detail(@PathVariable Long id) {
        return bloodUnitAppService.detail(id)
                .map(BloodUnitVoConverter::toVo)
                .map(Result::ok);
    }

    @GetMapping
    public Mono<Result<PageVO<BloodUnitVO>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String unitNo,
            @RequestParam(required = false) String donorNo,
            @RequestParam(required = false) String donorName,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String rh,
            @RequestParam(required = false) String component,
            @RequestParam(required = false) String status) {
        UnitQuery query = new UnitQuery(
                pageNum, pageSize, unitNo, donorNo, donorName,
                blankToNull(bloodGroup) == null ? null : BloodGroup.parse(bloodGroup),
                blankToNull(rh) == null ? null : RhFactor.parse(rh),
                blankToNull(component) == null ? null : ComponentType.parse(component),
                blankToNull(status) == null ? null : UnitStatus.parse(status));
        return bloodUnitAppService.page(query)
                .map(BloodUnitController::toPageVo)
                .map(Result::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<Result<Void>> delete(@PathVariable Long id) {
        return bloodUnitAppService.delete(id).thenReturn(Result.ok());
    }

    private static PageVO<BloodUnitVO> toPageVo(PageResult<UnitListItem> page) {
        List<BloodUnitVO> content = page.content().stream()
                .map(BloodUnitVoConverter::toVo)
                .collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
