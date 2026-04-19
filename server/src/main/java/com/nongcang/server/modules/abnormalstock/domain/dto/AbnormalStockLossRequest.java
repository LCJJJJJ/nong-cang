package com.nongcang.server.modules.abnormalstock.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AbnormalStockLossRequest(
		@NotBlank(message = "损耗原因不能为空")
		@Size(max = 64, message = "损耗原因长度不能超过64个字符")
		String lossReason,
		@Size(max = 255, message = "备注长度不能超过255个字符")
		String remarks) {
}
