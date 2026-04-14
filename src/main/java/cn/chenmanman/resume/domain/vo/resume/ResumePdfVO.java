package cn.chenmanman.resume.domain.vo.resume;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumePdfVO {
    private byte[] resume;
    private String resumeName;
}
