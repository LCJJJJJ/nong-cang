export interface InboundOrderListItem {
  id: string
  orderCode: string
  supplierId: string
  supplierName: string
  warehouseId: string
  warehouseName: string
  expectedArrivalAt: string
  actualArrivalAt: string | null
  totalItemCount: number
  totalQuantity: number
  status: number
  statusLabel: string
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export interface InboundOrderItem {
  id: string
  productId: string
  productCode: string
  productName: string
  productSpecification: string | null
  unitId: string
  unitName: string
  unitSymbol: string
  quantity: number
  sortOrder: number
  remarks: string | null
}

export interface InboundOrderDetail extends InboundOrderListItem {
  items: InboundOrderItem[]
}

export interface InboundOrderListQuery {
  orderCode?: string
  supplierId?: string
  warehouseId?: string
  status?: number
}

export interface InboundOrderItemPayload {
  productId: number
  quantity: number
  sortOrder: number
  remarks: string | null
}

export interface InboundOrderFormPayload {
  supplierId: number
  warehouseId: number
  expectedArrivalAt: string
  remarks: string | null
  items: InboundOrderItemPayload[]
}
