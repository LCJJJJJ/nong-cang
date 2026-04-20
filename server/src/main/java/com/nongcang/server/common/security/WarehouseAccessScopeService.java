package com.nongcang.server.common.security;

import java.util.Objects;

import com.nongcang.server.common.exception.BusinessException;
import com.nongcang.server.common.exception.CommonErrorCode;
import com.nongcang.server.modules.auth.domain.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class WarehouseAccessScopeService {

	public Long resolveQueryWarehouseId(Long requestedWarehouseId) {
		Long scopedWarehouseId = currentScopedWarehouseId();

		if (scopedWarehouseId == null) {
			return requestedWarehouseId;
		}

		if (requestedWarehouseId != null && !Objects.equals(requestedWarehouseId, scopedWarehouseId)) {
			throw new BusinessException(CommonErrorCode.FORBIDDEN);
		}

		return scopedWarehouseId;
	}

	public Long resolveRequiredWarehouseId(Long requestedWarehouseId) {
		Long scopedWarehouseId = currentScopedWarehouseId();

		if (scopedWarehouseId == null) {
			return requestedWarehouseId;
		}

		if (requestedWarehouseId == null) {
			return scopedWarehouseId;
		}

		if (!Objects.equals(requestedWarehouseId, scopedWarehouseId)) {
			throw new BusinessException(CommonErrorCode.FORBIDDEN);
		}

		return scopedWarehouseId;
	}

	public void assertWarehouseAccess(Long warehouseId) {
		Long scopedWarehouseId = currentScopedWarehouseId();

		if (scopedWarehouseId != null && !Objects.equals(scopedWarehouseId, warehouseId)) {
			throw new BusinessException(CommonErrorCode.FORBIDDEN);
		}
	}

	public void assertAdminOrNoWarehouseScope() {
		if (currentScopedWarehouseId() != null) {
			throw new BusinessException(CommonErrorCode.FORBIDDEN);
		}
	}

	public Long currentWarehouseIdOrNull() {
		return currentScopedWarehouseId();
	}

	private Long currentScopedWarehouseId() {
		AuthenticatedUser authenticatedUser = getAuthenticatedUser();

		if ("ADMIN".equals(authenticatedUser.roleCode())) {
			return null;
		}

		if (authenticatedUser.warehouseId() == null) {
			throw new BusinessException(CommonErrorCode.FORBIDDEN);
		}

		return authenticatedUser.warehouseId();
	}

	private AuthenticatedUser getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
			throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
		}

		return authenticatedUser;
	}
}
