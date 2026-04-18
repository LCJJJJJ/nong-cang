export interface SystemPingResponse {
  demoId: string
  service: string
  status: string
  serverTime: string
}

export interface SystemEchoRequest {
  content: string
}

export interface SystemEchoResponse {
  content: string
  length: number
}
