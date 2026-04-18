export interface QualityGradeListItem {
  id: string
  gradeCode: string
  gradeName: string
  scoreMin: number | null
  scoreMax: number | null
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type QualityGradeDetail = QualityGradeListItem

export interface QualityGradeOption {
  id: string
  label: string
  status: number
}

export interface QualityGradeListQuery {
  gradeCode?: string
  gradeName?: string
  status?: number
}

export interface QualityGradeFormPayload {
  gradeName: string
  scoreMin: number | null
  scoreMax: number | null
  status: number
  sortOrder: number
  remarks: string | null
}
