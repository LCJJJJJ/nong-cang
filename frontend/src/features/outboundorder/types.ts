export interface OutboundOrderListItem {
  id: string
  orderCode: string
  customerId: string
  customerName: string
  warehouseId: string
  warehouseName: string
  expectedDeliveryAt: string
  actualOutboundAt: string | null
  totalItemCount: number
  totalQuantity: number
  status: number
  statusLabel: string
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export interface OutboundOrderItem {
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

export interface OutboundOrderDetail extends OutboundOrderListItem {
  items: OutboundOrderItem[]
}

export interface OutboundOrderListQuery {
  orderCode?: string
  customerId?: string
  warehouseId?: string
  status?: number
}

export interface OutboundOrderFormItemPayload {
  productId: number
  quantity: number
  sortOrder: number
  remarks: string | null
}

export interface OutboundOrderFormPayload {
  customerId: number
  warehouseId: number
  expectedDeliveryAt: string
  remarks: string | null
  items: OutboundOrderFormItemPayload[]
}
