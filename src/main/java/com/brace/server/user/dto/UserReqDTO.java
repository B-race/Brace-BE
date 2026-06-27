package com.brace.server.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SkillTag;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

public class UserReqDTO {

    public record profileOnboarding(
            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "프로필 이미지는 http 또는 https URL이어야 합니다.")
            @JsonAlias("profileImgUrl")
            String profileImg,

            @NotBlank
            @Size(max = 50)
            String role,

            @NotEmpty
            @Size(min = 1, max = 15)
            List<@NotNull SkillTag> skillTags,

            @NotNull
            ParticipationType participationType,

            @Size(max = 255)
            String introduction,

            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "포트폴리오는 http 또는 https URL이어야 합니다.")
            String portfolioUrl
    ) {
    }

    public record updateProfile(
            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "프로필 이미지는 http 또는 https URL이어야 합니다.")
            @JsonAlias("profileImg")
            String profileImgUrl,

            @Size(max = 100)
            String name,

            @Size(max = 255)
            String introduction,

            @Size(max = 100)
            String role,

            @Size(max = 15)
            List<@NotNull SkillTag> skillTags,

            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "포트폴리오는 http 또는 https URL이어야 합니다.")
            String portfolioUrl,

            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "깃허브 URL은 http 또는 https URL이어야 합니다.")
            String githubUrl,

            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "노션 URL은 http 또는 https URL이어야 합니다.")
            String notionUrl,

            @Size(max = 500)
            @Pattern(regexp = "^$|https?://.+", message = "추가 URL은 http 또는 https URL이어야 합니다.")
            String extraUrl
    ) {}
}
