export interface QualityInspectionListItem {
  id: string
  inspectionCode: string
  sourceType: string
  sourceId: string
  sourceCode: string
  sourceLabel: string
  productId: string
  productCode: string
  productName: string
  unitName: string | null
  unitSymbol: string | null
  warehouseId: string
  warehouseName: string
  zoneId: string
  zoneName: string
  locationId: string
  locationName: string
  inspectQuantity: number
  qualifiedQuantity: number
  unqualifiedQuantity: number
  resultStatus: number
  resultStatusLabel: string
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export interface QualityInspectionDetail extends QualityInspectionListItem {
  precisionDigits: number | null
}

export interface QualityInspectionListQuery {
  inspectionCode?: string
  sourceType?: string
  productId?: string
  resultStatus?: number
}

export interface QualityInspectionFormPayload {
  sourceType: string
  sourceId: number
  inspectQuantity: number
  unqualifiedQuantity: number
  remarks: string | null
}
