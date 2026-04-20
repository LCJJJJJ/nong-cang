import type { KeyboardEvent, WheelEvent } from 'react'

export function preventNativeNumberInputWheel(event: WheelEvent<HTMLInputElement>) {
  event.currentTarget.blur()
}

export function preventNativeNumberInputStep(event: KeyboardEvent<HTMLInputElement>) {
  if (event.key === 'ArrowUp' || event.key === 'ArrowDown') {
    event.preventDefault()
  }
}
