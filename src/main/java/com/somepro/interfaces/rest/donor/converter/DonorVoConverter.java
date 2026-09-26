package com.somepro.interfaces.rest.donor.converter;

import com.somepro.application.donor.cmd.CreateDonorCmd;
import com.somepro.application.donor.cmd.UpdateDonorCmd;
import com.somepro.domain.donor.model.Donor;
import com.somepro.domain.donor.model.Gender;
import com.somepro.domain.shared.model.BloodGroup;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.domain.shared.model.RhFactor;
import com.somepro.interfaces.rest.common.vo.PageVO;
import com.somepro.interfaces.rest.donor.vo.CreateDonorRequest;
import com.somepro.interfaces.rest.donor.vo.DonorVO;
import com.somepro.interfaces.rest.donor.vo.UpdateDonorRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 献血者接口层转换器：请求体 → 应用命令，领域对象 → VO。
 * Controller 不直接返回领域对象，避免 delFlag / 审计字段外泄。
 */
public final class DonorVoConverter {

    private DonorVoConverter() {
    }

    public static CreateDonorCmd toCmd(CreateDonorRequest req) {
        return new CreateDonorCmd(
                req.name().trim(),
                Gender.parse(req.gender()),
                BloodGroup.parse(req.bloodGroup()),
                RhFactor.parse(req.rh()),
                blankToNull(req.phone()),
                blankToNull(req.donorNo()));
    }

    public static UpdateDonorCmd toCmd(UpdateDonorRequest req) {
        return new UpdateDonorCmd(
                req.name().trim(),
                Gender.parse(req.gender()),
                BloodGroup.parse(req.bloodGroup()),
                RhFactor.parse(req.rh()),
                blankToNull(req.phone()),
                blankToNull(req.status()),
                blankToNull(req.donorNo()));
    }

    public static DonorVO toVo(Donor d) {
        return new DonorVO(
                d.getId(),
                d.getDonorNo(),
                d.getName(),
                d.getGender() == null ? null : d.getGender().name(),
                d.getBloodGroup() == null ? null : d.getBloodGroup().name(),
                d.getRh() == null ? null : d.getRh().name(),
                d.getPhone(),
                d.getTotalMl(),
                d.getLastDonateAt(),
                d.getStatus() == null ? null : d.getStatus().name(),
                d.getCreateTime());
    }

    public static PageVO<DonorVO> toPageVo(PageResult<Donor> page) {
        List<DonorVO> content = page.content().stream().map(DonorVoConverter::toVo).collect(Collectors.toList());
        return new PageVO<>(content, page.total(), page.pageNum(), page.pageSize(), page.totalPages());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
