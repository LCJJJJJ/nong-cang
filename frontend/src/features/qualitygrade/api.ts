import { request } from '../../api/http'

import type {
  QualityGradeDetail,
  QualityGradeFormPayload,
  QualityGradeListItem,
  QualityGradeListQuery,
  QualityGradeOption,
} from './types'

export function getQualityGradeList(query?: QualityGradeListQuery) {
  return request<QualityGradeListItem[]>({
    method: 'GET',
    url: '/quality-grade/list',
    params: query,
  })
}

export function getQualityGradeOptions() {
  return request<QualityGradeOption[]>({
    method: 'GET',
    url: '/quality-grade/options',
  })
}

export function getQualityGradeDetail(qualityGradeId: string) {
  return request<QualityGradeDetail>({
    method: 'GET',
    url: `/quality-grade/${qualityGradeId}`,
  })
}

export function createQualityGrade(payload: QualityGradeFormPayload) {
  return request<QualityGradeDetail>({
    method: 'POST',
    url: '/quality-grade',
    data: payload,
  })
}

export function updateQualityGrade(
  qualityGradeId: string,
  payload: QualityGradeFormPayload,
) {
  return request<QualityGradeDetail>({
    method: 'PUT',
    url: `/quality-grade/${qualityGradeId}`,
    data: payload,
  })
}

export function updateQualityGradeStatus(qualityGradeId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/quality-grade/${qualityGradeId}/status`,
    data: { status },
  })
}

export function deleteQualityGrade(qualityGradeId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/quality-grade/${qualityGradeId}`,
  })
}
