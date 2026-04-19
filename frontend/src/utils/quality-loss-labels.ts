export function getInspectionSourceTypeLabel(sourceType: string) {
  switch (sourceType) {
    case 'INBOUND_RECORD':
      return '入库记录'
    case 'INVENTORY_STOCK':
      return '在库库存'
    default:
      return sourceType
  }
}

export function getLossSourceTypeLabel(sourceType: string) {
  switch (sourceType) {
    case 'DIRECT':
      return '人工登记'
    case 'ABNORMAL_STOCK':
      return '异常库存'
    default:
      return sourceType
  }
}

export function getLossSourceModeLabel(sourceMode: 'DIRECT' | 'ABNORMAL') {
  switch (sourceMode) {
    case 'DIRECT':
      return '人工登记'
    case 'ABNORMAL':
      return '异常库存'
    default:
      return sourceMode
  }
}
