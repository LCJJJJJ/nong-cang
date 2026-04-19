import { request } from '../../api/http'

import type {
  QualityInspectionDetail,
  QualityInspectionFormPayload,
  QualityInspectionListItem,
  QualityInspectionListQuery,
} from './types'

export function getQualityInspectionList(query?: QualityInspectionListQuery) {
  return request<QualityInspectionListItem[]>({
    method: 'GET',
    url: '/quality-inspection/list',
    params: query,
  })
}

export function getQualityInspectionDetail(inspectionId: string) {
  return request<QualityInspectionDetail>({
    method: 'GET',
    url: `/quality-inspection/${inspectionId}`,
  })
}

export function createQualityInspection(payload: QualityInspectionFormPayload) {
  return request<QualityInspectionDetail>({
    method: 'POST',
    url: '/quality-inspection',
    data: payload,
  })
}
