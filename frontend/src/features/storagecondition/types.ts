export interface StorageConditionListItem {
  id: string
  conditionCode: string
  conditionName: string
  storageType: string
  temperatureMin: number | null
  temperatureMax: number | null
  humidityMin: number | null
  humidityMax: number | null
  lightRequirement: string | null
  ventilationRequirement: string | null
  status: number
  statusLabel: string
  sortOrder: number
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type StorageConditionDetail = StorageConditionListItem

export interface StorageConditionOption {
  id: string
  label: string
  storageType: string
  status: number
}

export interface StorageConditionListQuery {
  conditionCode?: string
  conditionName?: string
  storageType?: string
  status?: number
}

export interface StorageConditionFormPayload {
  conditionName: string
  storageType: string
  temperatureMin: number | null
  temperatureMax: number | null
  humidityMin: number | null
  humidityMax: number | null
  lightRequirement: string | null
  ventilationRequirement: string | null
  status: number
  sortOrder: number
  remarks: string | null
}
