package com.nongcang.server.common.validation;

import java.math.BigDecimal;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import org.springframework.stereotype.Component;

@Component
public class QuantityPrecisionValidator {

	public void validate(BigDecimal quantity, Integer precisionDigits, String unitName) {
		if (quantity == null || precisionDigits == null) {
			return;
		}

		int actualScale = Math.max(quantity.stripTrailingZeros().scale(), 0);

		if (actualScale > precisionDigits) {
			throw new BusinessException(
					CommonErrorCode.QUANTITY_PRECISION_INVALID,
					buildMessage(precisionDigits, unitName));
		}
	}

	private String buildMessage(Integer precisionDigits, String unitName) {
		String resolvedUnitName = unitName == null || unitName.isBlank() ? "当前单位" : "单位“" + unitName + "”";

		if (precisionDigits == 0) {
			return resolvedUnitName + "只允许录入整数数量";
		}

		return resolvedUnitName + "最多允许 " + precisionDigits + " 位小数";
	}
}
