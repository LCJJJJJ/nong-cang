export function buildQuantityStep(precisionDigits?: number | null) {
  if (precisionDigits === undefined || precisionDigits === null) {
    return '0.001'
  }

  if (precisionDigits <= 0) {
    return '1'
  }

  return `0.${'0'.repeat(Math.max(precisionDigits - 1, 0))}1`
}
