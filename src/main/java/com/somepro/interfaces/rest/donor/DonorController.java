package com.somepro.interfaces.rest.donor;

import com.somepro.application.donor.DonorAppService;
import com.somepro.common.Result;
import com.somepro.domain.donor.model.DonorQuery;
import com.somepro.domain.donor.model.DonorStatus;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.RhFactor;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.donor.converter.DonorVoConverter;
import com.somepro.interfaces.rest.donor.vo.CreateDonorRequest;
import com.somepro.interfaces.rest.donor.vo.DonorVO;
import com.somepro.interfaces.rest.donor.vo.UpdateDonorRequest;
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

/**
 * 献血者档案接口（用户接口层）：登记、修改、查看、删除、组合条件分页查询。
 *
 * 查询条件全部可空、可任意组合；一个条件不填就是整份名单分页，pageNum/pageSize 透传。
 * 枚举码 query 参数统一收 String，空串视为不填，非空交给领域枚举 parse 做取值校验和友好报错。
 * 统一返回 Mono<Result<T>>，出参一律转 DonorVO / PageVO。
 */
@RestController
@RequestMapping("/api/donors")
public class DonorController {

    private final DonorAppService donorAppService;

    public DonorController(DonorAppService donorAppService) {
        this.donorAppService = donorAppService;
    }

    /** 登记新献血者：donorNo 可空 —— 指定了撞号挡回，不填由系统按年取号（DNR-yyyy-序号）；状态默认 ACTIVE。 */
    @PostMapping
    public Mono<Result<DonorVO>> create(@Valid @RequestBody CreateDonorRequest request) {
        return donorAppService.create(DonorVoConverter.toCmd(request))
                .map(DonorVoConverter::toVo)
                .map(Result::ok);
    }

    /** 改档案：姓名/性别/血型/Rh/电话可更正，status 传了就一并调整；donorNo 传了表示改号，撞别人在用的号挡回且原档案不动。 */
    @PutMapping("/{id}")
    public Mono<Result<DonorVO>> update(@PathVariable Long id,
                                        @Valid @RequestBody UpdateDonorRequest request) {
        return donorAppService.update(id, DonorVoConverter.toCmd(request))
                .map(DonorVoConverter::toVo)
                .map(Result::ok);
    }

    /** 看单条。 */
    @GetMapping("/{id}")
    public Mono<Result<DonorVO>> detail(@PathVariable Long id) {
        return donorAppService.detail(id)
                .map(DonorVoConverter::toVo)
                .map(Result::ok);
    }

    /**
     * 分页名单：按编号、姓名、血型、Rh、状态随意组合；都不填翻整份名单。
     * 每行带 donorNo，方便与纸质单对号。
     */
    @GetMapping
    public Mono<Result<PageVO<DonorVO>>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String donorNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String rh,
            @RequestParam(required = false) String status) {
        DonorQuery query = new DonorQuery(
                pageNum, pageSize, donorNo, name,
                blankToNull(bloodGroup) == null ? null : BloodGroup.parse(bloodGroup),
                blankToNull(rh) == null ? null : RhFactor.parse(rh),
                blankToNull(status) == null ? null : DonorStatus.parse(status));
        return donorAppService.page(query)
                .map(DonorVoConverter::toPageVo)
                .map(Result::ok);
    }

    /** 删除（逻辑删除），删掉后不再出现在名单里。 */
    @DeleteMapping("/{id}")
    public Mono<Result<Void>> delete(@PathVariable Long id) {
        return donorAppService.delete(id).thenReturn(Result.ok());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
