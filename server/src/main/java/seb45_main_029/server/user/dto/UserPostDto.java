package seb45_main_029.server.user.dto;

import lombok.Getter;
import lombok.Setter;
import seb45_main_029.server.audit.Auditable;
import seb45_main_029.server.common.Job;
import seb45_main_029.server.common.PainArea;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class UserPostDto {


    @NotBlank(message = "이메일은 필수값입니다.")
    @Email(message = "이메일 형식으로 작성해주세요. email@example.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,20}$",
            message = "비밀번호는 8자리 이상 숫자, 문자, 특수문자 조합으로 입력해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수값입니다.")
    private String confirmPassword;

    @NotBlank(message = "이름은 필수값입니다.")
    private String username;

    @NotBlank(message = "닉네임은 필수값입니다.")
    private String nickname;

    @NotBlank(message = "좌우명은 필수값입니다.")
    private String motto;

    //    @NotBlank(message = "건강상태는 필수값입니다.")
    private PainArea painArea;

    //    @NotBlank(message = "직업은 필수입니다.")
    private Job job;


    /*@CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;*/


}
