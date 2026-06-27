package com.brace.server.user.dto;

import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SkillTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserReqDTO {

    public record profileOnboarding(
            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "프로필 이미지는 http 또는 https URL이어야 합니다.")
            String profileImg,

            @NotBlank
            @Size(max = 50)
            String role,

            @NotEmpty
            @Size(max = 15)
            List<@NotNull SkillTag> techTags,

            @NotNull
            ParticipationType participationType,

            @Size(max = 255)
            String introduction,

            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "포트폴리오는 http 또는 https URL이어야 합니다.")
            String portfolioUrl
    ) {
    }
}
