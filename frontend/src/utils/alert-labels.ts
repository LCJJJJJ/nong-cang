export function getAlertSeverityLabel(severity: string) {
  switch (severity) {
    case 'LOW':
      return '低'
    case 'MEDIUM':
      return '中'
    case 'HIGH':
      return '高'
    default:
      return severity
  }
}

export function getAlertTypeLabel(alertType: string) {
  switch (alertType) {
    case 'LOW_STOCK':
      return '低库存预警'
    case 'PUTAWAY_TIMEOUT':
      return '待上架超时'
    case 'OUTBOUND_PICK_TIMEOUT':
      return '待拣货超时'
    case 'OUTBOUND_SHIP_TIMEOUT':
      return '待出库超时'
    case 'ABNORMAL_STOCK_STAGNANT':
      return '异常库存滞留'
    case 'STOCKTAKING_CONFIRM_TIMEOUT':
      return '待盘点确认超时'
    case 'INBOUND_PENDING_INSPECTION':
      return '待质检超时'
    default:
      return alertType
  }
}

export function getAlertThresholdUnitLabel(thresholdUnit: string) {
  switch (thresholdUnit) {
    case 'QUANTITY':
      return '件'
    case 'HOUR':
      return '小时'
    default:
      return thresholdUnit
  }
}

export function getAlertSourceTypeLabel(sourceType: string) {
  switch (sourceType) {
    case 'INVENTORY_STOCK':
      return '实时库存'
    case 'PUTAWAY_TASK':
      return '上架任务'
    case 'OUTBOUND_TASK':
      return '出库任务'
    case 'ABNORMAL_STOCK':
      return '异常库存'
    case 'INVENTORY_STOCKTAKING_ORDER':
      return '盘点单'
    case 'INBOUND_RECORD':
      return '入库记录'
    default:
      return sourceType
  }
}

export function getMessageNoticeTypeLabel(noticeType: string) {
  switch (noticeType) {
    case 'ALERT':
      return '预警消息'
    default:
      return noticeType
  }
}
